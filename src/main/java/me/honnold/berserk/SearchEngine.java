package me.honnold.berserk;

import java.util.Arrays;

import static me.honnold.berserk.BoardUtils.getBit;

public class SearchEngine {
    public static final int CHECKMATE_MAX = 69290;
    public static final int MAX_DEPTH = 32;

    public static final int[][] historyCache = new int[12][64];
    public final TranspositionTable table;
    public final Repetitions repetitions;
    public int nodes = 0;
    public int hits = 0;
    private boolean running = false;

    public SearchEngine(TranspositionTable table, Repetitions repetitions) {
        this.table = table;
        this.repetitions = repetitions;
    }

    public int searchMtdbi(Position p) {
        running = true;
        table.clear();

        long startTime = System.currentTimeMillis();

        int score = 0;

        for (int depth = 1; depth <= MAX_DEPTH; depth++) {
            score = mtdbi(score, depth, p);

            if (!running) break;

            StringBuilder output = new StringBuilder();
            output.append("info depth ").append(depth)
                    .append(" score ").append(score)
                    .append(" nodes ").append(nodes)
                    .append(" nps ").append(String.format("%.0f", 1000.0 * nodes / (System.currentTimeMillis() - startTime)))
                    .append(" pv ").append(getPv(p));
            System.out.println(output);
        }

        running = false;
        return score;
    }

    public boolean isRunning() {
        return this.running;
    }

    public void interrupt() {
        this.running = false;
    }

    private String getPv(Position p) {
        StringBuilder builder = new StringBuilder();
        Move m = table.getMoveForPosition(p);

        int iterations = 0;
        while (m != null && iterations++ <= 24) {
            builder.append(m.toString())
                    .append(" ");

            p = new Position(p);
            p.makeMove(m);
            m = table.getMoveForPosition(p);
        }

        return builder.toString();
    }

    public int mtdbi(int guess, int depth, Position p) {
        int upper = CHECKMATE_MAX;
        int lower = -CHECKMATE_MAX;
        int beta = guess;
        int gamma;

        do {
            gamma = alphaBeta(beta - 1, beta, depth, 0, p);

            if (gamma < beta)
                upper = gamma;
            else
                lower = gamma;

            beta = (lower + upper + 1) / 2;
        } while (lower < upper - 13);

        return gamma;
    }

