package me.honnold.berserk.tt;

import java.util.Arrays;
import me.honnold.berserk.board.Position;
import me.honnold.berserk.moves.Move;

public class Transpositions {
    private static final Transpositions singleton = new Transpositions();

    private final int power = 20;
    private final int shifts = 64 - power;
    private final Evaluation[] evaluations;
    private final int bucketSize = 4;

    private Transpositions() {
        evaluations = new Evaluation[(int) ((1L << power) * bucketSize)];
    }

    public static Transpositions getInstance() {
        return singleton;
    }

    public void clearEvaluations() {
        Arrays.fill(evaluations, null);
    }

    public Evaluation getEvaluationForPosition(Position p) {
        int idx = getTableIndex(p.zHash);

        for (int i = idx; i < idx + bucketSize; i++) {
            Evaluation eval = evaluations[i];
            if (eval == null) continue;

            if (eval.getHash() == p.zHash) return eval;
        }

        return null;
    }

    private int getTableIndex(long hash) {
        return (int) (hash >>> shifts) << 1;
    }

    public void putEvaluationForPosition(
            Position p, int depth, int score, EvaluationFlag flag, Move move) {
        final int idx = getTableIndex(p.zHash);
        int replacedDepth = Integer.MAX_VALUE;
        int replaceIdx = idx;
        for (int i = idx; i < idx + bucketSize; i++) {
            Evaluation eval = evaluations[i];
            if (eval == null) {
                replaceIdx = i;
                break;
            }

            int evalDepth = eval.getDepth();
            if (eval.getHash() == p.zHash) {
                if (evalDepth > depth && flag != EvaluationFlag.EXACT) return;

                replaceIdx = i;
                break;
            }

            if (evalDepth < replacedDepth) {
                replaceIdx = i;
                replacedDepth = evalDepth;
            }
        }

        evaluations[replaceIdx] = new Evaluation(p.zHash, depth, score, flag, move);
    }
}
