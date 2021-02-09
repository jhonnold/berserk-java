package me.honnold.berserk.search;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import me.honnold.berserk.board.GameStage;
import me.honnold.berserk.board.Piece;
import me.honnold.berserk.board.Position;
import me.honnold.berserk.eval.Constants;
import me.honnold.berserk.moves.Move;
import me.honnold.berserk.moves.MoveGenerator;
import me.honnold.berserk.tt.Evaluation;
import me.honnold.berserk.tt.EvaluationFlag;
import me.honnold.berserk.tt.Transpositions;
import me.honnold.berserk.tt.ZobristHash;

public class PVS implements Runnable {
    public static final int MAX_DEPTH = 100;
    private static final int[][] LMR_TABLE = new int[64][64];

    static {
        // Ethereal LMR formula with depth and number of performed moves
        for (int depth = 1; depth < 64; depth++) {
            for (int moveNumber = 1; moveNumber < 64; moveNumber++) {
                LMR_TABLE[depth][moveNumber] =
                        (int) (0.6f + Math.log(depth) * Math.log(moveNumber * 1.2f) / 2.5f);
            }
        }
    }

    private final Repetitions repetitions = Repetitions.getInstance();
    private final int[] pvLength = new int[MAX_DEPTH];
    private final Move[][] pvTable = new Move[MAX_DEPTH][MAX_DEPTH];
    private final Move[] moves = new Move[100 * MAX_DEPTH];
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final MoveGenerator moveGenerator = MoveGenerator.getInstance();
    private final Transpositions transpositions = Transpositions.getInstance();
    private final Position root;
    private final Results results;

    public PVS(Position root) {
        this.root = root;
        this.results = new Results();
    }

    @Override
    public void run() {
        running.set(true);

        transpositions.clearEvaluations();
        moveGenerator.clearHistoricalMoveScores();
        moveGenerator.clearKillers();

        int alpha = -Constants.CHECKMATE_MAX;
        int beta = Constants.CHECKMATE_MAX;

        int score = pvSearch(alpha, beta, 1, 0, true, this.root);

        for (int depth = 2; depth <= MAX_DEPTH && running.get(); depth++) {
            int delta = depth > 5 && Math.abs(score) < 1000 ? 20 : Constants.CHECKMATE_MAX;
            alpha = Math.max(score - delta, -Constants.CHECKMATE_MAX);
            beta = Math.min(score + delta, Constants.CHECKMATE_MAX);

            while (running.get()) {
                score = pvSearch(alpha, beta, depth, 0, true, this.root);

                if (score <= alpha) {
                    alpha = Math.max(alpha - delta, -Constants.CHECKMATE_MAX);
                    delta *= 2;
                } else if (score >= beta) {
                    beta = Math.min(beta + delta, Constants.CHECKMATE_MAX);
                    delta *= 2;
                } else {
                    break;
                }
            }
        }

        running.set(false);
    }

