package me.honnold.berserk;

public class BoardUtils {
    public static boolean getBit(long bitboard, int bit) {
        return (bitboard & (1L << bit)) != 0;
    }

    public static long setBit(long bitboard, int bit) {
        return (bitboard | (1L << bit));
    }

    public static long popBit(long bitboard, int bit) {
        return getBit(bitboard, bit) ?
                (bitboard ^ (1L << bit)) :
                bitboard;
    }

    public static int countBits(long bitboard) {
        int count = 0;
        while (bitboard != 0) {
            count++;
            bitboard &= bitboard - 1;
        }
        return count;
    }

    public static int getLSBIndex(long bitboard) {
        return bitboard != 0 ?
                countBits(((~bitboard + 1) & bitboard) - 1) :
                -1;
    }

    public static String bitBoardToString(long bitboard) {
        StringBuilder builder = new StringBuilder();

        for (int r = 0; r < 8; r++) {
            for (int f = 0; f < 8; f++) {
                int square = r * 8 + f;

                if (f == 0) builder.append(String.format(" %d ", 8 - r));
                builder.append(String.format(" %d", (bitboard & (1L << square)) != 0 ? 1 : 0));
            }

            builder.append("\n");
        }
        builder.append("\n    a b c d e f g h\n\nValue: ").append(bitboard).append("\n\n");

        return builder.toString();
    }
}
