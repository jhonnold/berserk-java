package me.honnold.position;

public class Move {
    private final int start;
    private final int end;

    public Move(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return "Move(" + this.start + ", " + this.end + ")";
    }
}