    public int pvSearch(
            int alpha, int beta, int depth, int ply, boolean canNull, Position position) {
        if (!running.get()) return 0;

        alpha = Math.max(alpha, -Constants.CHECKMATE_MAX + ply);
        beta = Math.min(beta, Constants.CHECKMATE_MAX - ply - 1);
        if (alpha >= beta) return alpha;

        boolean inCheck = position.inCheck();
        if (inCheck) depth++;

        pvLength[ply] = ply;

        if (depth <= 0) return quiesce(alpha, beta, position);

        boolean isPv = beta - alpha != 1;

        Evaluation evaluation = transpositions.getEvaluationForPosition(position);
        if (evaluation != null && evaluation.getDepth() >= depth) {
            this.results.incTableHits();

            int score = evaluation.getScore();
            if (evaluation.getFlag() == EvaluationFlag.EXACT) return score;
            if (!isPv && evaluation.getFlag() == EvaluationFlag.LOWER && score > beta) return score;
            if (!isPv && evaluation.getFlag() == EvaluationFlag.UPPER && score < alpha)
                return score;
        }

        results.incNodes();

        int score = -Constants.CHECKMATE_MAX, bestScore = score, alphaOg = alpha;
        Move bestMove = null;

        int staticEval = position.getValue();
        if (!isPv && !inCheck) {
            if (evaluation != null && evaluation.getDepth() >= depth) {
                EvaluationFlag flag = evaluation.getFlag();
                int evalScore = evaluation.getScore();
                if (flag == EvaluationFlag.EXACT
                        || flag == EvaluationFlag.UPPER && evalScore < staticEval
                        || flag == EvaluationFlag.LOWER && evalScore > staticEval) {
                    staticEval = evalScore;
                }
            }

            if (depth < 5) {
                int margin = 70 * depth + 10 * Math.max(0, depth - 2);
                if (staticEval - margin >= beta) {
                    this.results.incStaticEvalTrims();
                    return beta;
                }
            }

            int[] razors = {0, 240, 280, 300};
            if (depth < 4 && Math.abs(alpha) < Constants.CHECKMATE_MIN) {
                if (staticEval + razors[depth] < alpha) {
                    score = quiesce(alpha - razors[depth], alpha - razors[depth] + 1, position);
                    if (score + razors[depth] <= alpha) {
                        return score;
                    }
                }
            }

            if (depth >= 3
                    && canNull
                    && staticEval > beta
                    && position.getGameStage() != GameStage.ENDGAME) {
                int R = depth / 4 + 3 + Math.min((staticEval - beta) / 80, 3);

                position.nullMove();
                repetitions.add(position.zHash);

                int newDepth = Math.max(0, depth - R);
                score = -pvSearch(-beta, -beta + 1, newDepth, ply + 1, false, position);

                position.undoNullMove();
                repetitions.pop();

                if (!running.get()) return 0;
                if (score >= beta) {
                    this.results.incNullMoveTrims();
                    return beta;
                }
            }
        }

        boolean enableFutilityPruning = false;
        int[] futilityMargins = {0, 80, 170, 270, 380, 500, 630};
        if (depth <= 6
                && !isPv
                && !inCheck
                && Math.abs(alpha) < Constants.CHECKMATE_MIN
                && staticEval + futilityMargins[depth] <= alpha) enableFutilityPruning = true;

        List<Move> moves = moveGenerator.getAllMoves(position);
        moveGenerator.sortMoves(moves, pvTable[0][ply], position, ply);

        boolean hasLegalMove = false;

        int searches = 0;
        for (Move move : moves) {
            boolean isValid = position.makeMove(move);
            if (!isValid) {
                position.undoMove(move);
                continue;
            }

            hasLegalMove = true;
            searches++;
            repetitions.add(position.zHash);

            // TODO: Add material draw and 50 move rep here
            if (repetitions.isRepetition()) {
                score = 0;
            } else {

                if (enableFutilityPruning
                        && !move.isCapture()
                        && !move.isEPCapture()
                        && !move.isPromotion()
                        && !position.inCheck()) {
                    position.undoMove(move);
                    repetitions.pop();
                    this.results.incFutilityPrunes();
                    continue;
                }

                score = alpha + 1;

                int reductionDepth = 1;

                if (depth > 2
                        && searches > 1
                        && !move.isCapture()
                        && !move.isEPCapture()
                        && !move.isPromotion()) {
                    reductionDepth = LMR_TABLE[Math.min(depth, 63)][Math.min(searches, 63)];

                    if (moveGenerator.isAKiller(move, ply)) reductionDepth--;

                    if (!isPv) reductionDepth++;

                    reductionDepth = Math.min(depth - 1, Math.max(reductionDepth, 1));
                }

                if (reductionDepth != 1) {
                    score =
                            -pvSearch(
                                    -alpha - 1,
                                    -alpha,
                                    depth - reductionDepth,
                                    ply + 1,
                                    true,
                                    position);
                }

                if (score > alpha && searches > 1) {
                    score = -pvSearch(-alpha - 1, -alpha, depth - 1, ply + 1, true, position);
                }

                if (score > alpha) {
                    score = -pvSearch(-beta, -alpha, depth - 1, ply + 1, true, position);
                }
            }

            position.undoMove(move);
            repetitions.pop();

            if (!running.get()) return 0;

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;

                pvTable[ply][ply] = move;
                if (pvLength[ply + 1] - (ply + 1) >= 0)
                    System.arraycopy(
                            pvTable[ply + 1],
                            ply + 1,
                            pvTable[ply],
                            ply + 1,
                            pvLength[ply + 1] - (ply + 1));
                pvLength[ply] = pvLength[ply + 1];

                if (ply == 0 && running.get()) {
                    this.results.setBestMove(bestMove);
                    this.results.setScore(score);
                    printPV(depth, score);
                }

                alpha = Math.max(alpha, score);
                if (alpha >= beta) {
                    this.results.incFailHighs();

                    if (!move.isCapture() && !move.isEPCapture())
                        moveGenerator.addKiller(move, ply);

                    break;
                }

                if (!move.isCapture() && !move.isEPCapture())
                    moveGenerator.setHistoricalMoveScore(move, alpha);
            }
        }

        if (!hasLegalMove) {
            if (inCheck) bestScore = -Constants.CHECKMATE_MAX + ply;
            else bestScore = 0;
        }

        EvaluationFlag flag = EvaluationFlag.EXACT;
        if (bestScore >= beta) {
            flag = EvaluationFlag.LOWER;
        } else if (bestScore <= alphaOg) {
            flag = EvaluationFlag.UPPER;
        }

