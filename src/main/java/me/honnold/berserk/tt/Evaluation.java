package me.honnold.berserk.tt;

import me.honnold.berserk.eval.Constants;
import me.honnold.berserk.moves.Move;

public class Evaluation {
    private final long hash;
    private final int depth;
    private int score;
    private final EvaluationFlag flag;
    private final int move;

    public Evaluation(long hash, int depth, int score, EvaluationFlag flag, Move move) {
        this.hash = hash;
        this.depth = depth;
        this.score = score;
        this.flag = flag;
        this.move = move != null ? move.getRawData() : 0;
    }

    public long getHash() {
        return hash;
    }

    public int getDepth() {
        return depth;
    }

    public int getScore(int ply) {
        if (score > Constants.CHECKMATE_MIN) {
            score -= ply;
        } else if (score < -Constants.CHECKMATE_MIN) {
            score += ply;
        }

        return score;
    }

    public EvaluationFlag getFlag() {
        return flag;
    }

    public Move getMove() {
        return new Move(move);
    }
}
