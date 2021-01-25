package me.honnold.tt;

import me.honnold.piece.*;
import me.honnold.position.CastlingRights;
import me.honnold.position.Position;

import java.util.*;

public class ZobristHash {
    private final static Map<Class<? extends Piece>, long[]> pieceHashPerSquare;
    private final static long blackToMove;
    private final static long movingCanCastleEast;
    private final static long movingCanCastleWest;
    private final static long opponentCanCastleEast;
    private final static long opponentCanCastleWest;
    private final static long[] epFiles;

    static {
        pieceHashPerSquare = new HashMap<>();

        Random random = new Random(0);

        List<Class<? extends Piece>> pieceTypes =
                Arrays.asList(King.class, Queen.class, Rook.class, Bishop.class, Knight.class, Pawn.class);

        for (Class<? extends Piece> p : pieceTypes) {
            long[] hashes = new long[64];
            for (int i = 0; i < 64; i++)
                hashes[i] = random.nextLong();

            pieceHashPerSquare.put(p, hashes);
        }

        blackToMove = random.nextLong();
        movingCanCastleEast = random.nextLong();
        movingCanCastleWest = random.nextLong();
        opponentCanCastleEast = random.nextLong();
        opponentCanCastleWest = random.nextLong();

        epFiles = new long[8];
        for (int i = 0; i < 8; i++) epFiles[i] = random.nextLong();
    }

    public static long hash(Position position) {
        long result = 1;

        for (int i = 21, square = 0; i < 98; i++) {
            if (i % 10 == 0 || i % 10 == 9) continue;
            Piece piece = position.getPiece(i);
            if (piece != null)
                result ^= pieceHashPerSquare.get(piece.getClass())[square];

            square++;
        }

        if (position.getMoving() == Color.BLACK) result ^= blackToMove;

        CastlingRights movingCr = position.getMovingCastlingRights();
        CastlingRights oppoCr = position.getOpponentCastlingRights();

        if (movingCr.canEastSide()) result ^= movingCanCastleEast;
        if (movingCr.canWestSide()) result ^= movingCanCastleWest;
        if (oppoCr.canEastSide()) result ^= opponentCanCastleEast;
        if (oppoCr.canWestSide()) result ^= opponentCanCastleWest;

        int epSquare = position.getEpSquare();
        if (epSquare != 0)
            result ^= epFiles[epSquare % 10 - 1];

        return result;
    }
}
