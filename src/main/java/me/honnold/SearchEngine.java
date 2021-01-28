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
    private static final int MAX_DEPTH = 100;
    public static final int MAX_SEARCH_TIME = 30000;

    private long startTime = 0;
    private long endTime = 0;

    public int nodes = 0;
    public int hits = 0;

    private final TranspositionTable table = new TranspositionTable();

    public Pair<Move, Integer> bestMove(Position p, int timeLeft) {
        table.clear();
        nodes = 0;
        hits = 0;

        int howMuchTimeToTake = timeLeft / 10;
        howMuchTimeToTake = Math.min(MAX_SEARCH_TIME, howMuchTimeToTake);

        this.startTime = System.currentTimeMillis();
        this.endTime = this.startTime + howMuchTimeToTake;
        int score = search(p);
        return Pair.of(table.getMoveForPosition(p), score);
    }

    public int search(Position p) {
        int score = 0;
        for (int depth = 1; depth <= MAX_DEPTH; depth++) {
            score = mtdf(-score, depth, p);

            long now = System.currentTimeMillis();
            System.out.printf("info depth %d score cp %d nodes %d nps %.0f pv %s%n", depth, score, nodes, (double) nodes / (now - this.startTime) * 1000, getPv(p));
            if (now > this.endTime || Math.abs(score) == CHECKMATE_MAX) break;
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
        int upper = CHECKMATE_MAX;
        int lower = -CHECKMATE_MAX;
        int beta = guess;
        int gamma;

        do {
            gamma = alphaBeta(beta - 1, beta, depth, p);

            if (gamma < beta)
                upper = gamma;
            else
                lower = gamma;

            beta = (lower + upper + 1) / 2;
        } while (lower < upper);

        return gamma;
    }

    public int alphaBeta(int alpha, int beta, int depth, Position p) {
        depth = Math.max(0, depth);

        if (p.getScore() <= -CHECKMATE_MIN)
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

        next = p.move(null);
        score = -1 * alphaBeta(-beta, -a, depth - 3, next);

        if (score > gamma)
            gamma = score;

        if (gamma > a)
            a = gamma;

        List<Move> moves = p.generateMoves();
        Move killer = table.getMoveForPosition(p);

        if (killer != null)
            moves.add(0, killer);

        for (Move m : moves) {
            if (gamma >= beta) break;

            next = p.move(m);

            score = -1 * alphaBeta(-beta, -a, depth - 1, next);

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
        if (score <= -CHECKMATE_MIN)
            return -CHECKMATE_MAX;

        if (score >= beta)
            return beta;

        if (alpha < score)
            alpha = score;

        List<Move> moves = p.generateMoves();

        for (Move m : moves) {
            Position next = p.move(m);

            if (!m.isCapture()) continue;

            score = -1 * quiesce(-beta, -alpha, next);

            if (score >= beta)
                return beta;
            if (score > alpha)
                alpha = score;
        }

        return alpha;
    }
}
