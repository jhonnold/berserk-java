package me.honnold.berserk.board;

import static me.honnold.berserk.util.BBUtils.*;

import java.util.Arrays;
import me.honnold.berserk.eval.PositionEvaluations;
import me.honnold.berserk.moves.AttackMasks;
import me.honnold.berserk.moves.Move;
import me.honnold.berserk.tt.ZobristHash;
import org.apache.commons.lang3.ArrayUtils;

public class Position {
    private final AttackMasks attackMasks = AttackMasks.getInstance();
    private final ZobristHash hashUtil = ZobristHash.getInstance();

    public long[] pieceBitboards = new long[12];
    public long[] occupancyBitboards = new long[3];
    public byte sideToMove;
    public byte castling;
    public int epSquare;
    public long zHash;

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

        sideToMove = (byte) ("w".equals(parts[1]) ? 0 : 1);

        castling = 0;
        if (parts[2].contains("K")) castling |= 0x8;
        if (parts[2].contains("Q")) castling |= 0x4;
        if (parts[2].contains("k")) castling |= 0x2;
        if (parts[2].contains("q")) castling |= 0x1;

        epSquare = parts[3].equals("-") ? -1 : ArrayUtils.indexOf(squares, parts[3]);

        for (int i = 0; i < 12; i++) occupancyBitboards[i % 2] |= pieceBitboards[i];
        occupancyBitboards[2] = occupancyBitboards[0] | occupancyBitboards[1];

