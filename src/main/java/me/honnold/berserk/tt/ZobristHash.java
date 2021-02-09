package me.honnold.berserk.tt;

import me.honnold.berserk.board.Position;
import me.honnold.berserk.util.Random;

import static me.honnold.berserk.util.BBUtils.getLSBIndex;
import static me.honnold.berserk.util.BBUtils.popBit;

public class ZobristHash {
    private static final ZobristHash singleton = new ZobristHash();
    private final long[][] pieceKeys;
    private final long[] epKeys;
    private final long[] castleKeys;
    private final long sideKey;

    private ZobristHash() {
        pieceKeys = new long[12][64];
        Random random = Random.getInstance();
        for (int i = 0; i < 12; i++)
            for (int j = 0; j < 64; j++) pieceKeys[i][j] = random.getRandomLong();

        epKeys = new long[64];
        for (int i = 0; i < 64; i++) epKeys[i] = random.getRandomLong();

        castleKeys = new long[16];
        for (int i = 0; i < 16; i++) castleKeys[i] = random.getRandomLong();

        sideKey = random.getRandomLong();
    }

    public static ZobristHash getInstance() {
        return singleton;
    }

    public long getZobristHash(Position p) {
        long key = 0, bb;

        for (int piece = 0; piece < 12; piece++) {
            bb = p.pieceBitboards[piece];

            while (bb != 0) {
                int sq = getLSBIndex(bb);
                key ^= pieceKeys[piece][sq];

                bb = popBit(bb, sq);
            }
        }

        if (p.epSquare >= 0) key ^= epKeys[p.epSquare];

        key ^= castleKeys[p.castling];

        if (p.sideToMove == 1) key ^= sideKey;

        return key;
    }

    public long getSideKey() {
        return sideKey;
    }

    public long getPieceKey(int piece, int square) {
        return this.pieceKeys[piece][square];
    }

    public long getEpKey(int square) {
        return this.epKeys[square];
    }

    public long getCastleKey(int castleStatus) {
        return this.castleKeys[castleStatus];
    }
}
