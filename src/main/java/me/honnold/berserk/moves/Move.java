package me.honnold.berserk.moves;

import me.honnold.berserk.util.BBUtils;

public class Move {
    public static final int START_MASK = 0x3F;
    public static final int END_MASK = 0xFC0;
    public static final int PIECE_MASK = 0xF000;
    public static final int PROMOTION_MASK = 0xF0000;

    private int data;

    public Move(
            int start,
            int end,
            int pieceIdx,
            int promotionPiece,
            boolean capture,
            boolean doublePush,
            boolean epCapture,
            boolean castle) {
        this.data = start;
        this.data |= (end << 6);
        this.data |= (pieceIdx << 12);
        this.data |= (promotionPiece << 16);

        if (capture) this.data |= (1 << 20);
        if (doublePush) this.data |= (1 << 21);
        if (epCapture) this.data |= (1 << 22);
        if (castle) this.data |= (1 << 23);
    }

    public int getStart() {
        return this.data & START_MASK;
    }

    public int getEnd() {
        return (this.data & END_MASK) >> 6;
    }

    public int getPieceIdx() {
        return (this.data & PIECE_MASK) >> 12;
    }

    public int getPromotionPiece() {
        return (this.data & PROMOTION_MASK) >> 16;
    }

    public boolean isPromotion() {
        return getPromotionPiece() != 0;
    }

    public boolean isCapture() {
        return (this.data & 0x100000) != 0;
    }

    public boolean isDoublePush() {
        return (this.data & 0x200000) != 0;
    }

    public boolean isEPCapture() {
        return (this.data & 0x400000) != 0;
    }

    public boolean isCastle() {
        return (this.data & 0x800000) != 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Move)) return false;

        Move other = (Move) o;

        return other.data == this.data;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(BBUtils.squares[getStart()]).append(BBUtils.squares[getEnd()]);

        if (isPromotion())
            sb.append(Character.toLowerCase(BBUtils.pieceSymbols[getPromotionPiece()]));

        return sb.toString();
    }
}