    public int alphaBeta(int alpha, int beta, int depth, int ply, Position p) {
        int mateMaxValue = CHECKMATE_MAX - ply;

        if (mateMaxValue < beta) {
            beta = mateMaxValue;
            if (alpha >= mateMaxValue) return mateMaxValue;
        }


        boolean inCheck = p.isSquareAttacked(BoardUtils.getLSBIndex(p.pieceBitboards[10 + p.sideToMove]), 1 - p.sideToMove);
        if (inCheck) depth++;

        if (depth == 0) return quiesce(alpha, beta, p);

        nodes++;

        TranspositionTable.Evaluation previousEval = table.getEvaluationForPosition(p);

        if (previousEval != null) {
            if (previousEval.depth >= depth) {
                hits++;

                if (previousEval.alpha >= beta) return previousEval.alpha;
                if (previousEval.beta <= alpha) return previousEval.beta;

                alpha = Math.max(alpha, previousEval.alpha);
                beta = Math.min(beta, previousEval.beta);
            }
        }

        int gamma = -CHECKMATE_MAX, a = alpha, score;
        Position next;

        // null move pruning
        if (depth >= 3 && !inCheck && ply > 0) {
            int R = depth > 6 ? 4 : 3;
            next = new Position(p);

            repetitions.setCurrentPosition(p.zHash);

            // Make the null move
            if (next.epSquare != -1) next.zHash ^= ZobristHash.epKeys[next.epSquare];
            next.epSquare = -1;

            next.sideToMove = 1 - next.sideToMove;
            next.zHash ^= ZobristHash.sideKey;
            next.value = -next.value;

            score = -1 * alphaBeta(-beta, -beta + 1, depth - R, ply + 1, next);

            repetitions.decrement();
            if (!running) return 0;

            if (score >= beta) {
                depth -= 4;
                if (depth <= 0) return quiesce(alpha, beta, p);
            }
        }

        Move[] moves = p.getMoves();
        Move pv = table.getMoveForPosition(p);

        Arrays.sort(moves, (m1, m2) -> {
            if (m1.equals(m2)) return 0;
            if (m1.equals(pv)) return -1;
            if (m2.equals(pv)) return 1;

            if (m1.capture && m2.capture) {
                int move1Capture = -1, move2Capture = -1;
                for (int i = 0; i < 12; i++) {
                    long bb = p.pieceBitboards[i];

                    if (getBit(bb, m1.end))
                        move1Capture = i;

                    if (getBit(bb, m2.end))
                        move2Capture = i;

                    if (move1Capture >= 0 && move2Capture >= 0) break;
                }

                return Piece.mvvLva[m2.pieceIdx][move2Capture] - Piece.mvvLva[m1.pieceIdx][move1Capture];
            } else if (m1.capture) {
                return -1;
            } else if (m2.capture) {
                return 1;
            } else {
                return historyCache[m2.pieceIdx][m2.end] - historyCache[m1.pieceIdx][m1.end];
            }
        });

        boolean hasLegalMove = false;

        int fullDepthSearches = 0;
        for (Move m : moves) {
            if (gamma >= beta) break;

            next = new Position(p);
            boolean isValid = next.makeMove(m);
            if (!isValid) continue;

            if (repetitions.isRepetition(next))
                return 0;

            repetitions.setCurrentPosition(p.zHash);
            hasLegalMove = true;

            if (fullDepthSearches < 4 || depth < 3 || inCheck || m.capture || m.epCapture || m.promotionPiece != -1) {
                score = -alphaBeta(-beta, -a, depth - 1, ply + 1, next);
            } else {
                score = -alphaBeta(-a - 1, -a, depth - 2, ply + 1, next);

                if (score > a && score < beta)
                    score = -alphaBeta(-beta, -a, depth - 1, ply + 1, next);
            }

            repetitions.decrement();
            fullDepthSearches++;

            if (!running) return 0;

            if (score > gamma)
                gamma = score;

            if (gamma > a) {
                a = gamma;

                if (!m.capture) historyCache[m.pieceIdx][m.end] = gamma;
                table.putMoveForPosition(p, m);
            }
        }

        if (!hasLegalMove)
            return inCheck ? -mateMaxValue : 0;

        if (gamma <= alpha) {
            table.putEvaluationForPosition(p, depth, -CHECKMATE_MAX, gamma);
        } else if (gamma < beta) {
            table.putEvaluationForPosition(p, depth, gamma, gamma);
        } else {
            table.putEvaluationForPosition(p, depth, gamma, CHECKMATE_MAX);
        }

        return gamma;
    }

    public int quiesce(int alpha, int beta, Position p) {
        nodes++;

        int score = p.value;

        if (score >= beta)
            return beta;

        if (alpha < score)
            alpha = score;

        Move[] moves = p.getMoves();

        for (Move m : moves) {
            if (!m.capture && !m.epCapture && m.promotionPiece == -1) continue;

            if (m.capture || m.epCapture) {
                int capturedIdx = m.epCapture ? 1 - p.sideToMove : p.getCapturedPieceIdx(m.end);
                int captureSq = m.epCapture ? m.end + (p.sideToMove == 0 ? -8 : 8) : m.end;

                if (p.value + Piece.baseValues[capturedIdx >> 1] + Piece.squareValues[capturedIdx][captureSq] + 200 < alpha)
                    continue;
            }

            Position next = new Position(p);
            boolean isValid = next.makeMove(m);
            if (!isValid) continue;

            score = -1 * quiesce(-beta, -alpha, next);

            if (!running) return 0;

            if (score >= beta)
                return beta;

            if (score > alpha)
                alpha = score;
        }

        return alpha;
    }
}