        transpositions.putEvaluationForPosition(position, depth, bestScore, flag, bestMove);
        return bestScore;
    }

    public int quiesce(int alpha, int beta, Position position) {
        this.results.incNodes();

        Evaluation evaluation = transpositions.getEvaluationForPosition(position);
        if (evaluation != null) {
            this.results.incTableHits();

            int score = evaluation.getScore();
            if (evaluation.getFlag() == EvaluationFlag.EXACT) return score;
            if (evaluation.getFlag() == EvaluationFlag.LOWER && score >= beta) return score;
            if (evaluation.getFlag() == EvaluationFlag.UPPER && score <= alpha) return score;
        }

        int staticEval = position.getValue();
        if (evaluation != null) {
            if (evaluation.getFlag() == EvaluationFlag.EXACT
                    || evaluation.getFlag() == EvaluationFlag.UPPER
                            && evaluation.getScore() < staticEval
                    || evaluation.getFlag() == EvaluationFlag.LOWER
                            && evaluation.getScore() > staticEval) {
                staticEval = evaluation.getScore();
            }
        }

        if (staticEval >= beta) return beta;

        alpha = Math.max(alpha, staticEval);

        GameStage stage = position.getGameStage();

        List<Move> moves = moveGenerator.getAllMoves(position);

        for (Move m : moves) {
            if (!m.isCapture() && !m.isEPCapture() && !m.isPromotion()) continue;
            if (m.isPromotion()) {
                if (m.getPromotionPiece() < 8) continue;
            } else if (staticEval
                            + 200
                            + Piece.getPieceValue(position.getCapturedPieceIdx(m.getEnd()), stage)
                    < alpha) {
                continue;
            }

            boolean isValid = position.makeMove(m);
            if (!isValid) {
                position.undoMove(m);
                continue;
            }

            int score = -1 * quiesce(-beta, -alpha, position);
            position.undoMove(m);

            if (!running.get()) return 0;

            if (score >= beta) return beta;
            if (score > alpha) alpha = score;
        }

        return alpha;
    }

    private void printPV(int depth, int score) {
        String scoreUnit = Math.abs(score) >= Constants.CHECKMATE_MIN ? "mate " : "cp ";
        int scoreValue =
                Math.abs(score) <= Constants.CHECKMATE_MIN
                        ? score
                        : score < -Constants.CHECKMATE_MIN
                                ? -((Constants.CHECKMATE_MAX + score) / 2 + 1)
                                : (Constants.CHECKMATE_MAX - score) / 2 + 1;

        String output =
                "info depth "
                        + depth
                        + " score "
                        + scoreUnit
                        + scoreValue
                        + " nodes "
                        + results.getNodes()
                        + " nps "
                        + String.format(
                                "%.0f",
                                1_000_000_000.0
                                        * results.getNodes()
                                        / (System.nanoTime() - results.getStartTime()))
                        + " pv "
                        + getPv();
        System.out.println(output);
    }

    private String getPv() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < pvLength[0]; i++) builder.append(pvTable[0][i]).append(" ");

        return builder.toString();
    }

    public Results getResults() {
        return results;
    }

    public void stop() {
        running.set(false);
    }

    public static class Results {
        private final long startTime;
        private int score;
        private Move bestMove;
        private int nodes;
        private int tableHits;
        private int staticEvalTrims;
        private int nullMoveTrims;
        private int futilityPrunes;
        private int failHighs;
        private int failLows;
        private int exacts;
        private int razors;

        private Results() {
            this.startTime = System.nanoTime();
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }

        public Move getBestMove() {
            return bestMove;
        }

        public void setBestMove(Move bestMove) {
            this.bestMove = bestMove;
        }

        public int getNodes() {
            return nodes;
        }

        public void incNodes() {
            this.nodes++;
        }

        public int getTableHits() {
            return tableHits;
        }

        public void incTableHits() {
            this.tableHits++;
        }

        public long getStartTime() {
            return startTime;
        }

        public int getStaticEvalTrims() {
            return staticEvalTrims;
        }

        public void incStaticEvalTrims() {
            this.staticEvalTrims++;
        }

        public int getNullMoveTrims() {
            return nullMoveTrims;
        }

        public void incNullMoveTrims() {
            this.nullMoveTrims++;
        }

        public int getFutilityPrunes() {
            return futilityPrunes;
        }

        public void incFutilityPrunes() {
            this.futilityPrunes++;
        }

        public int getFailHighs() {
            return failHighs;
        }

        public void incFailHighs() {
            this.failHighs++;
        }

        public int getFailLows() {
            return failLows;
        }

        public void incFailLows() {
            this.failLows++;
        }

        public int getExacts() {
            return exacts;
        }

        public void incExacts() {
            this.exacts++;
        }

        @Override
        public String toString() {
            return "Results{"
                    + "score="
                    + score
                    + ", bestMove="
                    + bestMove
                    + ", nodes="
                    + nodes
                    + ", tableHits="
                    + tableHits
                    + ", staticEvalTrims="
                    + staticEvalTrims
                    + ", nullMoveTrims="
                    + nullMoveTrims
                    + ", futilityPrunes="
                    + futilityPrunes
                    + ", failHighs="
                    + failHighs
                    + ", failLows="
                    + failLows
                    + ", exacts="
                    + exacts
                    + ", razors="
                    + razors
                    + '}';
        }

        public int getRazors() {
            return razors;
        }

        public void incRazors() {
            this.razors++;
        }
    }
}
