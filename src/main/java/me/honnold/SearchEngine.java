package me.honnold;

import me.honnold.position.Move;
import me.honnold.position.Position;
import me.honnold.tt.ZobristHash;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchEngine {
    private static final int CHECKMATE_MIN = 50710;
    private static final int CHECKMATE_MAX = 69290;

    public int nodes = 0;
    public int hits = 0;

    public final Map<Long, Triple<Integer, Integer, Integer>> evals = new HashMap<>();
    public final Map<Long, Move> moves = new HashMap<>();

    public Pair<Move, Integer> bestMove(Position p) {
        evals.clear();
        moves.clear();

        long posHash = ZobristHash.hash(p);
        int score = abMemory(1, -CHECKMATE_MAX, CHECKMATE_MIN, 6, p);

        return Pair.of(moves.get(posHash), score);
    }

    public int abMemory(int type, int alpha, int beta, int depth, Position p) {
        nodes++;

        long posHash = ZobristHash.hash(p);
        Triple<Integer, Integer, Integer> previousEval = evals.get(posHash);
        if (previousEval != null) {
            int evalDepth = previousEval.getLeft();
            if (evalDepth >= depth) {
                hits++;

                if (previousEval.getMiddle() >= beta) return previousEval.getMiddle();
                if (previousEval.getRight() <= alpha) return previousEval.getRight();

                alpha = Math.max(alpha, previousEval.getMiddle());
                beta = Math.min(beta, previousEval.getRight());
            }
        }

        if (depth == 0) return quiesce(alpha, beta, p);

        int gamma = type == 1 ? -CHECKMATE_MAX : CHECKMATE_MAX;
        if (type == 1) { // Maximize
            int a = alpha;
            
            for (Move m : p.generateMoves()) {
                if (gamma >= beta) break;

                Position next = p.move(m);

                int score = abMemory(0, a, beta, depth - 1, next);

                if (score > gamma)
                    gamma = score;

                if (gamma > a) {
                    a = gamma;
                    moves.put(posHash, m);
                }
            }
        } else if (type == 0) { // Minimize
            int b = beta;

            for (Move m : p.generateMoves()) {
                if (gamma <= alpha) break;

                Position next = p.move(m);

                int score = abMemory(1, alpha, b, depth - 1, next);

                if (score < gamma)
                    gamma = score;

                if (gamma < b) {
                    b = gamma;
                    moves.put(posHash, m);
                }
            }
        }

        if (gamma <= alpha) {
            evals.put(posHash, Triple.of(depth, -CHECKMATE_MAX, gamma));
        } else if (gamma < beta) {
            evals.put(posHash, Triple.of(depth, gamma, gamma));
        } else  {
            evals.put(posHash, Triple.of(depth, gamma, CHECKMATE_MAX));
        }

        return gamma;
    }

    private int quiesce(int a, int b, Position position) {
        nodes++;

        int score = position.getScore();

        if (score >= b)
            return b;
        if (score > a)
            a = score;

        List<Move> moves = position.generateMoves();

        for (Move m : moves) {
            if (!m.isCapture()) continue;

            Position newPosition = position.move(m);
            score = -1 * quiesce(-b, -a, newPosition);

            if (score >= b)
                return b;

            if (score > a)
                a = score;
        }

        return a;
    }
}
