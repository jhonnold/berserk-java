package me.honnold.berserk.eval;

public class Constants {
    public static final int CHECKMATE_MAX =
            Piece.getPieceValue(10, 0) + 10 * Piece.getPieceValue(8, 0);
    public static final int CHECKMATE_MIN =
            Piece.getPieceValue(10, 0) - 10 * Piece.getPieceValue(8, 0);
}
