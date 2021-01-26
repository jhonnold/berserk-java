package me.honnold.tt;

import me.honnold.position.Move;
import me.honnold.position.Position;

import java.util.Arrays;

public class TranspositionTable {
    private static final int TABLE_SIZE = 50000000;

    private final Evaluation[] evaluations = new Evaluation[TABLE_SIZE];
    private final MoveEntry[] moves = new MoveEntry[TABLE_SIZE];

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
    }

    private int getTableIndex(long hash) {
        int modulo = (int) (hash % TABLE_SIZE);

        return modulo < 0 ? modulo + TABLE_SIZE : modulo;
    }

    public Evaluation getEvaluationForPosition(Position p) {
        long hash = ZobristHash.hash(p);
        int idx = getTableIndex(hash);

        Evaluation evaluation = evaluations[idx];
        if (evaluation == null || evaluation.hash != hash) return null;

        return evaluation;
    }

    public void putEvaluationForPosition(Position p, int depth, int alpha, int beta) {
        long hash = ZobristHash.hash(p);
        int idx = getTableIndex(hash);

        Evaluation evaluation = new Evaluation(hash, depth, alpha, beta);
        evaluations[idx] = evaluation;
    }

    public Move getMoveForPosition(Position p) {
        long hash = ZobristHash.hash(p);
        int idx = getTableIndex(hash);

        MoveEntry entry = moves[idx];

        if (entry == null || entry.hash != hash) return null;

        return entry.move;
    }

    public synchronized void putMoveForPosition(Position p, Move m) {
        long hash = ZobristHash.hash(p);
        int idx = getTableIndex(hash);

        moves[idx] = new MoveEntry(hash, m);
    }
}
