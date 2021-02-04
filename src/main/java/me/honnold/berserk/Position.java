package me.honnold.berserk;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static me.honnold.berserk.BoardUtils.*;

public class Position {
    public static final String[] squares = {
            "a8", "b8", "c8", "d8", "e8", "f8", "g8", "h8",
            "a7", "b7", "c7", "d7", "e7", "f7", "g7", "h7",
            "a6", "b6", "c6", "d6", "e6", "f6", "g6", "h6",
            "a5", "b5", "c5", "d5", "e5", "f5", "g5", "h5",
            "a4", "b4", "c4", "d4", "e4", "f4", "g4", "h4",
            "a3", "b3", "c3", "d3", "e3", "f3", "g3", "h3",
            "a2", "b2", "c2", "d2", "e2", "f2", "g2", "h2",
            "a1", "b1", "c1", "d1", "e1", "f1", "g1", "h1",
    };

    public static final int[] castlingRights = {
            14, 15, 15, 15, 12, 15, 15, 13,
            15, 15, 15, 15, 15, 15, 15, 15,
            15, 15, 15, 15, 15, 15, 15, 15,
            15, 15, 15, 15, 15, 15, 15, 15,
            15, 15, 15, 15, 15, 15, 15, 15,
            15, 15, 15, 15, 15, 15, 15, 15,
            15, 15, 15, 15, 15, 15, 15, 15,
            11, 15, 15, 15, 3, 15, 15, 7
    };

    public static final char[] pieceSymbols = {'P', 'p', 'N', 'n', 'B', 'b', 'R', 'r', 'Q', 'q', 'K', 'k'};

    public long[] pieceBitboards = new long[12];
    public long[] occupancyBitboards = new long[3];

    public int sideToMove;
    public int castling;
    public int epSquare;
    public long zHash = 0;
    private long pawnHash = 0;
    public static int[] pawnDirections = {-8, 8};

    public Position(String fen) {
        String[] parts = fen.split("\\s+");

        int square = 0;
        for (char c : parts[0].toCharArray()) {
            if (c == '/') continue;

            if (c >= '1' && c <= '8') {
                square += (c - '0');
            } else {
                int bitboard = ArrayUtils.indexOf(pieceSymbols, c);

                pieceBitboards[bitboard] = setBit(pieceBitboards[bitboard], square++);
            }
        }

        sideToMove = "w".equals(parts[1]) ? 0 : 1;

        castling = 0;
        if (parts[2].contains("K")) castling |= 0x8;
        if (parts[2].contains("Q")) castling |= 0x4;
        if (parts[2].contains("k")) castling |= 0x2;
        if (parts[2].contains("q")) castling |= 0x1;

        epSquare = parts[3].equals("-") ? -1 : ArrayUtils.indexOf(squares, parts[3]);

        for (int i = 0; i < 12; i++)
            occupancyBitboards[i % 2] |= pieceBitboards[i];
        occupancyBitboards[2] = occupancyBitboards[0] | occupancyBitboards[1];

        this.zHash = ZobristHash.generate(this);
        this.pawnHash = ZobristHash.generatePawn(this);
    }

    public Position(Position p) {
        this.pieceBitboards = new long[12];
        System.arraycopy(p.pieceBitboards, 0, this.pieceBitboards, 0, 12);

        this.occupancyBitboards = new long[3];
        System.arraycopy(p.occupancyBitboards, 0, this.occupancyBitboards, 0, 3);

        this.sideToMove = p.sideToMove;
        this.castling = p.castling;
        this.epSquare = p.epSquare;
        this.zHash = p.zHash;
    }

    public int getValue() {
        return PositionEvaluations.getInstance().positionEvaluation(this);
    }

