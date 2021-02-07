package me.honnold.berserk.util;

public class BBUtils {
    public static final String[] squares = {
        "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8",
        "a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7",
        "a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6",
        "a5", "b5", "c5", "d5", "e5", "f5", "g5", "h5",
        "a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4",
        "a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3",
        "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2",
        "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1",
    };
    public static final int[] castlingRights = {
        14, 15, 15, 15, 12, 15, 15, 13,
        15, 15, 15, 15, 15, 15, 15, 15,
        15, 15, 15, 15, 15, 15, 15, 15,
        15, 15, 15, 15, 15, 15, 15, 15,
        15, 15, 15, 15, 15, 15, 15, 15,
        15, 15, 15, 15, 15, 15, 15, 15,
        15, 15, 15, 15, 15, 15, 15, 15,
        11, 15, 15, 15, 3, 15, 15, 7
    };
    public static final char[] pieceSymbols = {
        'P', 'p', 'N', 'n', 'B', 'b', 'R', 'r', 'Q', 'q', 'K', 'k'
    };
    public static int[] pawnDirections = {-8, 8};

    public static boolean getBit(long bitboard, int bit) {
        return (bitboard & (1L << bit)) != 0;
    }

    public static long setBit(long bitboard, int bit) {
        return (bitboard | (1L << bit));
    }

    public static long popBit(long bitboard, int bit) {
        return getBit(bitboard, bit) ? (bitboard ^ (1L << bit)) : bitboard;
    }

    public static long popLSB(long bitboard) {
        return bitboard & (bitboard - 1);
    }

    public static int countBits(long bitboard) {
        return Long.bitCount(bitboard);
    }

    public static int getLSBIndex(long bitboard) {
        return Long.numberOfTrailingZeros(bitboard);
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
