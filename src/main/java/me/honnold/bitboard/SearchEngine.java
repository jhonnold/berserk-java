package me.honnold.bitboard;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class SearchEngine {
    public static final int CHECKMATE_MIN = 50710;
    public static final int CHECKMATE_MAX = 69290;
    public static final int MAX_DEPTH = 8;

    public int nodes = 0;
    public int hits = 0;
    public long startTime = 0;

    TranspositionTable table = new TranspositionTable();

    public Pair<Move, Integer> bestMove(Position p) {
        table.clear();
        nodes = 0;
        hits = 0;

        this.startTime = System.currentTimeMillis();
        int score = search(p);
        return Pair.of(table.getMoveForPosition(p), score);
    }

    public int search(Position p) {
        int score = 0;
        for (int depth = 1; depth <= MAX_DEPTH; depth++) {
            score = mtdf(-score, depth, p);

            long now = System.currentTimeMillis();
            System.out.printf("info depth %d score cp %d nodes %d nps %.0f pv %s%n", depth, score, nodes, (double) nodes / (now - this.startTime) * 1000, getPv(p, depth));
            if (Math.abs(score) == CHECKMATE_MAX) break;
        }

        return score;
    }

    private String getPv(Position p, int depth) {
        StringBuilder builder = new StringBuilder();
        Move m = table.getMoveForPosition(p);

        while (m != null && depth-- > 0) {
            builder.append(m.toString())
                    .append(" ");

            p = new Position(p);
            p.makeMove(m);
            m = table.getMoveForPosition(p);
        }

        return builder.toString();
    }

    public int mtdf(int guess, int depth, Position p) {
        int upper = CHECKMATE_MAX;
        int lower = -CHECKMATE_MAX;
        int beta = guess;
        int gamma;

        do {
            gamma = alphaBeta(beta - 1, beta, depth, p, true);

            if (gamma < beta)
                upper = gamma;
            else
                lower = gamma;

            beta = (lower + upper + 1) / 2;
        } while (lower < upper);

        return gamma;
    }

    public int alphaBeta(int alpha, int beta, int depth, Position p, boolean isRoot) {
        depth = Math.max(0, depth);

        if (p.value <= -CHECKMATE_MIN)
            return -CHECKMATE_MAX;

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

        int gamma = -CHECKMATE_MAX;
        int a = alpha;
        int score;
        Position next;

        List<Move> moves = p.getMoves();
        Move killer = table.getMoveForPosition(p);

        if (killer != null)
            moves.add(0, killer);

        for (int i = 1; i <= moves.size(); i++) {
            if (gamma >= beta) break;

            Move m = moves.get(i - 1);
//            if (isRoot)
//                System.out.printf("info depth %d currmove %s currmovenumber %d%n",  depth, m, i);

            next = new Position(p);
            boolean isValid = next.makeMove(m);
            if (!isValid) continue;

            score = -alphaBeta(-beta, -a, depth - 1, next, false);

            if (score > gamma)
                gamma = score;

            if (gamma > a) {
                a = gamma;
                table.putMoveForPosition(p, m);
            }

        }

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

        if (score <= -CHECKMATE_MIN)
            return -CHECKMATE_MAX;

        if (score >= beta)
            return beta;

        if (alpha < score)
            alpha = score;

        List<Move> moves = p.getMoves();

        for (Move m : moves) {
            if (!m.capture && !m.epCapture) continue;

            Position next = new Position(p);
            boolean isValid = next.makeMove(m);
            if (!isValid) continue;

            score = -1 * quiesce(-beta, -alpha, next);

            if (score >= beta)
                return beta;
            if (score > alpha)
                alpha = score;
        }

        return alpha;
    }
}
