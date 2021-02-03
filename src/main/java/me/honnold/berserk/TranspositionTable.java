package me.honnold.berserk;

import java.util.Arrays;

public class TranspositionTable {
    private static final int TABLE_SIZE = 10000000;

    private final Evaluation[] evaluations = new Evaluation[TABLE_SIZE];
    private final MoveEntry[] moves = new MoveEntry[TABLE_SIZE];

    private TranspositionTable() {
    }

    public static TranspositionTable getInstance() {
        return new TranspositionTable();
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
        int idx = getTableIndex(p.zHash);

        Evaluation evaluation = evaluations[idx];
        if (evaluation == null || evaluation.hash != p.zHash) return null;

        return evaluation;
    }

    public void putEvaluationForPosition(Position p, int depth, int alpha, int beta) {
        int idx = getTableIndex(p.zHash);

        Evaluation evaluation = new Evaluation(p.zHash, depth, alpha, beta);
        evaluations[idx] = evaluation;
    }

    public Move getMoveForPosition(Position p) {
        int idx = getTableIndex(p.zHash);

        MoveEntry entry = moves[idx];

        if (entry == null || entry.hash != p.zHash) return null;

        return entry.move;
    }

    public void putMoveForPosition(Position p, Move m) {
        int idx = getTableIndex(p.zHash);

        moves[idx] = new MoveEntry(p.zHash, m);
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
}
