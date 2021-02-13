package me.honnold.berserk.eval;

import static me.honnold.berserk.util.BBUtils.*;

import me.honnold.berserk.board.Position;
import me.honnold.berserk.moves.AttackMasks;

public class PositionEvaluations {
    private static final PositionEvaluations POSITION_EVALUATION = new PositionEvaluations();
    private static final int DOUBLED_PAWN = 10;
    private static final int ISOLATED_PAWN = 20;
    private static final int BACKWARDS_PAWN = 10;
    private static final int PASSED_PAWN = 20;
    private static final int BISHOP_PAIR = 35;
    private static final int KNIGHT_PAIR = 15;
    private static final int OPEN_FILE = 20;
    private static final int SEMI_OPEN_FILE = 8;
    private final AttackMasks masks = AttackMasks.getInstance();
    private final int power = 12;
    private final int shifts = 64 - power;
    private final int[] evaluations;

    private PositionEvaluations() {
        evaluations = new int[(1 << power) * 2];
    }

    public static PositionEvaluations getInstance() {
        return POSITION_EVALUATION;
    }

    public int positionEvaluation(Position position) {
        //        int idx = getEvalTableIdx(position.zHash);
        //
        //        if (evaluations[idx] == (int) position.zHash) return evaluations[idx + 1];

        int xside = 1 ^ position.sideToMove;

        long mobilityArea = getMobilitySquares(position, position.sideToMove);
        long oppoMobilityArea = getMobilitySquares(position, xside);

        int score = this.pieceValue(position, position.sideToMove);
        score -= this.pieceValue(position, xside);

        score += this.pawnsEval(position, position.sideToMove);
        score -= this.pawnsEval(position, xside);

        score += this.knightEval(position, position.sideToMove, mobilityArea);
        score -= this.knightEval(position, xside, oppoMobilityArea);

        score += this.bishopEval(position, position.sideToMove, mobilityArea);
        score -= this.bishopEval(position, xside, mobilityArea);

        score += this.rookEval(position, position.sideToMove, mobilityArea);
        score -= this.rookEval(position, xside, mobilityArea);

        score += this.queenEval(position, position.sideToMove, mobilityArea);
        score -= this.queenEval(position, xside, mobilityArea);

        score += this.kingEval(position, position.sideToMove);
        score -= this.kingEval(position, xside);

        //        idx = getEvalTableIdx(position.zHash);
        //        evaluations[idx] = (int) position.zHash;
        //        evaluations[idx + 1] = score;

        return score;
    }

    private long getMobilitySquares(Position position, int side) {
        long shiftedPieces =
                side == 0
                        ? position.occupancyBitboards[2] << 8
                        : position.occupancyBitboards[2] >>> 8;

        long blockedAndHomePawns =
                position.pieceBitboards[side]
                        & (shiftedPieces
                                | masks.rowMasks[side == 0 ? 6 : 1]
                                | masks.rowMasks[side == 0 ? 5 : 2]);
        return ~(blockedAndHomePawns
                | position.pieceBitboards[10 + side]
                | position.pieceBitboards[8 + side]
                | position.pawnAttacks(side ^ 1));
    }

    private int pieceValue(Position position, int side) {
        int score = 0;

        for (int i = side; i < 12; i += 2) {
            long bb = position.pieceBitboards[i];
            while (bb != 0) {
                int sq = getLSBIndex(bb);
                bb = popLSB(bb);

                score +=
                        Piece.getPieceValue(i, position.getPhase())
                                + Piece.getPositionValue(i, sq, position.getPhase());
            }
        }

        return score;
    }

    private int pawnsEval(Position position, int side) {
        int score = 0;

        int xside = side ^ 1;
        long myPawns = position.pieceBitboards[side], originalPawns = myPawns;
        long opponentPawns = position.pieceBitboards[xside];
        int pawnDirection = pawnDirections[side];

        while (myPawns != 0) {
            int sq = getLSBIndex(myPawns);
            int col = sq % 8;
            int row = sq / 8;

            myPawns = popLSB(myPawns);

            if ((myPawns & (1L << (sq - pawnDirection))) != 0) score -= DOUBLED_PAWN;

            long futureRanks = 0;
            if (side == 0) for (int i = row - 1; i >= 0; i--) futureRanks |= masks.rowMasks[i];
            else for (int i = row + 1; i <= 7; i++) futureRanks |= masks.rowMasks[i];

            if ((originalPawns & masks.getColumnMask(col - 1)) == 0
                    && (originalPawns & masks.getColumnMask(col + 1)) == 0) score -= ISOLATED_PAWN;
            else {
                long previousRanks = ~futureRanks;
                if ((previousRanks & masks.getColumnMask(col - 1) & originalPawns) == 0
                        && (previousRanks & masks.getColumnMask(col + 1) & originalPawns) == 0) {
                    score -= BACKWARDS_PAWN;
                }
            }

            if ((opponentPawns & futureRanks & masks.getColumnMask(col)) == 0
                    && (opponentPawns & futureRanks & masks.getColumnMask(col - 1)) == 0
                    && (opponentPawns & futureRanks & masks.getColumnMask(col + 1)) == 0) {
                if (side == 0) score += (7 - row) * PASSED_PAWN;
                else score += row * PASSED_PAWN;
            }
        }

        return score;
    }

