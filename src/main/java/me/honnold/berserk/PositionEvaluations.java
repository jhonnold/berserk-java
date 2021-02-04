package me.honnold.berserk;

import java.util.Arrays;

import static me.honnold.berserk.BoardUtils.*;

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
    private final int[] pawnEvaluations = new int[PAWN_TABLE_SIZE];

    private PositionEvaluations() {
        Arrays.fill(pawnEvaluations, Integer.MAX_VALUE);
    }

    public static PositionEvaluations getInstance() {
        return POSITION_EVALUATION;
    }

    private int getPawnTableIndex(long pHash) {
        int modulo = (int) (pHash % PAWN_TABLE_SIZE);

        return modulo < 0 ? modulo + PAWN_TABLE_SIZE : modulo;
    }

    public void setPawnEvaluation(long pHash, int value) {
        int idx = getPawnTableIndex(pHash);

        pawnEvaluations[idx] = value;
    }

    public int getPawnEvaluation(long pHash) {
        return pawnEvaluations[getPawnTableIndex(pHash)];
    }

    public int evaluatePawnStructure(Position position) {
        long pHash = position.getPawnHash();

        int storedEvaluation = getPawnEvaluation(pHash);
        if (storedEvaluation != Integer.MAX_VALUE) return storedEvaluation;

        int extraScore = 0;

        long pawnBitboard = position.pieceBitboards[position.sideToMove];
        long originalPawnBitboard = pawnBitboard;
        while (pawnBitboard != 0) {
            long opponentPawnBitboard = position.pieceBitboards[1 - position.sideToMove];

            int pawnSquare = getLSBIndex(pawnBitboard);
            pawnBitboard = popBit(pawnBitboard, pawnSquare);

            int column = pawnSquare % 8;
            int row = pawnSquare / 8;

            boolean isolated = true;
            if (column > 0)
                isolated = ((columnMasks[column - 1] & originalPawnBitboard) == 0);
            if (column < 7)
                isolated = isolated && ((columnMasks[column + 1] & originalPawnBitboard) == 0);

            if (isolated) extraScore -= 40;

            boolean doubled = countBits(columnMasks[column] & originalPawnBitboard) > 1;
            if (doubled) extraScore -= 20;

            boolean passed = true;
            if (position.sideToMove == 0) {
                for (int i = 1; i < row; i++) {
                    long opponentRow = opponentPawnBitboard & rowMasks[i];
                    if (opponentRow == 0) continue;

                    passed = ((opponentRow & columnMasks[column]) == 0);
                    if (column > 0)
                        passed = passed && ((columnMasks[column - 1] & opponentRow) == 0);
                    if (column < 7)
                        passed = passed && ((columnMasks[column + 1] & opponentRow) == 0);

                    if (!passed) break;
                }
            } else {
                for (int i = 7; i > row; i--) {
                    long opponentRow = opponentPawnBitboard & rowMasks[i];
                    if (opponentRow == 0) continue;

                    passed = ((opponentRow & columnMasks[column]) == 0);
                    if (column > 0)
                        passed = passed && ((columnMasks[column - 1] & opponentRow) == 0);
                    if (column < 7)
                        passed = passed && ((columnMasks[column + 1] & opponentRow) == 0);

                    if (!passed) break;
                }
            }

            if (passed) extraScore += 50;
        }

        setPawnEvaluation(pHash, extraScore);

        return extraScore;
    }

    public void clearPawnTT() {
        Arrays.fill(pawnEvaluations, Integer.MAX_VALUE);
    }

    public int positionEvaluation(Position position) {
        boolean isEndgame = position.isEndgame();
        int[][] squareValues = isEndgame ? Piece.endgameSquareValues : Piece.squareValues;

        int score = 0;

        for (int i = position.sideToMove; i < 12; i += 2) {
            long bb = position.pieceBitboards[i];
            while (bb != 0) {
                int sq = getLSBIndex(bb);
                bb = popBit(bb, sq);

                score += (Piece.baseValues[i >> 1] + squareValues[i][sq]);
            }
        }

        for (int i = 1 - position.sideToMove; i < 12; i += 2) {
            long bb = position.pieceBitboards[i];
            while (bb != 0) {
                int sq = getLSBIndex(bb);
                bb = popBit(bb, sq);

                score -= (Piece.baseValues[i >> 1] + squareValues[i][sq]);
            }
        }

        score += this.evaluatePawnStructure(position);

        return score;
    }
}