    public boolean makeMove(Move m) {
        int start = m.start;
        int end = m.end;
        int piece = m.pieceIdx;
        int promotionPiece = m.promotionPiece;

        this.pieceBitboards[piece] = popBit(this.pieceBitboards[piece], start);
        this.zHash ^= ZobristHash.pieceKeys[piece][start];
        if (piece <= 1) this.pawnHash ^= ZobristHash.pieceKeys[piece][start];

        this.pieceBitboards[piece] = setBit(this.pieceBitboards[piece], end);
        this.zHash ^= ZobristHash.pieceKeys[piece][end];
        if (piece <= 1) this.pawnHash ^= ZobristHash.pieceKeys[piece][end];

        if (m.capture) {
            for (int i = (1 - sideToMove); i < 12; i += 2) {
                if (getBit(this.pieceBitboards[i], end)) {
                    this.pieceBitboards[i] = popBit(this.pieceBitboards[i], end);
                    this.zHash ^= ZobristHash.pieceKeys[i][end];
                    if (i <= 1) this.pawnHash ^= ZobristHash.pieceKeys[i][end];
                    break;
                }
            }
        }

        if (promotionPiece >= 0) {
            this.pieceBitboards[sideToMove] = popBit(this.pieceBitboards[sideToMove], end);
            this.zHash ^= ZobristHash.pieceKeys[sideToMove][end];
            this.pawnHash ^= ZobristHash.pieceKeys[sideToMove][end];

            this.pieceBitboards[promotionPiece] = setBit(this.pieceBitboards[promotionPiece], end);
            this.zHash ^= ZobristHash.pieceKeys[promotionPiece][end];
        }

        if (m.epCapture) {
            this.pieceBitboards[1 - sideToMove] = popBit(this.pieceBitboards[1 - sideToMove], end - this.pawnDirections[this.sideToMove]);
            this.zHash ^= ZobristHash.pieceKeys[1 - sideToMove][end - this.pawnDirections[this.sideToMove]];
            this.pawnHash ^= ZobristHash.pieceKeys[1 - sideToMove][end - this.pawnDirections[this.sideToMove]];
        }

        if (this.epSquare != -1) {
            this.zHash ^= ZobristHash.epKeys[this.epSquare];
            this.epSquare = -1;
        }

        if (m.doublePush) {
            this.epSquare = end - pawnDirections[this.sideToMove];
            this.zHash ^= ZobristHash.epKeys[this.epSquare];
        }

        if (m.castle) {
            switch (end) {
                case 62:
                    this.pieceBitboards[6] = popBit(this.pieceBitboards[6], 63);
                    this.zHash ^= ZobristHash.pieceKeys[6][63];

                    this.pieceBitboards[6] = setBit(this.pieceBitboards[6], 61);
                    this.zHash ^= ZobristHash.pieceKeys[6][61];
                    break;
                case 58:
                    this.pieceBitboards[6] = popBit(this.pieceBitboards[6], 56);
                    this.zHash ^= ZobristHash.pieceKeys[6][56];

                    this.pieceBitboards[6] = setBit(this.pieceBitboards[6], 59);
                    this.zHash ^= ZobristHash.pieceKeys[6][59];
                    break;
                case 6:
                    this.pieceBitboards[7] = popBit(this.pieceBitboards[7], 7);
                    this.zHash ^= ZobristHash.pieceKeys[7][7];

                    this.pieceBitboards[7] = setBit(this.pieceBitboards[7], 5);
                    this.zHash ^= ZobristHash.pieceKeys[7][5];
                    break;
                case 2:
                    this.pieceBitboards[7] = popBit(this.pieceBitboards[7], 0);
                    this.zHash ^= ZobristHash.pieceKeys[7][0];

                    this.pieceBitboards[7] = setBit(this.pieceBitboards[7], 3);
                    this.zHash ^= ZobristHash.pieceKeys[7][3];
                    break;
            }
        }

        this.zHash ^= ZobristHash.castleKeys[this.castling];
        this.castling &= castlingRights[start];
        this.castling &= castlingRights[end];
        this.zHash ^= ZobristHash.castleKeys[this.castling];

        Arrays.fill(this.occupancyBitboards, 0L);
        for (int i = 0; i < 12; i++)
            occupancyBitboards[i % 2] |= pieceBitboards[i];

        occupancyBitboards[2] = occupancyBitboards[0] | occupancyBitboards[1];

        this.sideToMove = 1 - this.sideToMove;

        return !isSquareAttacked(getLSBIndex(this.pieceBitboards[11 - this.sideToMove]), this.sideToMove);
    }

    public boolean isSquareAttacked(int square, int bySide) {
        if (square == -1) return false;

        if ((AttackMasks.PAWN_ATTACKS[1 - bySide][square] & pieceBitboards[bySide]) != 0) return true;
        if ((AttackMasks.KNIGHT_ATTACKS[square] & pieceBitboards[2 + bySide]) != 0) return true;
        if ((AttackMasks.getBishopAttacks(square, occupancyBitboards[2]) & pieceBitboards[4 + bySide]) != 0)
            return true;
        if ((AttackMasks.getRookAttacks(square, occupancyBitboards[2]) & pieceBitboards[6 + bySide]) != 0) return true;
        if ((AttackMasks.getQueenAttacks(square, occupancyBitboards[2]) & pieceBitboards[8 + bySide]) != 0) return true;

        return (AttackMasks.KING_ATTACKS[square] & pieceBitboards[10 + bySide]) != 0;
    }