    private int knightEval(Position position, int side, long mobilityArea) {
        int score = 0;

        long myKnights = position.pieceBitboards[side + 2];
        long opponentKing = position.pieceBitboards[11 - side];
        long opponentKingSquares = opponentKing | masks.getKingAttacks(getLSBIndex(opponentKing));

        if (countBits(myKnights) > 1) {
            score -= KNIGHT_PAIR;
        }

        while (myKnights != 0) {
            int sq = getLSBIndex(myKnights);
            myKnights = popLSB(myKnights);

            int mobility = countBits(mobilityArea & masks.getKnightAttacks(sq));

            score += Piece.getPieceMobilityValue(2, mobility, position.getPhase());

            if ((masks.getKnightAttacks(sq) & opponentKingSquares) != 0) score += 31;

            if ((masks.getPawnAttacks(side ^ 1, sq) & position.pieceBitboards[side]) != 0)
                score += 10;
        }

        return score;
    }

    private int bishopEval(Position position, int side, long mobilityArea) {
        int score = 0;

        long myBishops = position.pieceBitboards[4 + side];
        long opponentKing = position.pieceBitboards[11 - side];
        long opponentKingSquares = opponentKing | masks.getKingAttacks(getLSBIndex(opponentKing));

        if (countBits(myBishops)
                > 1) { // TODO: This check should include something to make sure they're different
            // colors
            score += BISHOP_PAIR;
        }

        while (myBishops != 0) {
            int sq = getLSBIndex(myBishops);
            myBishops = popLSB(myBishops);

            int mobility =
                    countBits(
                            masks.getBishopAttacks(
                                            sq,
                                            position.occupancyBitboards[2]
                                                    ^ position.pieceBitboards[8 + side])
                                    & mobilityArea);
            score += Piece.getPieceMobilityValue(4, mobility, position.getPhase());

            long kingAttackSqs = masks.getBishopAttacks(sq, side) & opponentKingSquares;
            if (kingAttackSqs != 0) score += 15 * countBits(kingAttackSqs);

            if ((masks.getPawnAttacks(side ^ 1, sq) & position.pieceBitboards[side]) != 0)
                score += 5;
        }

        return score;
    }

    private int rookEval(Position position, int side, long mobilityArea) {
        int score = 0;

        long myRooks = position.pieceBitboards[6 + side];
        long opponentKing = position.pieceBitboards[11 - side];
        long opponentKingSquares = opponentKing | masks.getKingAttacks(getLSBIndex(opponentKing));

        while (myRooks != 0) {
            int sq = getLSBIndex(myRooks);
            int col = sq & 7;

            int mobility =
                    countBits(
                            masks.getRookAttacks(
                                            sq,
                                            position.occupancyBitboards[2]
                                                    ^ (position.pieceBitboards[8 + side]
                                                            | position.pieceBitboards[6 + side]))
                                    & mobilityArea);
            score += Piece.getPieceMobilityValue(6, mobility, position.getPhase());

            if (((position.pieceBitboards[side] | position.pieceBitboards[1 ^ side])
                            & masks.getColumnMask(col))
                    == 0) {
                score += OPEN_FILE;
            } else if ((position.pieceBitboards[side] & masks.getColumnMask(col)) == 0) {
                score += SEMI_OPEN_FILE;
            }

            long kingAttackSqs = masks.getRookAttacks(sq, side) & opponentKingSquares;
            if (kingAttackSqs != 0) score += 15 * countBits(kingAttackSqs);

            myRooks = popLSB(myRooks);
        }

        return score;
    }

    private int queenEval(Position position, int side, long mobilityArea) {
        int score = 0;

        long myQueens = position.pieceBitboards[8 + side];
        long opponentKing = position.pieceBitboards[11 - side];
        long opponentKingSquares = opponentKing | masks.getKingAttacks(getLSBIndex(opponentKing));

        while (myQueens != 0) {
            int sq = getLSBIndex(myQueens);
            int mobility =
                    countBits(
                            masks.getQueenAttacks(sq, position.occupancyBitboards[2])
                                    & mobilityArea);
            score += Piece.getPieceMobilityValue(8, mobility, position.getPhase());

            long kingAttackSqs = masks.getQueenAttacks(sq, side) & opponentKingSquares;
            if (kingAttackSqs != 0) score += 4 * countBits(kingAttackSqs);

            myQueens = popLSB(myQueens);
        }

        return score;
    }

    private int kingEval(Position position, int side) {
        int score = 0;
        int kingLoc = getLSBIndex(position.pieceBitboards[10 + side]);

        if (side == 0) {
            if (kingLoc > 60) {
                score += 10 * (countBits((7L << 53) & position.pieceBitboards[side]));
                score += 5 * (countBits((7L << 45) & position.pieceBitboards[side]));
            } else if (kingLoc < 59) {
                score += 10 * (countBits((7L << 48) & position.pieceBitboards[side]));
                score += 5 * (countBits((7L << 40) & position.pieceBitboards[side]));
            }
        } else {
            if (kingLoc > 4) {
                score += 10 * (countBits((7L << 13) & position.pieceBitboards[side]));
                score += 5 * (countBits((7L << 21) & position.pieceBitboards[side]));
            } else if (kingLoc < 3) {
                score += 10 * (countBits((7L << 8) & position.pieceBitboards[side]));
                score += 5 * (countBits((7L << 16) & position.pieceBitboards[side]));
            }
        }

        return score;
    }

    private int getEvalTableIdx(long hash) {
        return (int) (hash >>> shifts) << 1;
    }
}
