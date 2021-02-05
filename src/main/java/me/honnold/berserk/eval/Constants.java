package me.honnold.berserk.eval;

import me.honnold.berserk.board.Piece;

public class Constants {
    public static final int CHECKMATE_MAX = Piece.baseValues[5] + 10 * Piece.baseValues[4];
    public static final int CHECKMATE_MIN = Piece.baseValues[5] - 10 * Piece.baseValues[4];
}
