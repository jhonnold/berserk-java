package me.honnold.berserk.eval;

import static me.honnold.berserk.util.BBUtils.getLSBIndex;
import static me.honnold.berserk.util.BBUtils.popLSB;

import me.honnold.berserk.board.GameStage;
import me.honnold.berserk.board.Piece;
import me.honnold.berserk.board.Position;

public class PositionEvaluations {
    private static final PositionEvaluations POSITION_EVALUATION = new PositionEvaluations();
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

        idx = getEvalTableIdx(position.zHash);
        evaluations[idx] = (int) position.zHash;
        evaluations[idx + 1] = score;

        return score;
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

    private int getEvalTableIdx(long hash) {
        return (int) (hash >>> shifts) << 1;
    }
}
