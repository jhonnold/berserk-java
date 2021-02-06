package me.honnold.berserk.eval;

import me.honnold.berserk.board.GameStage;
import me.honnold.berserk.board.Piece;

public class Constants {
    public static final int CHECKMATE_MAX =
            Piece.getPieceValue(10, GameStage.OPENING)
                    + 10 * Piece.getPieceValue(8, GameStage.OPENING);
    public static final int CHECKMATE_MIN =
            Piece.getPieceValue(10, GameStage.OPENING)
                    - 10 * Piece.getPieceValue(8, GameStage.OPENING);
}
