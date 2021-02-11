package me.honnold.berserk.tt;

import me.honnold.berserk.board.Position;
import me.honnold.berserk.eval.Constants;

import java.util.Arrays;

public class Transpositions {
    public static final int LOWER = 0;
    public static final int UPPER = 1;
    public static final int EXACT = 2;
    private static final Transpositions singleton = new Transpositions();
    public final int hashsize;
    // 32 bits = score TODO: Make this smaller so depth can go up
    // 2 bits = flag
    // 6 bits = depth
    // 24 bits = move
    private final int power = 18;
    private final int shifts = 64 - power;
    private final long[] transpositions;
    private final int bucketSize = 4;
    public int collisions = 0;
    public int hashes;

    private Transpositions() {
        this.hashsize = (int) ((1L << power) * bucketSize);

        transpositions = new long[this.hashsize * 2];
    }

    public static Transpositions getInstance() {
        return singleton;
    }

    public static int getScore(long value, int ply) {
        int score = (int) (value >>> 32);

        if (score > Constants.CHECKMATE_MIN) {
            score -= ply;
        } else if (score < -Constants.CHECKMATE_MIN) {
            score += ply;
        }

        return score;
    }

    public static int getFlag(long value) {
        return (int) ((value & 0xC0000000) >>> 30);
    }

    public static int getMove(long value) {
        return (int) (value & 0xFFFFFF);
    }

    public void clearEvaluations() {
        collisions = 0;
        hashes = 0;
        Arrays.fill(transpositions, 0);
    }

    public long getEvaluationForPosition(Position p) {
        int idx = getTableIndex(p.zHash);

        for (int i = idx; i < idx + bucketSize * 2; i += 2) {
            long hash = transpositions[i];
            if (hash == p.zHash) return transpositions[i + 1];
        }

        return 0;
    }

    private int getTableIndex(long hash) {
        return (int) (hash >>> shifts) << 1;
    }

    public void putEvaluationForPosition(Position p, int depth, int score, int flag, int move) {
        final int idx = getTableIndex(p.zHash);
        int replacedDepth = Integer.MAX_VALUE;
        int replaceIdx = idx;
        for (int i = idx; i < idx + bucketSize * 2; i += 2) {
            long hash = transpositions[i];
            if (hash == 0) {
                replaceIdx = i;
                break;
            }

            int evalDepth = getDepth(transpositions[i + 1]);
            if (transpositions[i] == p.zHash) {
                if (evalDepth > depth && flag != EXACT) return;

                replaceIdx = i;
                break;
            }

            if (evalDepth < replacedDepth) {
                replaceIdx = i;
                replacedDepth = evalDepth;
            }
        }

        if (transpositions[replaceIdx] != p.zHash) {
            collisions++;
        } else {
            hashes++;
        }

        transpositions[replaceIdx] = p.zHash;
        transpositions[replaceIdx + 1] = createValue(depth, score, flag, move);
    }

    public static int getDepth(long value) {
        return (int) ((value & 0x3F000000) >>> 24);
    }

    public long createValue(int score, int flag, int depth, int move) {
        long result = 0;
        result |= ((long) score << 32);
        result |= ((long) flag << 30);
        result |= ((long) depth << 24);
        return result | ((long) move);
    }
}
