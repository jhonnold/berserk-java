package me.honnold.berserk.moves;

import me.honnold.berserk.util.BBUtils;

public class Move {
    public static final int START_MASK = 0x3F;
    public static final int END_MASK = 0xFC0;
    public static final int PIECE_MASK = 0xF000;
    public static final int PROMOTION_MASK = 0xF0000;

    public static int createMove(
            int start,
            int end,
            int pieceIdx,
            int promotionPiece,
            boolean capture,
            boolean doublePush,
            boolean epCapture,
            boolean castle) {
        int data = start;
        data |= (end << 6);
        data |= (pieceIdx << 12);
        data |= (promotionPiece << 16);

        if (capture) data |= (1 << 20);
        if (doublePush) data |= (1 << 21);
        if (epCapture) data |= (1 << 22);
        if (castle) data |= (1 << 23);

        return data;
    }

    public static boolean equals(int m1, int m2) {
        return getStart(m1) == getStart(m2)
                && getEnd(m1) == getEnd(m2)
                && getPromotionPiece(m1) == getPromotionPiece(m2);
    }

    public static int getPieceIdx(int data) {
        return (data & PIECE_MASK) >> 12;
    }

    public static boolean isCapture(int data) {
        return (data & 0x100000) != 0;
    }

    public static boolean isDoublePush(int data) {
        return (data & 0x200000) != 0;
    }

    public static boolean isEPCapture(int data) {
        return (data & 0x400000) != 0;
    }

    public static boolean isCastle(int data) {
        return (data & 0x800000) != 0;
    }

    public static String toString(int data) {
        StringBuilder sb = new StringBuilder();
        sb.append(BBUtils.squares[getStart(data)]).append(BBUtils.squares[getEnd(data)]);

        if (isPromotion(data))
            sb.append(Character.toLowerCase(BBUtils.pieceSymbols[getPromotionPiece(data)]));

        return sb.toString();
    }

    public static int getStart(int data) {
        return data & START_MASK;
    }

    public static int getEnd(int data) {
        return (data & END_MASK) >> 6;
    }

    public static boolean isPromotion(int data) {
        return getPromotionPiece(data) != 0;
    }

    public static int getPromotionPiece(int data) {
        return (data & PROMOTION_MASK) >> 16;
    }
}
