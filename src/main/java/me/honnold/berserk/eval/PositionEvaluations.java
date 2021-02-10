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
    private final static int BISHOP_PAIR = 35;
    private final static int KNIGHT_PAIR = 15;
    private final static int OPEN_FILE = 25;
    private final static int SEMI_OPEN_FILE = 15;
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

        int xside = 1 ^ position.sideToMove;

        int score = this.pieceValue(position, position.sideToMove);
        score -= this.pieceValue(position, xside);

        score += this.pawnsEval(position, position.sideToMove);
        score -= this.pawnsEval(position, xside);

        score += this.knightEval(position, position.sideToMove);
        score -= this.knightEval(position, xside);

        score += this.bishopEval(position, position.sideToMove);
        score -= this.bishopEval(position, xside);

        score += this.rookEval(position, position.sideToMove);
        score -= this.rookEval(position, xside);

        idx = getEvalTableIdx(position.zHash);
        evaluations[idx] = (int) position.zHash;
        evaluations[idx + 1] = score;

        return score;
    }

    private int getEvalTableIdx(long hash) {
        return (int) (hash >>> shifts) << 1;
    }

    private int pieceValue(Position position, int side) {
        int score = 0;

        GameStage stage = position.getGameStage();

        for (int i = side; i < 12; i += 2) {
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

    private int knightEval(Position position, int side) {
        int score = 0;

        long myKnights = position.pieceBitboards[side + 2];
        if (countBits(myKnights) > 1) {
            score -= KNIGHT_PAIR;
        }

        return score;
    }

    private int bishopEval(Position position, int side) {
        int score = 0;

        long myBishops = position.pieceBitboards[4 + side];
        if (countBits(myBishops) > 1) { // TODO: This check should include something to make sure they're different
            score += BISHOP_PAIR;
        }

        return score;
    }

    private int rookEval(Position position, int side) {
        int score = 0;

        long myRooks = position.pieceBitboards[6 + side];
        long myPawns = position.pieceBitboards[side];
        long opponentPawns = position.pieceBitboards[side];

        while (myRooks != 0) {
            int sq = getLSBIndex(myRooks);
            myRooks = popLSB(myRooks);

            int col = sq % 8;
            // we only want to do this calc once for doubled rooks (the removal of the rook will allow this to happen
            if (countBits(masks.getColumnMask(col) & myRooks) == 1) {
                if (((myPawns | opponentPawns) & masks.getColumnMask(col)) == 0) {
                    score += OPEN_FILE;
                } else if ((myPawns & masks.getColumnMask(col)) == 0) {
                    score += SEMI_OPEN_FILE;
                }
            }
        }

        return score;
    }
}
