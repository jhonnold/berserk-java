package me.honnold.bitboard;

public class Move {
    public int start;
    public int end;
    public int pieceIdx;
    public int promotionPiece;
    public boolean capture;
    public boolean doublePush;
    public boolean epCapture;
    public boolean castle;

    public Move(int start, int end, int pieceIdx, int promotionPiece, boolean capture, boolean doublePush, boolean epCapture, boolean castle) {
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
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Position.squares[start]).append(Position.squares[end]);

        if (promotionPiece >= 0)
            sb.append(Character.toLowerCase(Position.pieceSymbols[promotionPiece]));

        return sb.toString();
    }
}
