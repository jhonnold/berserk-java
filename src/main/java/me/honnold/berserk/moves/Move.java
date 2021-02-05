package me.honnold.berserk.moves;

import me.honnold.berserk.board.Position;

public class Move {
    public int start;
    public int end;
    public int pieceIdx;
    public int promotionPiece;
    public boolean capture;
    public boolean doublePush;
    public boolean epCapture;
    public boolean castle;

    public Move(
            int start,
            int end,
            int pieceIdx,
            int promotionPiece,
            boolean capture,
            boolean doublePush,
            boolean epCapture,
            boolean castle) {
        this.start = start;
        this.end = end;
        this.pieceIdx = pieceIdx;
        this.promotionPiece = promotionPiece;
        this.capture = capture;
        this.doublePush = doublePush;
        this.epCapture = epCapture;
        this.castle = castle;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Move)) return false;

        Move other = (Move) o;

        return other.start == start
                && other.end == end
                && other.pieceIdx == pieceIdx
                && other.promotionPiece == promotionPiece
                && other.capture == capture
                && other.doublePush == doublePush
                && other.epCapture == epCapture
                && other.castle == castle;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Position.squares[start]).append(Position.squares[end]);

        if (promotionPiece >= 0)
            sb.append(Character.toLowerCase(Position.pieceSymbols[promotionPiece]));

        return sb.toString();
    }
}
