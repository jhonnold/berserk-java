package me.honnold.berserk.eval;

import static me.honnold.berserk.util.BBUtils.*;

import me.honnold.berserk.board.GameStage;
import me.honnold.berserk.board.Piece;
import me.honnold.berserk.board.Position;

public class PositionEvaluations {
    private static final int EVAL_TABLE_SIZE = 10_000_000;
    private static final PositionEvaluations POSITION_EVALUATION = new PositionEvaluations();
    private final int[] evaluations = new int[EVAL_TABLE_SIZE];
    private final long[] hashes = new long[EVAL_TABLE_SIZE];

    private PositionEvaluations() {}

    public static PositionEvaluations getInstance() {
        return POSITION_EVALUATION;
    }

    public int positionEvaluation(Position position) {
        int idx = getEvalTableIdx(position);

        int determined = evaluations[idx];
        long hash = hashes[idx];
        if (hash == position.zHash) return determined;

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

        for (int i = 1 - position.sideToMove; i < 12; i += 2) {
            long bb = position.pieceBitboards[i];
            while (bb != 0) {
                int sq = getLSBIndex(bb);
                bb = popLSB(bb);

                score -= Piece.getPieceValue(i, stage) + Piece.getPositionValue(i, sq, stage);
            }
        }

        evaluations[idx] = score;
        hashes[idx] = position.zHash;
        return score;
    }

    private int getEvalTableIdx(Position position) {
        int modulo = (int) (position.zHash % EVAL_TABLE_SIZE);

        return modulo < 0 ? modulo + EVAL_TABLE_SIZE : modulo;
    }
}
