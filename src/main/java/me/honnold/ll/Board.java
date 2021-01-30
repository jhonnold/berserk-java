package me.honnold.ll;

public class Board {
    public static final int[] mailbox = {
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, 0, 1, 2, 3, 4, 5, 6, 7, -1,
            -1, 8, 9, 10, 11, 12, 13, 14, 15, -1,
            -1, 16, 17, 18, 19, 20, 21, 22, 23, -1,
            -1, 24, 25, 26, 27, 28, 29, 30, 31, -1,
            -1, 32, 33, 34, 35, 36, 37, 38, 39, -1,
            -1, 40, 41, 42, 43, 44, 45, 46, 47, -1,
            -1, 48, 49, 50, 51, 52, 53, 54, 55, -1,
            -1, 56, 57, 58, 59, 60, 61, 62, 63, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1
    };

    public static final int[] mailbox64 = {
            21, 22, 23, 24, 25, 26, 27, 28,
            31, 32, 33, 34, 35, 36, 37, 38,
            41, 42, 43, 44, 45, 46, 47, 48,
            51, 52, 53, 54, 55, 56, 57, 58,
            61, 62, 63, 64, 65, 66, 67, 68,
            71, 72, 73, 74, 75, 76, 77, 78,
            81, 82, 83, 84, 85, 86, 87, 88,
            91, 92, 93, 94, 95, 96, 97, 98
    };

    public static final int[] initialPieces = {
            3, 1, 2, 4, 5, 2, 1, 3,
            0, 0, 0, 0, 0, 0, 0, 0,
            -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1,
            0, 0, 0, 0, 0, 0, 0, 0,
            3, 1, 2, 4, 5, 2, 1, 3
    };

    public static final int[] initialColors = {
            1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1,
            -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1,
            0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0
    };

    public int numPieces = 32;
    public int[] pieceLocations;
    public int[] pieces;
    public int[] colors;
    public int[] kings;

    public int epSquare = 0;
    public int castling = 0xF; // KQkq
    public int moving = Piece.WHITE;

    public Board() {
        pieces = new int[64];
        System.arraycopy(initialPieces, 0, pieces, 0, 64);

        colors = new int[64];
        System.arraycopy(initialColors, 0, colors, 0, 64);

        pieceLocations = new int[]{
                0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
                48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63
        };

        kings = new int[]{60, 12};
    }

