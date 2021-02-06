package me.honnold.berserk.eval;

import static me.honnold.berserk.util.BBUtils.getLSBIndex;
import static me.honnold.berserk.util.BBUtils.popBit;

import java.util.Arrays;
import me.honnold.berserk.board.GameStage;
import me.honnold.berserk.board.Piece;
import me.honnold.berserk.board.Position;
import me.honnold.berserk.moves.AttackMasks;

public class PositionEvaluations {
    private static final int PAWN_TABLE_SIZE = 1000000;
    private static final PositionEvaluations POSITION_EVALUATION = new PositionEvaluations();
    public final long[] columnMasks = {
        72340172838076673L,
        144680345676153346L,
        289360691352306692L,
        578721382704613384L,
        1157442765409226768L,
        2314885530818453536L,
        4629771061636907072L,
        -9187201950435737472L
    };
    public final long[] rowMasks = {
        255L,
        65280L,
        16711680L,
        4278190080L,
        1095216660480L,
        280375465082880L,
        71776119061217280L,
        -72057594037927936L
    };
    private final AttackMasks masks = AttackMasks.getInstance();
    private final int[] pawnEvaluations = new int[PAWN_TABLE_SIZE];

    private PositionEvaluations() {
        Arrays.fill(pawnEvaluations, Integer.MAX_VALUE);
    }

    public static PositionEvaluations getInstance() {
        return POSITION_EVALUATION;
    }

