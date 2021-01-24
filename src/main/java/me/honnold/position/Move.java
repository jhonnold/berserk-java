package me.honnold.position;

public class Move {
    private final int start;
    private final int end;
    private final boolean isCapture;

    public Move(int start, int end, boolean isCapture) {
        this.start = start;
        this.end = end;
        this.isCapture = isCapture;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public boolean isCapture() {
        return isCapture;
    }

    @Override
    public String toString() {
        return "Move(" + this.start + ", " + this.end + ")";
    }
}
