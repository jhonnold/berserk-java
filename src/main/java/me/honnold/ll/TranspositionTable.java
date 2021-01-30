package me.honnold.ll;

import me.honnold.position.Move;

import java.util.Arrays;

public class TranspositionTable {
    private static final int TABLE_SIZE = (int) Math.pow(2, 24);

    private final Evaluation[] evaluations = new Evaluation[TABLE_SIZE];
    private final MoveEntry[] moves = new MoveEntry[TABLE_SIZE];
    private final PerftEntry[] moveCounts = new PerftEntry[TABLE_SIZE];

    public static class PerftEntry {
        public final long hash;
        public final long moves;
        public final int depth;

        public PerftEntry(long hash, long moves, int depth) {
            this.hash = hash;
            this.moves = moves;
            this.depth = depth;
        }
    }

    public long getMoveCountForBoard(Board b, int depth) {
        long hash = ZobristHash.hash(b);
        int idx = getTableIndex(hash);

        PerftEntry entry = moveCounts[idx];

        if (entry == null || entry.hash != hash || entry.depth != depth) return -1;

        return entry.moves;
    }

    public void putMoveCountForBoard(Board b, long count, int depth) {
        long hash = ZobristHash.hash(b);
        int idx = getTableIndex(hash);

//        PerftEntry entry = moveCounts[idx];
//        if (entry != null)
//            if (entry.depth >= depth)
//                return;

        moveCounts[idx] = new PerftEntry(hash, count, depth);
    }

    public static class Evaluation {
        public final long hash;
        public final int depth;
        public final int alpha;
        public final int beta;

        public Evaluation(long hash, int depth, int alpha, int beta) {
            this.hash = hash;
            this.depth = depth;
            this.alpha = alpha;
            this.beta = beta;
        }
    }

    public static class MoveEntry {
        public final long hash;
        public final Move move;

        public MoveEntry(long hash, Move move) {
            this.hash = hash;
            this.move = move;
        }
    }

    public void clear() {
        Arrays.fill(evaluations, null);
        Arrays.fill(moves, null);
        Arrays.fill(moveCounts, null);
    }

    private int getTableIndex(long hash) {
        int modulo = (int) (hash % TABLE_SIZE);

        return modulo < 0 ? modulo + TABLE_SIZE : modulo;
    }

    public Evaluation getEvaluationForBoard(Board p) {
        long hash = ZobristHash.hash(p);
        int idx = getTableIndex(hash);

        Evaluation evaluation = evaluations[idx];
        if (evaluation == null || evaluation.hash != hash) return null;

        return evaluation;
    }

    public void putEvaluationForBoard(Board p, int depth, int alpha, int beta) {
        long hash = ZobristHash.hash(p);
        int idx = getTableIndex(hash);

        Evaluation evaluation = new Evaluation(hash, depth, alpha, beta);
        evaluations[idx] = evaluation;
    }

    public Move getMoveForBoard(Board p) {
        long hash = ZobristHash.hash(p);
        int idx = getTableIndex(hash);

        MoveEntry entry = moves[idx];

        if (entry == null || entry.hash != hash) return null;

        return entry.move;
    }

    public void putMoveForBoard(Board p, Move m) {
        long hash = ZobristHash.hash(p);
        int idx = getTableIndex(hash);

        moves[idx] = new MoveEntry(hash, m);
    }
}
