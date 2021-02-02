package me.honnold.berserk;

import java.util.Arrays;

public class Repetitions {
    private int idx = 0;
    private final long[] positions = new long[1024];

    private Repetitions() {
    }

    public void clear() {
        idx = 0;
        Arrays.fill(positions, 0);
    }

    public void setCurrentPosition(long position) {
        this.positions[idx++] = position;
    }

    public void decrement() {
        this.idx--;
    }

    public boolean isRepetition(Position p) {
        boolean foundOnce = false;
        for (int i = 0; i < idx; i++) {
            if (foundOnce && positions[i] == p.zHash) return true;
            else if (positions[i] == p.zHash) foundOnce = true;
        }

        return false;
    }

    public static Repetitions getInstance() {
        return new Repetitions();
    }
}
