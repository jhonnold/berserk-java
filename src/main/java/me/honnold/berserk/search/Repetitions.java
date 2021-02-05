package me.honnold.berserk.search;

import me.honnold.berserk.board.Position;

public class Repetitions {
    private static final Repetitions singleton = new Repetitions();

    private final long[] positions = new long[1024];
    private int idx = 0;

    private Repetitions() {}

    public static Repetitions getInstance() {
        return singleton;
    }

    public void clearPreviousPositions() {
        this.idx = 0;
    }

    public void add(long position) {
        this.positions[idx++] = position;
    }

    public void pop() {
        this.idx--;
    }

    public boolean isRepetition(Position position) {
        int positionSeenCount = 1;
        for (int i = 0; i < idx; i++) {
            if (positions[i] == position.zHash) positionSeenCount++;

            if (positionSeenCount > 2) return true;
        }

        return false;
    }
}
