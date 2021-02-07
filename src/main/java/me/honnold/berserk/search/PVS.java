package me.honnold.berserk.search;

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

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final MoveGenerator moveGenerator = MoveGenerator.getInstance();
    private final Transpositions transpositions = Transpositions.getInstance();
    private final ZobristHash hash = ZobristHash.getInstance();
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
        int score = 0;

        for (int depth = 1; depth <= MAX_DEPTH; depth++) {
            score = rootPvs(alpha, beta, depth, this.root);

            if (score <= alpha || score >= beta)
                score =
                        rootPvs(
                                -Constants.CHECKMATE_MAX,
                                Constants.CHECKMATE_MAX,
                                depth,
                                this.root);

            alpha = score - 50;
            beta = score + 50;

            if (!running.get()) break;
        }

        running.set(false);
    }

    public int rootPvs(int alpha, int beta, int depth, Position position) {
        boolean inCheck = position.inCheck();
        if (inCheck) depth++;

        List<Move> moves = moveGenerator.getAllMoves(position);
        moveGenerator.sortMoves(moves, pvTable[0][0], position, 0);

        int score = 0;
        int movesSearched = 0;
        for (int i = 0; i < moves.size(); i++) {
            Move move = moves.get(i);
            Position nextPosition = new Position(position);

            boolean isValid = nextPosition.makeMove(move);
            if (!isValid) continue;

            repetitions.add(nextPosition.zHash);

            if (movesSearched == 0
                    || -pvSearch(-alpha - 1, -alpha, depth - 1, 1, true, nextPosition)
                    > alpha) {
                score = -pvSearch(-beta, -alpha, depth - 1, 1, true, nextPosition);
            }

            movesSearched++;
            repetitions.pop();

            if (!running.get()) break;

            if (score > alpha) {
                pvTable[0][0] = move;
                this.results.setBestMove(move);
                this.results.setScore(score);

                if (pvLength[1] - 1 >= 0)
                    System.arraycopy(pvTable[1], 1, pvTable[0], 1, pvLength[1] - 1);
                pvLength[0] = pvLength[1];

                printPV(depth, score);

                if (score >= beta) {
                    transpositions.putEvaluationForPosition(
                            position, depth, beta, EvaluationFlag.UPPER, move);
                    return beta;
                }

                alpha = score;
                transpositions.putEvaluationForPosition(
                        position, depth, alpha, EvaluationFlag.LOWER, move);
            }
        }

        transpositions.putEvaluationForPosition(position, depth, alpha, EvaluationFlag.EXACT, pvTable[0][0]);

        return alpha;
    }

    public int pvSearch(
            int alpha,
            int beta,
            int depth,
            int ply,
            boolean canNull,
            Position position) {
        if (!running.get()) return 0;

        if (ply > 50)
            System.out.println("we deep");

        int mateMaxValue = Constants.CHECKMATE_MAX - ply;
        if (alpha < -mateMaxValue) alpha = -mateMaxValue;
        if (beta > mateMaxValue - 1) beta = mateMaxValue - 1;
        if (alpha >= beta) return alpha;

        boolean inCheck = position.inCheck();
        if (inCheck) depth++;

        pvLength[ply] = ply;

        if (depth <= 0) return quiesce(alpha, beta, position);

        results.incNodes();

        Evaluation evaluation = transpositions.getEvaluationForPosition(position);
        if (evaluation != null && evaluation.getDepth() >= depth) {
            this.results.incTableHits();

            int score = evaluation.getScore();
            if (evaluation.getFlag() == EvaluationFlag.EXACT) return score;
            if (evaluation.getFlag() == EvaluationFlag.LOWER && score >= beta) return score;
            if (evaluation.getFlag() == EvaluationFlag.UPPER && score <= alpha) return score;
        }

        boolean isPv = beta - alpha != 1;
        int score = -Constants.CHECKMATE_MAX, bestScore = score, alphaOg = alpha;
        Move bestMove = null;
        Position nextPosition = new Position(position);

        if (!isPv && !inCheck) {
            int staticEval = position.getValue();
            if (evaluation != null) {
                if (evaluation.getFlag() == EvaluationFlag.EXACT ||
                        evaluation.getFlag() == EvaluationFlag.UPPER && evaluation.getScore() < staticEval ||
                        evaluation.getFlag() == EvaluationFlag.LOWER && evaluation.getScore() > staticEval) {
                    staticEval = evaluation.getScore();
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

                // Make the null move
                if (nextPosition.epSquare != -1)
                    nextPosition.zHash ^= hash.getEpKey(nextPosition.epSquare);
                nextPosition.epSquare = -1;

                nextPosition.sideToMove = 1 - nextPosition.sideToMove;
                nextPosition.zHash ^= hash.getSideKey();

                repetitions.add(nextPosition.zHash);

                score = depth - R <= 0 ?
                        -quiesce(-beta, -beta + 1, nextPosition) :
                        -pvSearch(-beta, -beta + 1, depth - R, ply + 1, false, nextPosition);

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
                && position.getValue() + futilityMargins[depth] <= alpha)
            enableFutilityPruning = true;

        List<Move> moves = moveGenerator.getAllMoves(position);
        moveGenerator.sortMoves(moves, pvTable[0][ply], position, ply);

        boolean hasLegalMove = false;

        for (int i = 0; i < moves.size(); i++) {
            Move move = moves.get(i);
            nextPosition = new Position(position);
            boolean isValid = nextPosition.makeMove(move);
            if (!isValid) continue;
            hasLegalMove = true;

            repetitions.add(nextPosition.zHash);
            if (repetitions.isRepetition()) {
                repetitions.pop();
                return 0;
            }

            if (enableFutilityPruning
                    && !move.capture
                    && !move.epCapture
                    && move.promotionPiece == -1
                    && !nextPosition.inCheck()) {
                repetitions.pop();
                this.results.incFutilityPrunes();
                continue;
            }

            score = alpha + 1;

            int reductionDepth = 1;

            if (depth > 2
                    && i > 0
                    && !move.capture
                    && !move.epCapture
                    && move.promotionPiece == -1) {
                reductionDepth = LMR_TABLE[Math.min(depth, 63)][Math.min(i, 63)];

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
                                nextPosition);
            }

            if (score > alpha && i > 0) {
                score = -pvSearch(-alpha - 1, -alpha, depth - 1, ply + 1, true, nextPosition);
            }

            if (score > alpha) {
                score = -pvSearch(-beta, -alpha, depth - 1, ply + 1, true, nextPosition);
            }

            repetitions.pop();

            if (!running.get()) return 0;

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;

                if (score > alpha) {
                    if (score >= beta) {
                        if (!move.capture && !move.epCapture) moveGenerator.addKiller(move, ply);

                        bestScore = beta;
                        break;
                    }

                    if (!move.capture && !move.epCapture)
                        moveGenerator.setHistoricalMoveScore(move, score);

                    pvTable[ply][ply] = move;
                    if (pvLength[ply + 1] - (ply + 1) >= 0)
                        System.arraycopy(
                                pvTable[ply + 1],
                                ply + 1,
                                pvTable[ply],
                                ply + 1,
                                pvLength[ply + 1] - (ply + 1));
                    pvLength[ply] = pvLength[ply + 1];

                    alpha = bestScore;
                }
            }
        }

        if (!hasLegalMove) {
            if (inCheck) bestScore = -Constants.CHECKMATE_MAX + ply;
            else bestScore = 0;
        }

        EvaluationFlag flag = EvaluationFlag.EXACT;
        if (bestScore >= beta) {
            flag = EvaluationFlag.LOWER;
            this.results.incFailHighs();
        } else if (bestScore <= alphaOg) {
            flag = EvaluationFlag.UPPER;
            this.results.incFailLows();
        } else {
            this.results.incExacts();

        }

        transpositions.putEvaluationForPosition(position, depth, bestScore, flag, bestMove);
        return bestScore;
    }

    public int quiesce(int alpha, int beta, Position position) {
        this.results.incNodes();

        GameStage stage = position.getGameStage();

        int score = position.getValue();
        int standPat = score;

        if (score >= beta) return beta;

        if (alpha < score) alpha = score;

        List<Move> moves = moveGenerator.getAllMoves(position);

        for (Move m : moves) {
            Position next = new Position(position);
            boolean isValid = next.makeMove(m);
            if (!isValid) continue;
            if (!m.capture && !m.epCapture && m.promotionPiece == -1) continue;

            if ((m.capture || m.epCapture) && stage != GameStage.ENDGAME) {
                int capturedIdx =
                        m.epCapture ? 1 - position.sideToMove : position.getCapturedPieceIdx(m.end);
                int captureSq = m.epCapture ? m.end + (position.sideToMove == 0 ? -8 : 8) : m.end;

                if (standPat
                        + Piece.getPieceValue(capturedIdx, stage)
                        + Piece.getPositionValue(capturedIdx, captureSq, stage)
                        + 200
                        < alpha) continue;
            }

            score = -1 * quiesce(-beta, -alpha, next);

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
            return "Results{" +
                    "score=" + score +
                    ", bestMove=" + bestMove +
                    ", nodes=" + nodes +
                    ", tableHits=" + tableHits +
                    ", staticEvalTrims=" + staticEvalTrims +
                    ", nullMoveTrims=" + nullMoveTrims +
                    ", futilityPrunes=" + futilityPrunes +
                    ", failHighs=" + failHighs +
                    ", failLows=" + failLows +
                    ", exacts=" + exacts +
                    ", razors=" + razors +
                    '}';
        }

        public int getRazors() {
            return razors;
        }

        public void incRazors() {
            this.razors++;
        }
    }
}
