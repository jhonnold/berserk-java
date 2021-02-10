package me.honnold.berserk.search;

public class Repetitions {
    private static final Repetitions singleton = new Repetitions();

    private final long[] hashes = new long[1024];
    private int idx = 0;

    private Repetitions() {}

    public static Repetitions getInstance() {
        return singleton;
    }

    public void clearPreviousPositions() {
        idx = 0;
    }

    public void add(long zhash) {
        hashes[idx++] = zhash;
    }

    public void pop() {
        idx--;
    }

    public boolean isRepetition(long zHash) {
        for (int i = 0; i < idx; i++) {
            if (hashes[i] == zHash) return true;
        }

        return false;
    }
}
