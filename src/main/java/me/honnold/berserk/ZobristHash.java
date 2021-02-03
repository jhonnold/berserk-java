package me.honnold.berserk;

import static me.honnold.berserk.BoardUtils.getLSBIndex;
import static me.honnold.berserk.BoardUtils.popBit;

public class ZobristHash {
    public static final long[][] pieceKeys;
    public static final long[] epKeys;
    public static final long[] castleKeys;
    public static final long sideKey;

    static {
        pieceKeys = new long[12][64];
        for (int i = 0; i < 12; i++)
            for (int j = 0; j < 64; j++)
                pieceKeys[i][j] = Random.getRandomLong();

        epKeys = new long[64];
        for (int i = 0; i < 64; i++)
            epKeys[i] = Random.getRandomLong();

        castleKeys = new long[16];
        for (int i = 0; i < 16; i++)
            castleKeys[i] = Random.getRandomLong();

        sideKey = Random.getRandomLong();
    }

    public static long generate(Position p) {
        long key = 0, bb;

        for (int piece = 0; piece < 12; piece++) {
            bb = p.pieceBitboards[piece];

            while (bb != 0) {
                int sq = getLSBIndex(bb);
                key ^= pieceKeys[piece][sq];

                bb = popBit(bb, sq);
            }
        }

        if (p.epSquare >= 0)
            key ^= epKeys[p.epSquare];

        key ^= castleKeys[p.castling];

        if (p.sideToMove == 1)
            key ^= sideKey;

        return key;
    }

    public static long generatePawn(Position p) {
        long key = 0;

        long whitePawns = p.pieceBitboards[0];
        while (whitePawns != 0) {
            int sq = getLSBIndex(whitePawns);
            key ^= pieceKeys[0][sq];
            whitePawns = popBit(whitePawns, sq);
        }

        long blackPawns = p.pieceBitboards[1];
        while (blackPawns != 0) {
            int sq = getLSBIndex(blackPawns);
            key ^= pieceKeys[1][sq];
            blackPawns = popBit(blackPawns, sq);
        }

        return key;
    }
}
