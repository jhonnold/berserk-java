package me.honnold.berserk.tt;

import java.util.Arrays;
import me.honnold.berserk.board.Position;

public class TranspositionEvaluations {
    private static final TranspositionEvaluations singleton =
            new TranspositionEvaluations(10_000_000);
    private final Evaluation[] evaluations;
    private final int tableSize;

    private TranspositionEvaluations(int tableSize) {
        this.tableSize = tableSize;
        this.evaluations = new Evaluation[this.tableSize];
    }

    public static TranspositionEvaluations getInstance() {
        return singleton;
    }

    private int getTableIndex(long hash) {
        int modulo = (int) (hash % tableSize);

        return modulo < 0 ? modulo + tableSize : modulo;
    }

    public void clearEvaluations() {
        Arrays.fill(evaluations, null);
    }

    public Evaluation getEvaluationForPosition(Position p) {
        int idx = getTableIndex(p.zHash);

        Evaluation evaluation = evaluations[idx];
        if (evaluation == null || evaluation.getHash() != p.zHash) return null;

        return evaluation;
    }

    public void putEvaluationForPosition(Position p, int depth, int score, EvaluationFlag flag) {
        int idx = getTableIndex(p.zHash);

        Evaluation evaluation = new Evaluation(p.zHash, depth, score, flag);
        evaluations[idx] = evaluation;
    }
}
