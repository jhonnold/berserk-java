package me.honnold.berserk.search;

import java.util.Arrays;
import java.util.Stack;

public class Repetitions {
    private static final Repetitions singleton = new Repetitions();
    public static final int TABLE_SIZE = 1024;

    private final Stack<Long> positions = new Stack<>();
    private final long[] counts = new long[1024];
    private final long[] hashes = new long[1024];

    private Repetitions() {}

    public static Repetitions getInstance() {
        return singleton;
    }

    public int getIdx(long zhash) {
        int idx = (int) (zhash % TABLE_SIZE);
        if (idx < 0) idx += TABLE_SIZE;

        if (hashes[idx] == 0 || hashes[idx] == zhash) return idx;

        while (hashes[idx] != 0 && hashes[idx] != zhash) {
            idx++;
            if (idx >= TABLE_SIZE) idx = 0;
        }

        return idx;
    }

    public void clearPreviousPositions() {
        Arrays.fill(hashes, 0);
        Arrays.fill(counts, 0);
    }

    public void add(long zhash) {
        positions.add(zhash);
        int idx = this.getIdx(zhash);

        counts[idx]++;
        hashes[idx] = zhash;
    }

    public void pop() {
        long zhash = positions.pop();
        int idx = this.getIdx(zhash);

        counts[idx]--;
        if (counts[idx] <= 0) hashes[idx] = 0;
    }

    public boolean isRepetition() {
        for (int i = 0; i < TABLE_SIZE; i++) {
            if (counts[i] > 1) return true;
        }

        return false;
    }
}