    // moves
    // [start end captured flags epSquare castling]
    public int[][] generateMoves() {
        int numMoves = 0;
        int[][] moves = new int[128][6];

        int i = 0, startSquare;
        while (i < numPieces && (startSquare = pieceLocations[i++]) != -1) {
            if (colors[startSquare] != moving) continue;

            int p = pieces[startSquare];
            if (p != Piece.PAWN) {
                for (int j = 0; j < Piece.numDirection[p]; ++j) {
                    for (int square = startSquare; ; ) {
                        square = mailbox[mailbox64[square] + Piece.directions[p][j]];

                        // square isn't on board or it's our color
                        if (square == -1 || colors[square] == moving) break;

                        // generate move, 1 means capture, 0 means quiet
                        // TODO: checks?
                        moves[numMoves++] = new int[]{startSquare, square, pieces[square], (colors[square] == 1 - moving ? Move.CAPTURE : Move.NORMAL), this.epSquare, this.castling};

                        // If we captured or this piece isn't ray stop generating
                        if (colors[square] != Piece.EMPTY || !Piece.rays[p]) break;
                    }
                }
            } else {
                if (this.moving == Piece.WHITE) {
                    int square = mailbox[mailbox64[startSquare] + Piece.N];
                    if (colors[square] == Piece.EMPTY)
                        moves[numMoves++] = new int[]{startSquare, square, pieces[square], Move.NORMAL, this.epSquare, this.castling};

                    square = mailbox[mailbox64[startSquare] + Piece.N + Piece.W];
                    if (square != -1 && (colors[square] == 1 - moving || square == epSquare))
                        moves[numMoves++] = new int[]{startSquare, square, pieces[square], (square == epSquare ? Move.EPCAPTURE : Move.CAPTURE), this.epSquare, this.castling};

                    square = mailbox[mailbox64[startSquare] + Piece.N + Piece.E];
                    if (square != -1 && (colors[square] == 1 - moving || square == epSquare))
                        moves[numMoves++] = new int[]{startSquare, square, pieces[square], (square == epSquare ? Move.EPCAPTURE : Move.CAPTURE), this.epSquare, this.castling};

                    if (startSquare >= 48 && startSquare <= 55) {
                        int skip = mailbox[mailbox64[startSquare] + Piece.N];
                        square = mailbox[mailbox64[skip] + Piece.N];

                        if (colors[square] == Piece.EMPTY && colors[skip] == Piece.EMPTY)
                            moves[numMoves++] = new int[]{startSquare, square, pieces[square], Move.EP, this.epSquare, this.castling};
                    }
                } else {
                    int square = mailbox[mailbox64[startSquare] + Piece.S];
                    if (colors[square] == Piece.EMPTY)
                        moves[numMoves++] = new int[]{startSquare, square, pieces[square], Move.NORMAL, this.epSquare, this.castling};

                    square = mailbox[mailbox64[startSquare] + Piece.S + Piece.W];
                    if (square != -1 && (colors[square] == 1 - moving || square == epSquare))
                        moves[numMoves++] = new int[]{startSquare, square, pieces[square], (square == epSquare ? Move.EPCAPTURE : Move.CAPTURE), this.epSquare, this.castling};

                    square = mailbox[mailbox64[startSquare] + Piece.S + Piece.E];
                    if (square != -1 && (colors[square] == 1 - moving || square == epSquare))
                        moves[numMoves++] = new int[]{startSquare, square, pieces[square], (square == epSquare ? Move.EPCAPTURE : Move.CAPTURE), this.epSquare, this.castling};

                    if (startSquare >= 8 && startSquare <= 15) {
                        int skip = mailbox[mailbox64[startSquare] + Piece.S];
                        square = mailbox[mailbox64[skip] + Piece.S];

                        if (colors[square] == Piece.EMPTY && colors[skip] == Piece.EMPTY)
                            moves[numMoves++] = new int[]{startSquare, square, pieces[square], Move.EP, this.epSquare, this.castling};
                    }
                }
            }
        }

        if (this.moving == Piece.WHITE &&
                (this.castling & 0x8) == 8 &&
                this.pieces[61] == Piece.EMPTY &&
                this.pieces[62] == Piece.EMPTY &&
                !this.isSquareAttackedBy(60, 1 - this.moving) &&
                !this.isSquareAttackedBy(61, 1 - this.moving) &&
                !this.isSquareAttackedBy(62, 1 - this.moving)
        )
            moves[numMoves++] = new int[]{60, 62, -1, Move.CASTLE, this.epSquare, this.castling};

        if (this.moving == Piece.WHITE &&
                (this.castling & 0x4) == 4 &&
                this.pieces[57] == Piece.EMPTY &&
                this.pieces[58] == Piece.EMPTY &&
                this.pieces[59] == Piece.EMPTY &&
                !this.isSquareAttackedBy(60, 1) &&
                !this.isSquareAttackedBy(59, 1) &&
                !this.isSquareAttackedBy(58, 1)
        )
            moves[numMoves++] = new int[]{60, 58, -1, Move.CASTLE, this.epSquare, this.castling};

        if (this.moving == Piece.BLACK &&
                (this.castling & 0x2) == 2 &&
                this.pieces[5] == Piece.EMPTY &&
                this.pieces[6] == Piece.EMPTY &&
                !this.isSquareAttackedBy(4, 0) &&
                !this.isSquareAttackedBy(5, 0) &&
                !this.isSquareAttackedBy(6, 0)
        )
            moves[numMoves++] = new int[]{4, 6, -1, Move.CASTLE, this.epSquare, this.castling};

        if (this.moving == Piece.BLACK &&
                (this.castling & 0x1) == 1 &&
                this.pieces[1] == Piece.EMPTY &&
                this.pieces[2] == Piece.EMPTY &&
                this.pieces[3] == Piece.EMPTY &&
                !this.isSquareAttackedBy(4, 0) &&
                !this.isSquareAttackedBy(3, 0) &&
                !this.isSquareAttackedBy(2, 0)
        )
            moves[numMoves++] = new int[]{4, 2, -1, Move.CASTLE, this.epSquare, this.castling};

        int[][] result = new int[numMoves][6];
        System.arraycopy(moves, 0, result, 0, numMoves);

        return result;
    }

    public void move(int[] move) {
        int start = move[0];
        int end = move[1];
        int flags = move[3];

        this.pieces[end] = this.pieces[start];
        this.colors[end] = this.colors[start];

        this.pieces[start] = Piece.EMPTY;
        this.colors[start] = Piece.EMPTY;

        if (this.pieces[end] == Piece.KING)
            this.kings[this.moving] = end;

        this.epSquare = 0;

        if (start == 63 || end == 63) this.castling &= 0x7; // -Qkq 0111
        if (start == 56 || end == 56) this.castling &= 0xB; // K-kq 1011
        if (start == 60 || end == 60) this.castling &= 0x3; // --kq 0011
        if (start == 0 || end == 0) this.castling &= 0xE; // KQk- 1110
        if (start == 7 || end == 7) this.castling &= 0xD; // KQ-q 1101
        if (start == 4 || end == 4) this.castling &= 0xC; // KQ-- 1100

        if (flags == Move.EP) {
            int dir = this.moving == 0 ? 8 : -8;
            this.epSquare = end + dir;
        } else if (flags == Move.EPCAPTURE) {
            int dir = this.moving == 0 ? 8 : -8;
            this.pieces[end + dir] = Piece.EMPTY;
            this.colors[end + dir] = Piece.EMPTY;
        } else if (flags == Move.CASTLE) {
            if (end == 62) { // white king side
                this.pieces[61] = Piece.ROOK;
                this.colors[61] = this.moving;
                this.pieces[63] = Piece.EMPTY;
                this.colors[63] = Piece.EMPTY;
            } else if (end == 58) { // white queen side
                this.pieces[59] = Piece.ROOK;
                this.colors[59] = this.moving;
                this.pieces[56] = Piece.EMPTY;
                this.colors[56] = Piece.EMPTY;
            } else if (end == 2) { // black queen side
                this.pieces[3] = Piece.ROOK;
                this.colors[3] = this.moving;
                this.pieces[0] = Piece.EMPTY;
                this.colors[0] = Piece.EMPTY;
            } else if (end == 6) { // black king side
                this.pieces[5] = Piece.ROOK;
                this.colors[5] = this.moving;
                this.pieces[7] = Piece.EMPTY;
                this.colors[7] = Piece.EMPTY;
            }
        }

        this.moving = 1 - this.moving;
    }

