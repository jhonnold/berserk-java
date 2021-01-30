package me.honnold.ll;

import java.util.*;

public class ZobristHash {
    private final static Map<Integer, long[]> pieceHashPerSquare;
    private final static long blackToMove;
    private final static long whiteCanCastleKingside;
    private final static long whiteCanCastleQueenside;
    private final static long blackCanCastleQueenside;
    private final static long blackCanCastleKingside;
    private final static long[] epFiles;

    static {
        pieceHashPerSquare = new HashMap<>();

        Random random = new Random(0);

        List<Integer> pieceTypes =
                Arrays.asList(Piece.KING, Piece.QUEEN, Piece.ROOK, Piece.BISHOP, Piece.KNIGHT, Piece.PAWN);

        for (Integer p : pieceTypes) {
            long[] hashes = new long[64];
            for (int i = 0; i < 64; i++)
                hashes[i] = random.nextLong();

            pieceHashPerSquare.put(p, hashes);
        }

        blackToMove = random.nextLong();
        whiteCanCastleKingside = random.nextLong();
        whiteCanCastleQueenside = random.nextLong();
        blackCanCastleQueenside = random.nextLong();
        blackCanCastleKingside = random.nextLong();

        epFiles = new long[8];
        for (int i = 0; i < 8; i++) epFiles[i] = random.nextLong();
    }

    public static long hash(Board b) {
        long result = 1;

        for (Integer i : b.pieceLocations) {
            if (i < 0) continue;

            int piece = b.pieces[i];
            result ^= pieceHashPerSquare.get(piece)[i];
        }

        if (b.moving == Piece.BLACK) result ^= blackToMove;

        if ((b.castling & 0x8) == 8) result ^= whiteCanCastleKingside;
        if ((b.castling & 0x4) == 4) result ^= whiteCanCastleQueenside;
        if ((b.castling & 0x2) == 2) result ^= blackCanCastleKingside;
        if ((b.castling & 0x1) == 1) result ^= blackCanCastleQueenside;

        if (b.epSquare != -1)
            result ^= epFiles[Board.mailbox64[b.epSquare] % 10 - 1];

        return result;
    }
}