    //    private int getPawnTableIndex(long pHash) {
    //        int modulo = (int) (pHash % PAWN_TABLE_SIZE);
    //
    //        return modulo < 0 ? modulo + PAWN_TABLE_SIZE : modulo;
    //    }
    //
    //    public void setPawnEvaluation(long pHash, int value) {
    //        int idx = getPawnTableIndex(pHash);
    //
    //        pawnEvaluations[idx] = value;
    //    }
    //
    //    public int getPawnEvaluation(long pHash) {
    //        return pawnEvaluations[getPawnTableIndex(pHash)];
    //    }
    //
    //    public int evaluatePawnStructure(Position position) {
    //        AttackMasks attackMasks = AttackMasks.getInstance();
    //
    //        long pHash = position.getPawnHash();
    //
    //        int storedEvaluation = getPawnEvaluation(pHash);
    //        if (storedEvaluation != Integer.MAX_VALUE) return storedEvaluation;
    //
    //        int extraScore = 0;
    //
    //        long pawnBitboard = position.pieceBitboards[position.sideToMove];
    //        long originalPawnBitboard = pawnBitboard;
    //        while (pawnBitboard != 0) {
    //            long opponentPawnBitboard = position.pieceBitboards[1 - position.sideToMove];
    //
    //            int pawnSquare = getLSBIndex(pawnBitboard);
    //            pawnBitboard = popBit(pawnBitboard, pawnSquare);
    //
    //            int column = pawnSquare % 8;
    //            int row = pawnSquare / 8;
    //
    //            boolean isolated = true;
    //            if (column > 0) isolated = ((columnMasks[column - 1] & originalPawnBitboard) ==
    // 0);
    //            if (column < 7)
    //                isolated = isolated && ((columnMasks[column + 1] & originalPawnBitboard) ==
    // 0);
    //
    //            if (isolated) extraScore -= 20;
    //
    //            boolean doubled = countBits(columnMasks[column] & originalPawnBitboard) > 1;
    //            if (doubled) extraScore -= 20;
    //
    //            boolean passed = true;
    //            if (position.sideToMove == 0) {
    //                for (int i = 1; i < row; i++) {
    //                    long opponentRow = opponentPawnBitboard & rowMasks[i];
    //                    if (opponentRow == 0) continue;
    //
    //                    passed = ((opponentRow & columnMasks[column]) == 0);
    //                    if (column > 0)
    //                        passed = passed && ((columnMasks[column - 1] & opponentRow) == 0);
    //                    if (column < 7)
    //                        passed = passed && ((columnMasks[column + 1] & opponentRow) == 0);
    //
    //                    if (!passed) break;
    //                }
    //            } else {
    //                for (int i = 7; i > row; i--) {
    //                    long opponentRow = opponentPawnBitboard & rowMasks[i];
    //                    if (opponentRow == 0) continue;
    //
    //                    passed = ((opponentRow & columnMasks[column]) == 0);
    //                    if (column > 0)
    //                        passed = passed && ((columnMasks[column - 1] & opponentRow) == 0);
    //                    if (column < 7)
    //                        passed = passed && ((columnMasks[column + 1] & opponentRow) == 0);
    //
    //                    if (!passed) break;
    //                }
    //            }
    //
    //            if (passed) {
    //                if (position.sideToMove == 0) extraScore += Math.min(20, (20 * (row - 2)));
    //                else extraScore += Math.min(20, (20 * (6 - row)));
    //            }
    //
    //            // backwards
    //            boolean backwards =
    //                    (attackMasks.getPawnAttacks(
    //                                            position.sideToMove,
    //                                            pawnSquare
    //                                                    +
    // Position.pawnDirections[position.sideToMove])
    //                                    & position.pieceBitboards[1 - position.sideToMove])
    //                            != 0;
    //            if (backwards && position.sideToMove == 0) {
    //                if (!isolated) {
    //                    for (int i = row; i < 7; i++) {
    //                        long currentRow = originalPawnBitboard & rowMasks[i];
    //                        if (column > 0)
    //                            backwards = backwards && ((columnMasks[column - 1] & currentRow)
    // == 0);
    //                        if (column < 7)
    //                            backwards = backwards && ((columnMasks[column + 1] & currentRow)
    // == 0);
    //
    //                        if (!backwards) break;
    //                    }
    //                }
    //            } else if (backwards && position.sideToMove == 1) {
    //                if (!isolated) {
    //                    for (int i = row; i > 0; i--) {
    //                        long currentRow = originalPawnBitboard & rowMasks[i];
    //                        if (column > 0)
    //                            backwards = backwards && ((columnMasks[column - 1] & currentRow)
    // == 0);
    //                        if (column < 7)
    //                            backwards = backwards && ((columnMasks[column + 1] & currentRow)
    // == 0);
    //
    //                        if (!backwards) break;
    //                    }
    //                }
    //            }
    //
    //            if (backwards) extraScore -= 30;
    //        }
    //
    //        setPawnEvaluation(pHash, extraScore);
    //
    //        return extraScore;
    //    }
    //
    //    public int evaluateKnights(Position position) {
    //        int score = 0;
    //
    //        long knightsBB = position.pieceBitboards[2 + position.sideToMove];
    //        int knights = countBits(knightsBB);
    //
    //        // Lower value as pawns start to go away
    //        int pawns = countBits(position.pieceBitboards[0] | position.pieceBitboards[1]);
    //        score -= (5 * knights) * Math.min(0, 12 - pawns);
    //
    //        while (knightsBB != 0) {
    //            int square = getLSBIndex(knightsBB);
    //            knightsBB = popBit(knightsBB, square);
    //            long opponentPawns = position.pieceBitboards[1 - position.sideToMove];
    //            long myPawns = position.pieceBitboards[position.sideToMove];
    //
    //            if (square == 42 && position.sideToMove == 0) {
    //                if (getBit(myPawns, 35) && getBit(myPawns, 50) && !getBit(myPawns, 36))
    //                    score -= 40;
    //            } else if (square == 18 && position.sideToMove == 1) {
    //                if (getBit(myPawns, 27) && getBit(myPawns, 10) && !getBit(myPawns, 28))
    //                    score -= 40;
    //            }
    //
    //            long knightAttacks = masks.getKnightAttacks(square);
    //            int squares = countBits(knightAttacks);
    //
    //            int pawnControlled = 0;
    //            while (knightAttacks != 0) {
    //                int attackedSquare = getLSBIndex(knightAttacks);
    //                knightAttacks = popBit(knightAttacks, attackedSquare);
    //
    //                if ((masks.getPawnAttacks(position.sideToMove, attackedSquare) &
    // opponentPawns) != 0)
    //                    pawnControlled++;
    //            }
    //
    //            int deduction = 48 / squares;
    //
    //            score -= (deduction * pawnControlled);
    //
    //            if ((masks.getPawnAttacks(1 - position.sideToMove, square) & myPawns) != 0)
    //                score += 10;
    //        }
    //
    //        return score;
    //    }
    //
    //    public int evaluateBishops(Position position) {
    //        int score = 0;
    //        long bishopBitboard = position.pieceBitboards[4 + position.sideToMove];
    //        long opponentBishops = position.pieceBitboards[5 - position.sideToMove];
    //        long pawnsBitboard = position.pieceBitboards[position.sideToMove];
    //
    //        int bishopCount = countBits(bishopBitboard);
    //        int opponentBishopCount = countBits(opponentBishops);
    //
    //        if (bishopCount == 2 && opponentBishopCount < 2) score += 75;
    //
    //        while (bishopBitboard != 0) {
    //            int square = getLSBIndex(bishopBitboard);
    //            bishopBitboard = popBit(bishopBitboard, square);
    //
    //            long bishopAttacks = masks.getBishopAttacks(square, pawnsBitboard);
    //            int movement = countBits(bishopAttacks);
    //
    //            if (movement < 4) score -= 100;
    //        }
    //
    //        return score;
    //    }

    public void clearPawnTT() {
        Arrays.fill(pawnEvaluations, Integer.MAX_VALUE);
    }

    public int positionEvaluation(Position position) {
        int score = 0;

        GameStage stage = position.getGameStage();

        for (int i = position.sideToMove; i < 12; i += 2) {
            long bb = position.pieceBitboards[i];
            while (bb != 0) {
                int sq = getLSBIndex(bb);
                bb = popBit(bb, sq);

                score += Piece.getPieceValue(i, stage) + Piece.getPositionValue(i, sq, stage);
            }
        }

        for (int i = 1 - position.sideToMove; i < 12; i += 2) {
            long bb = position.pieceBitboards[i];
            while (bb != 0) {
                int sq = getLSBIndex(bb);
                bb = popBit(bb, sq);

                score -= Piece.getPieceValue(i, stage) + Piece.getPositionValue(i, sq, stage);
            }
        }

        return score;
    }
}
