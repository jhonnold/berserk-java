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
import me.honnold.berserk.tt.TranspositionEvaluations;
import me.honnold.berserk.tt.ZobristHash;
import me.honnold.berserk.util.BBUtils;

public class PVS implements Runnable {
    public static final int MAX_DEPTH = 100;
    public final Repetitions repetitions = Repetitions.getInstance();
    public final int[] pvLength = new int[MAX_DEPTH];
    public final Move[][] pvTable = new Move[MAX_DEPTH][MAX_DEPTH];
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final MoveGenerator moveGenerator = MoveGenerator.getInstance();
    private final TranspositionEvaluations transpositions = TranspositionEvaluations.getInstance();
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

        int alpha = -Constants.CHECKMATE_MAX;
        int beta = Constants.CHECKMATE_MAX;
        int score = 0;

        for (int depth = 1; depth <= MAX_DEPTH; depth++) {
            score = pvs(alpha, beta, depth, 0, this.root);

            if (score <= alpha || score >= beta) {
                alpha = -Constants.CHECKMATE_MAX;
                beta = Constants.CHECKMATE_MAX;
            } else {
                alpha = score - 50;
                beta = score + 50;
            }

            if (!running.get()) break;
        }

        running.set(false);
    }

    public int pvs(int alpha, int beta, int depth, int ply, Position position) {
        int mateMaxValue = Constants.CHECKMATE_MAX - ply;
        boolean pvNode = beta - alpha > 1;
        EvaluationFlag flag = EvaluationFlag.ALPHA;

        Evaluation evaluation = transpositions.getEvaluationForPosition(position);
        if (evaluation != null && ply > 0 && !pvNode) {
            if (evaluation.getDepth() >= depth) {
                this.results.incTableHits();

                if (evaluation.getFlag() == EvaluationFlag.EXACT) return evaluation.getScore();
                if (evaluation.getFlag() == EvaluationFlag.BETA && evaluation.getScore() >= beta)
                    return beta;
                if (evaluation.getFlag() == EvaluationFlag.ALPHA && evaluation.getScore() <= alpha)
                    return alpha;
            }
        }

        boolean inCheck =
                position.isSquareAttacked(
                        BBUtils.getLSBIndex(position.pieceBitboards[10 + position.sideToMove]),
                        1 - position.sideToMove);
        if (inCheck) depth++;

        pvLength[ply] = ply;

        if (depth == 0) return quiesce(alpha, beta, position);

        this.results.incNodes();

        int bestScore = -Constants.CHECKMATE_MAX;
        int score = bestScore;
        Position nextPosition = null;

        // null move pruning
        if (depth >= 3 && !inCheck && ply > 0) {
            int R = depth > 6 ? 4 : 3;
            nextPosition = new Position(position);

            repetitions.add(position.zHash);

            // Make the null move
            if (nextPosition.epSquare != -1)
                nextPosition.zHash ^= hash.getEpKey(nextPosition.epSquare);
            nextPosition.epSquare = -1;

            nextPosition.sideToMove = 1 - nextPosition.sideToMove;
            nextPosition.zHash ^= hash.getSideKey();

            score = -1 * pvs(-beta, -beta + 1, depth - R, ply + 1, nextPosition);

            repetitions.pop();

            if (!running.get()) return 0;

            if (score >= beta) return beta;
        }

        List<Move> moves = moveGenerator.getAllMoves(position);
        moveGenerator.sortMoves(moves, pvTable[0][ply], position);

        boolean hasLegalMove = false;

        int fullDepthSearches = 0;
        for (Move move : moves) {
            nextPosition = new Position(position);
            boolean isValid = nextPosition.makeMove(move);
            if (!isValid) continue;

            if (repetitions.isRepetition(nextPosition)) return 0;

            repetitions.add(position.zHash);
            hasLegalMove = true;

            if (fullDepthSearches == 0) {
                score = -pvs(-beta, -alpha, depth - 1, ply + 1, nextPosition);
            } else {
                if (fullDepthSearches < 4
                        || depth < 3
                        || inCheck
                        || move.capture
                        || move.epCapture
                        || move.promotionPiece != -1) {
                    score = alpha + 1;
                } else {
                    score = -pvs(-alpha - 1, -alpha, depth - 2, ply + 1, nextPosition);
                }

                if (score > alpha) {
                    score = -pvs(-alpha - 1, -alpha, depth - 1, ply + 1, nextPosition);

                    if (score > alpha && score < beta)
                        score = -pvs(-beta, -alpha, depth - 1, ply + 1, nextPosition);
                }
            }

            repetitions.pop();
            fullDepthSearches++;

            if (!running.get()) return 0;

            if (score > bestScore) bestScore = score;

            if (bestScore >= beta) {
                transpositions.putEvaluationForPosition(position, beta, depth, EvaluationFlag.BETA);
                return beta;
            }

            if (bestScore > alpha) {
                alpha = bestScore;
                flag = EvaluationFlag.EXACT;

                if (!move.capture) moveGenerator.setHistoricalMoveScore(move, bestScore);

                pvTable[ply][ply] = move;
                if (pvLength[ply + 1] - (ply + 1) >= 0)
                    System.arraycopy(
                            pvTable[ply + 1],
                            ply + 1,
                            pvTable[ply],
                            ply + 1,
                            pvLength[ply + 1] - (ply + 1));
                pvLength[ply] = pvLength[ply + 1];

                if (ply == 0 && pvLength[0] > 0) {
                    this.results.setBestMove(pvTable[0][0]);

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
            }
        }

        if (!hasLegalMove) return inCheck ? -mateMaxValue : 0;

        transpositions.putEvaluationForPosition(position, bestScore, depth, flag);
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

            Position next = new Position(position);
            boolean isValid = next.makeMove(m);
            if (!isValid) continue;

            score = -1 * quiesce(-beta, -alpha, next);

            if (!running.get()) return 0;

            if (score >= beta) return beta;
            if (score > alpha) alpha = score;
        }

        return alpha;
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
    }
}