    public Move[] getMoves() {
        List<Move> moves = new ArrayList<>();

        long pieceBoard, attacks;
        int direction = sideToMove == 0 ? -8 : 8;
        int start, end;

        for (int i = sideToMove; i < 12; i += 2) {
            pieceBoard = pieceBitboards[i];

            if (i >> 1 == 0) { // pawns
                while (pieceBoard != 0) {
                    start = getLSBIndex(pieceBoard);
                    pieceBoard = popBit(pieceBoard, start);
                    end = start + direction;

                    // off the board or blocked
                    if (!getBit(occupancyBitboards[2], end)) {
                        if ((sideToMove == 0 && start >= 8 && start <= 15) || (sideToMove == 1 && start >= 48 && start <= 55)) {
                            moves.add(new Move(start, end, i, 8 + sideToMove, false, false, false, false));
                            moves.add(new Move(start, end, i, 6 + sideToMove, false, false, false, false));
                            moves.add(new Move(start, end, i, 4 + sideToMove, false, false, false, false));
                            moves.add(new Move(start, end, i, 2 + sideToMove, false, false, false, false));
                        } else {
                            moves.add(new Move(start, end, i, -1, false, false, false, false));
                            if ((sideToMove == 0 && start >= 48 && start <= 55) || (sideToMove == 1 && start >= 8 && start <= 15)) {
                                end += direction;
                                if (!getBit(occupancyBitboards[2], end))
                                    moves.add(new Move(start, end, i, -1, false, true, false, false));
                            }
                        }
                    }

                    attacks = AttackMasks.PAWN_ATTACKS[sideToMove][start] & occupancyBitboards[1 - sideToMove];
                    while (attacks != 0) {
                        end = getLSBIndex(attacks);
                        attacks = popBit(attacks, end);

                        if ((sideToMove == 0 && start >= 8 && start <= 15) || (sideToMove == 1 && start >= 48 && start <= 55)) {
                            moves.add(new Move(start, end, i, 8 + sideToMove, true, false, false, false));
                            moves.add(new Move(start, end, i, 6 + sideToMove, true, false, false, false));
                            moves.add(new Move(start, end, i, 4 + sideToMove, true, false, false, false));
                            moves.add(new Move(start, end, i, 2 + sideToMove, true, false, false, false));
                        } else {
                            moves.add(new Move(start, end, i, -1, true, false, false, false));
                        }
                    }

                    if (epSquare >= 0) {
                        attacks = AttackMasks.PAWN_ATTACKS[sideToMove][start] & (1L << epSquare);
                        if (attacks != 0) {
                            moves.add(new Move(start, epSquare, i, -1, false, false, true, false));
                        }
                    }
                }
            } else if (i >> 1 == 5) { // kings
                if (sideToMove == 0 && (castling & 0x8) == 8) {
                    if (!getBit(occupancyBitboards[2], 61) && !getBit(occupancyBitboards[2], 62)) {
                        if (!isSquareAttacked(60, 1) && !isSquareAttacked(61, 1) && !isSquareAttacked(62, 1)) {
                            moves.add(new Move(60, 62, i, -1, false, false, false, true));
                        }
                    }
                }

                if (sideToMove == 0 && (castling & 0x4) == 4) {
                    if (!getBit(occupancyBitboards[2], 57) && !getBit(occupancyBitboards[2], 58) && !getBit(occupancyBitboards[2], 59)) {
                        if (!isSquareAttacked(60, 1) && !isSquareAttacked(59, 1) && !isSquareAttacked(58, 1)) {
                            moves.add(new Move(60, 58, i, -1, false, false, false, true));
                        }
                    }
                }

                if (sideToMove == 1 && (castling & 0x2) == 2) {
                    if (!getBit(occupancyBitboards[2], 5) && !getBit(occupancyBitboards[2], 6)) {
                        if (!isSquareAttacked(4, 0) && !isSquareAttacked(5, 0) && !isSquareAttacked(6, 0)) {
                            moves.add(new Move(4, 6, i, -1, false, false, false, true));
                        }
                    }
                }

                if (sideToMove == 1 && (castling & 0x1) == 1) {
                    if (!getBit(occupancyBitboards[2], 3) && !getBit(occupancyBitboards[2], 2) && !getBit(occupancyBitboards[2], 1)) {
                        if (!isSquareAttacked(4, 0) && !isSquareAttacked(3, 0) && !isSquareAttacked(2, 0)) {
                            moves.add(new Move(4, 2, i, -1, false, false, false, true));
                        }
                    }
                }

                while (pieceBoard != 0) {
                    start = getLSBIndex(pieceBoard);
                    pieceBoard = popBit(pieceBoard, start);

                    attacks = AttackMasks.KING_ATTACKS[start] & ~occupancyBitboards[sideToMove];

                    while (attacks != 0) {
                        end = getLSBIndex(attacks);
                        attacks = popBit(attacks, end);

                        if (getBit(occupancyBitboards[1 - sideToMove], end))
                            moves.add(new Move(start, end, i, -1, true, false, false, false));
                        else
                            moves.add(new Move(start, end, i, -1, false, false, false, false));
                    }
                }
            } else if (i >> 1 == 1) { // knights
                while (pieceBoard != 0) {
                    start = getLSBIndex(pieceBoard);
                    pieceBoard = popBit(pieceBoard, start);

                    attacks = AttackMasks.KNIGHT_ATTACKS[start] & ~occupancyBitboards[sideToMove];

                    while (attacks != 0) {
                        end = getLSBIndex(attacks);
                        attacks = popBit(attacks, end);

                        if (getBit(occupancyBitboards[1 - sideToMove], end))
                            moves.add(new Move(start, end, i, -1, true, false, false, false));
                        else
                            moves.add(new Move(start, end, i, -1, false, false, false, false));
                    }
                }
            } else if (i >> 1 == 2) { // bishops
                while (pieceBoard != 0) {
                    start = getLSBIndex(pieceBoard);
                    pieceBoard = popBit(pieceBoard, start);

                    attacks = AttackMasks.getBishopAttacks(start, occupancyBitboards[2]) & ~occupancyBitboards[sideToMove];

                    while (attacks != 0) {
                        end = getLSBIndex(attacks);
                        attacks = popBit(attacks, end);

                        if (getBit(occupancyBitboards[1 - sideToMove], end))
                            moves.add(new Move(start, end, i, -1, true, false, false, false));
                        else
                            moves.add(new Move(start, end, i, -1, false, false, false, false));
                    }
                }
            } else if (i >> 1 == 3) { // rooks
                while (pieceBoard != 0) {
                    start = getLSBIndex(pieceBoard);
                    pieceBoard = popBit(pieceBoard, start);

                    attacks = AttackMasks.getRookAttacks(start, occupancyBitboards[2]) & ~occupancyBitboards[sideToMove];

                    while (attacks != 0) {
                        end = getLSBIndex(attacks);
                        attacks = popBit(attacks, end);

                        if (getBit(occupancyBitboards[1 - sideToMove], end))
                            moves.add(new Move(start, end, i, -1, true, false, false, false));
                        else
                            moves.add(new Move(start, end, i, -1, false, false, false, false));
                    }
                }
            } else if (i >> 1 == 4) { // queens
                while (pieceBoard != 0) {
                    start = getLSBIndex(pieceBoard);
                    pieceBoard = popBit(pieceBoard, start);

                    attacks = AttackMasks.getQueenAttacks(start, occupancyBitboards[2]) & ~occupancyBitboards[sideToMove];

                    while (attacks != 0) {
                        end = getLSBIndex(attacks);
                        attacks = popBit(attacks, end);

                        if (getBit(occupancyBitboards[1 - sideToMove], end))
                            moves.add(new Move(start, end, i, -1, true, false, false, false));
                        else
                            moves.add(new Move(start, end, i, -1, false, false, false, false));
                    }
                }
            }
        }

        return moves.toArray(new Move[0]);
    }

