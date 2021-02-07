package me.honnold.berserk.tt;

import java.util.Arrays;
import me.honnold.berserk.board.Position;
import me.honnold.berserk.moves.Move;

public class Transpositions {
    private static final Transpositions singleton =
            new Transpositions(10_000_000);
    private final Evaluation[] evaluations;
    private final int tableSize;

    private Transpositions(int tableSize) {
        this.tableSize = tableSize;
        this.evaluations = new Evaluation[this.tableSize];
    }

    public static Transpositions getInstance() {
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

    public void putEvaluationForPosition(Position p, int depth, int score, EvaluationFlag flag, Move move) {
        int idx = getTableIndex(p.zHash);

        Evaluation evaluation = new Evaluation(p.zHash, depth, score, flag, move);
        evaluations[idx] = evaluation;
    }
}
