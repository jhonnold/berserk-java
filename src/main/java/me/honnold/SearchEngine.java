package me.honnold;

import me.honnold.piece.Color;
import me.honnold.position.Move;
import me.honnold.position.Position;
import me.honnold.tt.TranspositionTable;
import me.honnold.util.FEN;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class SearchEngine {
    private static final int CHECKMATE_MIN = 50710;
    private static final int CHECKMATE_MAX = 69290;
    private static final int MAX_DEPTH = 1000;
    private static final int MAX_SEARCH_TIME = 2500;

    private long startTime = 0;

    public int nodes = 0;
    public int hits = 0;

    private final TranspositionTable table = new TranspositionTable();

    public Pair<Move, Integer> bestMove(Position p) {
        table.clear();
        nodes = 0;
        hits = 0;

        this.startTime = System.currentTimeMillis();
        int score = search(p);
        return Pair.of(table.getMoveForPosition(p), score);
    }

    private boolean timeup() {
        return System.currentTimeMillis() - this.startTime > MAX_SEARCH_TIME;
    }

    public int search(Position p) {
        int score = 0;
        for (int depth = 1; depth <= MAX_DEPTH; depth++) {
            score = mtdf(-score, depth, p);

            if (timeup()) break;
            System.out.printf("info depth %d score cp %d nodes %d pv %s%n", depth, score, nodes, getPv(p));
        }

        return score;
    }

    private String getPv(Position p) {
        StringBuilder builder = new StringBuilder();
        Move m = table.getMoveForPosition(p);

        while (m != null) {
            builder.append(FEN.convertIdxToSquare(m.getStart(), p.getMoving() == Color.WHITE))
                    .append(FEN.convertIdxToSquare(m.getEnd(), p.getMoving() == Color.WHITE))
                    .append(" ");

            p = p.move(m);
            m = table.getMoveForPosition(p);
        }

        return builder.toString();
    }

    public int mtdf(int guess, int depth, Position p) {
        int gamma = guess;
        int upper = CHECKMATE_MAX;
        int lower = -CHECKMATE_MAX;

        do {
            int beta = gamma;
            if (gamma == lower) beta ++;

            gamma = alphaBeta(beta - 1, beta, depth, p);

            if (gamma < beta)
                upper = gamma;
            else
                lower = gamma;
        } while (lower < upper - 20);

        return gamma;
    }

    public int alphaBeta(int alpha, int beta, int depth, Position p) {
        nodes++;

        if (p.getScore() <= -CHECKMATE_MIN)
            return -CHECKMATE_MAX;

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

        if (depth == 0) return quiesce(alpha, beta, p);

        int gamma = -CHECKMATE_MAX;

        List<Move> moves = p.generateMoves();
        Move killer = table.getMoveForPosition(p);

        if (killer != null)
            moves.add(0, killer);

        int a = alpha;

        for (Move m : moves) {
            if (gamma >= beta) break;

            Position next = p.move(m);

            int score = -1 * alphaBeta(-beta, -a, depth - 1, next);

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

        int score = p.getScore();
        if (score >= beta)
            return score;

        if (alpha < score)
            alpha = score;

        List<Move> moves = p.generateMoves();

        for (Move m: moves) {
            if (!m.isCapture()) continue;

            score = -1 * quiesce(-beta, -alpha, p.move(m));

            if (score >= beta)
                return beta;
            if (score > alpha)
                alpha = score;
        }

        return alpha;
    }
}