    public int getCapturedPieceIdx(int captureSquare) {
        for (int i = 0; i < 12; i++) {
            long bb = pieceBitboards[i];

            if (getBit(bb, captureSquare))
                return i;
        }

        return -1;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (int r = 0; r < 8; r++) {
            for (int f = 0; f < 8; f++) {
                int sq = r * 8 + f;
                if (f == 0) builder.append(" ").append(8 - r).append(" ");

                if (!getBit(occupancyBitboards[2], sq)) {
                    builder.append(". ");
                    continue;
                }

                for (int i = 0; i < 12; i++) {
                    if (getBit(pieceBitboards[i], sq)) {
                        builder.append(pieceSymbols[i]).append(" ");
                        break;
                    }
                }
            }

            builder.append("\n");
        }
        builder.append("\n    a b c d e f g h\n\n");

        if (epSquare >= 0) builder.append("    ep: ").append(squares[epSquare]).append("\n");

        builder.append("    castling: ");
        if ((castling & 0x8) == 8) builder.append("K");
        if ((castling & 0x4) == 4) builder.append("Q");
        if ((castling & 0x2) == 2) builder.append("k");
        if ((castling & 0x1) == 1) builder.append("q");
        if (castling == 0) builder.append("-");


        return builder.append("\n\n").toString();
    }

    public long getPawnHash() {
        return pawnHash;
    }

    public boolean isEndgame() {
        int basePieceValue = 0;
        // dont add up king values
        for (int i = sideToMove; i < 10; i ++) {
            long bb = pieceBitboards[i];
            int numPieces = countBits(bb);

            basePieceValue += (numPieces * Piece.baseValues[i >> 1]);
        }

        return basePieceValue <= 2600;
    }
}
