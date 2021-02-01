package me.honnold.bitboard;

import java.util.Arrays;

public class Repetitions {
    public static int idx = 0;
    public static long[] positions = new long[1024];

    public static void clear() {
        idx = 0;
        Arrays.fill(positions, 0);
    }

    public static boolean isRepetition(Position p) {
        boolean foundOnce = false;
        for (int i = 0; i < idx; i++) {
            if (foundOnce && positions[i] == p.zHash) return true;
            else if (positions[i] == p.zHash) foundOnce = true;
        }

        return false;
    }
}
