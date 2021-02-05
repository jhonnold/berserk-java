package me.honnold.berserk.tt;

public class Evaluation {
    private final long hash;
    private final int depth;
    private final int score;
    private final EvaluationFlag flag;

    public Evaluation(long hash, int depth, int score, EvaluationFlag flag) {
        this.hash = hash;
        this.depth = depth;
        this.score = score;
        this.flag = flag;
    }

    public long getHash() {
        return hash;
    }

    public int getDepth() {
        return depth;
    }

    public int getScore() {
        return score;
    }

    public EvaluationFlag getFlag() {
        return flag;
    }
}