    public void unmove(int[] move) {
        this.moving = 1 - this.moving;

        int start = move[0];
        int end = move[1];
        int captured = move[2];
        int flags = move[3];
        int epSquare = move[4];
        int castling = move[5];

        this.pieces[start] = this.pieces[end];
        this.colors[start] = this.colors[end];

        this.pieces[end] = captured;
        this.colors[end] = flags == Move.CAPTURE ? 1 - this.moving : Piece.EMPTY;

        if (this.pieces[start] == Piece.KING)
            this.kings[this.moving] = start;

        this.epSquare = epSquare;
        this.castling = castling;

        if (flags == Move.EPCAPTURE) {
            int dir = this.moving == 0 ? 8 : -8;
            this.pieces[end + dir] = Piece.PAWN;
            this.colors[end + dir] = 1 - this.moving;

        } else if (flags == Move.CASTLE) {
            if (end == 62) { // white king side
                this.pieces[63] = Piece.ROOK;
                this.colors[63] = this.moving;
                this.pieces[61] = Piece.EMPTY;
                this.colors[61] = Piece.EMPTY;
            } else if (end == 58) { // white queen side
                this.pieces[56] = Piece.ROOK;
                this.colors[56] = this.moving;
                this.pieces[59] = Piece.EMPTY;
                this.colors[59] = Piece.EMPTY;
            } else if (end == 2) { // black queen side
                this.pieces[0] = Piece.ROOK;
                this.colors[0] = this.moving;
                this.pieces[3] = Piece.EMPTY;
                this.colors[3] = Piece.EMPTY;
            } else if (end == 6) { // black king side
                this.pieces[7] = Piece.ROOK;
                this.colors[7] = this.moving;
                this.pieces[5] = Piece.EMPTY;
                this.colors[5] = Piece.EMPTY;
            }
        }
    }

    public boolean isSquareAttackedBy(int square, int color) {
        // Pawn attacks
        int attackingSquare;

        if (color == Piece.WHITE) {
            attackingSquare = mailbox[mailbox64[square] + Piece.S + Piece.W];
            if (attackingSquare != -1 && pieces[attackingSquare] == Piece.PAWN && colors[attackingSquare] == color)
                return true;

            attackingSquare = mailbox[mailbox64[square] + Piece.S + Piece.E];
            if (attackingSquare != -1 && pieces[attackingSquare] == Piece.PAWN && colors[attackingSquare] == color)
                return true;
        } else {
            attackingSquare = mailbox[mailbox64[square] + Piece.N + Piece.W];
            if (attackingSquare != -1 && pieces[attackingSquare] == Piece.PAWN && colors[attackingSquare] == color)
                return true;

            attackingSquare = mailbox[mailbox64[square] + Piece.N + Piece.E];
            if (attackingSquare != -1 && pieces[attackingSquare] == Piece.PAWN && colors[attackingSquare] == color)
                return true;
        }

        // knights
        for (int i = 0; i < 8; i++) {
            attackingSquare = mailbox[mailbox64[square] + Piece.directions[Piece.KNIGHT][i]];
            if (attackingSquare != -1 && pieces[attackingSquare] == Piece.KNIGHT && colors[attackingSquare] == color)
                return true;
        }

        // ranks/columns
        int[] directions = new int[]{Piece.N, Piece.E, Piece.W, Piece.S};
        for (int i = 0; i < 4; i++) {
            attackingSquare = square;
            do {
                attackingSquare = mailbox[mailbox64[attackingSquare] + directions[i]];
            } while (attackingSquare != -1 && colors[attackingSquare] == Piece.EMPTY);

            if (attackingSquare != -1 && colors[attackingSquare] == color && (pieces[attackingSquare] == Piece.QUEEN || pieces[attackingSquare] == Piece.ROOK))
                return true;
        }

        // diagonals
        directions = new int[]{Piece.N + Piece.E, Piece.S + Piece.E, Piece.S + Piece.W, Piece.N + Piece.W};
        for (int i = 0; i < 4; i++) {
            attackingSquare = square;
            do {
                attackingSquare = mailbox[mailbox64[attackingSquare] + directions[i]];
            } while (attackingSquare != -1 && colors[attackingSquare] == Piece.EMPTY);

            if (attackingSquare != -1 && colors[attackingSquare] == color && (pieces[attackingSquare] == Piece.QUEEN || pieces[attackingSquare] == Piece.BISHOP))
                return true;
        }

        return false;
    }
}
