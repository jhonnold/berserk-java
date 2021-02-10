package me.honnold.berserk.eval;

import me.honnold.berserk.board.GameStage;
import me.honnold.berserk.board.Piece;
import me.honnold.berserk.board.Position;
import me.honnold.berserk.moves.AttackMasks;

import static me.honnold.berserk.util.BBUtils.*;

public class PositionEvaluations {
    private static final PositionEvaluations POSITION_EVALUATION = new PositionEvaluations();
    private final static int DOUBLED_PAWN = 10;
    private final static int ISOLATED_PAWN = 20;
    private final static int BACKWARDS_PAWN = 10;
    private final static int PASSED_PAWN = 20;
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
        int idx = getEvalTableIdx(position.zHash);

        if (evaluations[idx] == (int) position.zHash) return evaluations[idx + 1];

        int score = this.myPieceValue(position);
        score -= this.opponentPieceValue(position);

        score += this.pawnsEval(position, position.sideToMove);
        score -= this.pawnsEval(position, 1 ^ position.sideToMove);

        idx = getEvalTableIdx(position.zHash);
        evaluations[idx] = (int) position.zHash;
        evaluations[idx + 1] = score;

        return score;
    }

    private int getEvalTableIdx(long hash) {
        return (int) (hash >>> shifts) << 1;
    }

    private int myPieceValue(Position position) {
        int score = 0;

        GameStage stage = position.getGameStage();

        for (int i = position.sideToMove; i < 12; i += 2) {
            long bb = position.pieceBitboards[i];
            while (bb != 0) {
                int sq = getLSBIndex(bb);
                bb = popLSB(bb);

                score += Piece.getPieceValue(i, stage) + Piece.getPositionValue(i, sq, stage);
            }
        }

        return score;
    }

    private int opponentPieceValue(Position position) {
        int score = 0;
        GameStage stage = position.getGameStage();

        for (int i = 1 - position.sideToMove; i < 12; i += 2) {
            long bb = position.pieceBitboards[i];
            while (bb != 0) {
                int sq = getLSBIndex(bb);
                bb = popLSB(bb);

                score += Piece.getPieceValue(i, stage) + Piece.getPositionValue(i, sq, stage);
            }
        }

        return score;
    }

    private int pawnsEval(Position position, int side) {
        int score = 0;

        int xside = side ^ 1;
        long myPawns = position.pieceBitboards[side],
                originalPawns = myPawns;
        long opponentPawns = position.pieceBitboards[xside];
        int pawnDirection = pawnDirections[side];

        while (myPawns != 0) {
            int sq = getLSBIndex(myPawns);
            int col = sq % 8;
            int row = sq / 8;

            myPawns = popLSB(myPawns);

            if ((myPawns & (1L << (sq - pawnDirection))) != 0)
                score -= DOUBLED_PAWN;

            long futureRanks = 0;
            if (side == 0)
                for (int i = row - 1; i >= 0; i--) futureRanks |= masks.rowMasks[i];
            else
                for (int i = row + 1; i <= 7; i++) futureRanks |= masks.rowMasks[i];

            if ((originalPawns & masks.getColumnMask(col - 1)) == 0 && (originalPawns & masks.getColumnMask(col + 1)) == 0)
                score -= ISOLATED_PAWN;
            else {
                long previousRanks = ~futureRanks;
                if ((previousRanks & masks.getColumnMask(col - 1) & originalPawns) == 0
                        && (previousRanks & masks.getColumnMask(col + 1) & originalPawns) == 0) {
                    score -= BACKWARDS_PAWN;
                }
            }

            if ((opponentPawns & futureRanks & masks.getColumnMask(col)) == 0
                    && (opponentPawns & futureRanks & masks.getColumnMask(col - 1)) == 0
                    && (opponentPawns & futureRanks & masks.getColumnMask(col + 1)) == 0
            ) {
                if (side == 0)
                    score += (7 - row) * PASSED_PAWN;
                else
                    score += row * PASSED_PAWN;
            }
        }

        return score;
    }
}