        this.zHash = hashUtil.getZobristHash(this);
    }

    public Position(Position p) {
        this.pieceBitboards = p.pieceBitboards.clone();
        this.occupancyBitboards = p.occupancyBitboards.clone();
        this.sideToMove = p.sideToMove;
        this.castling = p.castling;
        this.epSquare = p.epSquare;
        this.zHash = p.zHash;
    }

    public int getValue() {
        return PositionEvaluations.getInstance().positionEvaluation(this);
    }

    public boolean makeMove(Move m) {
        int start = m.getStart();
        int end = m.getEnd();
        int piece = m.getPieceIdx();
        int promotionPiece = m.getPromotionPiece();

        this.pieceBitboards[piece] = popBit(this.pieceBitboards[piece], start);
        this.zHash ^= hashUtil.getPieceKey(piece, start);

        this.pieceBitboards[piece] = setBit(this.pieceBitboards[piece], end);
        this.zHash ^= hashUtil.getPieceKey(piece, end);

        if (m.isCapture()) {
            for (int i = (1 - sideToMove); i < 12; i += 2) {
                if (getBit(this.pieceBitboards[i], end)) {
                    this.pieceBitboards[i] = popBit(this.pieceBitboards[i], end);
                    this.zHash ^= hashUtil.getPieceKey(i, end);
                    break;
                }
            }
        }

        if (m.isPromotion()) {
            this.pieceBitboards[sideToMove] = popBit(this.pieceBitboards[sideToMove], end);
            this.zHash ^= hashUtil.getPieceKey(sideToMove, end);

            this.pieceBitboards[promotionPiece] = setBit(this.pieceBitboards[promotionPiece], end);
            this.zHash ^= hashUtil.getPieceKey(promotionPiece, end);
        }

        if (m.isEPCapture()) {
            this.pieceBitboards[1 - sideToMove] =
                    popBit(
                            this.pieceBitboards[1 - sideToMove],
                            end - pawnDirections[this.sideToMove]);
            this.zHash ^= hashUtil.getPieceKey(1 - sideToMove, end - pawnDirections[sideToMove]);
        }

        if (this.epSquare != -1) {
            this.zHash ^= hashUtil.getEpKey(this.epSquare);
            this.epSquare = -1;
        }

        if (m.isDoublePush()) {
            this.epSquare = end - pawnDirections[this.sideToMove];
            this.zHash ^= hashUtil.getEpKey(this.epSquare);
        }

        if (m.isCastle()) {
            switch (end) {
                case 62:
                    this.pieceBitboards[6] = popBit(this.pieceBitboards[6], 63);
                    this.zHash ^= hashUtil.getPieceKey(6, 63);

                    this.pieceBitboards[6] = setBit(this.pieceBitboards[6], 61);
                    this.zHash ^= hashUtil.getPieceKey(6, 61);
                    break;
                case 58:
                    this.pieceBitboards[6] = popBit(this.pieceBitboards[6], 56);
                    this.zHash ^= hashUtil.getPieceKey(6, 56);

                    this.pieceBitboards[6] = setBit(this.pieceBitboards[6], 59);
                    this.zHash ^= hashUtil.getPieceKey(6, 59);
                    break;
                case 6:
                    this.pieceBitboards[7] = popBit(this.pieceBitboards[7], 7);
                    this.zHash ^= hashUtil.getPieceKey(7, 7);

                    this.pieceBitboards[7] = setBit(this.pieceBitboards[7], 5);
                    this.zHash ^= hashUtil.getPieceKey(7, 5);
                    break;
                case 2:
                    this.pieceBitboards[7] = popBit(this.pieceBitboards[7], 0);
                    this.zHash ^= hashUtil.getPieceKey(7, 0);

                    this.pieceBitboards[7] = setBit(this.pieceBitboards[7], 3);
                    this.zHash ^= hashUtil.getPieceKey(7, 3);
                    break;
            }
        }

        this.zHash ^= hashUtil.getCastleKey(this.castling);
        this.castling &= castlingRights[start];
        this.castling &= castlingRights[end];
        this.zHash ^= hashUtil.getCastleKey(this.castling);

        Arrays.fill(this.occupancyBitboards, 0L);
        for (int i = 0; i < 12; i++) occupancyBitboards[i % 2] |= pieceBitboards[i];

        occupancyBitboards[2] = occupancyBitboards[0] | occupancyBitboards[1];

        this.sideToMove ^= 1;

        return !isSquareAttacked(
                getLSBIndex(this.pieceBitboards[11 - this.sideToMove]), this.sideToMove);
    }

    public boolean isSquareAttacked(int square, int bySide) {
        if (square == -1) return false;

        if ((attackMasks.getPawnAttacks(1 - bySide, square) & pieceBitboards[bySide]) != 0)
            return true;
        if ((attackMasks.getKnightAttacks(square) & pieceBitboards[2 + bySide]) != 0) return true;
        if ((attackMasks.getBishopAttacks(square, occupancyBitboards[2])
                        & pieceBitboards[4 + bySide])
                != 0) return true;
        if ((attackMasks.getRookAttacks(square, occupancyBitboards[2]) & pieceBitboards[6 + bySide])
                != 0) return true;
        if ((attackMasks.getQueenAttacks(square, occupancyBitboards[2])
                        & pieceBitboards[8 + bySide])
                != 0) return true;

        return (attackMasks.getKingAttacks(square) & pieceBitboards[10 + bySide]) != 0;
    }

    public boolean inCheck() {
        return this.isSquareAttacked(
                getLSBIndex(pieceBitboards[10 + this.sideToMove]), 1 - this.sideToMove);
    }

    public int getCapturedPieceIdx(int captureSquare) {
        for (int i = 0; i < 12; i++) {
            long bb = pieceBitboards[i];

            if (getBit(bb, captureSquare)) return i;
        }

        if (captureSquare == this.epSquare) return 1 - this.sideToMove;

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

    public GameStage getGameStage() {
        int pieceValues = 0;
        for (int i = 2; i < 10; i++) {
            long bb = pieceBitboards[i];
            int numPieces = countBits(bb);

            pieceValues += (numPieces * Piece.getPieceValue(i, GameStage.OPENING));
        }

        if (pieceValues > 6000) return GameStage.OPENING;
        else if (pieceValues < 1500) return GameStage.ENDGAME;
        else return GameStage.MIDDLEGAME;
    }
}