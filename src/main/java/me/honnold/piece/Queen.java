package me.honnold.piece;

public class Queen implements Piece {
    private static final boolean ray = true;
    private static final int[] directions = new int[]{-10, 1, 10, -1, -9, 11, 9, -11};
    private static final int[] values;

    static {
        int[] squareValues = {
                6, 1, -8, -104, 69, 24, 88, 26,
                14, 32, 60, -10, 20, 76, 57, 24,
                -2, 43, 32, 60, 72, 63, 43, 2,
                1, -16, 22, 17, 25, 20, -13, -6,
                -14, -15, -2, -5, -1, -10, -20, -22,
                -30, -6, -13, -11, -16, -11, -16, -27,
                -36, -18, 0, -19, -15, -15, -21, -38,
                -39, -30, -31, -13, -31, -36, -34, -42
        };

        values = new int[120];
        for (int i = 0; i < 120; i++) {
            if (i < 21 || i > 98) {
                values[i] = 0;
            } else if (i % 10 == 0 || i % 10 == 9) {
                values[i] = 0;
            } else {
                int rank = (i - 21) / 10;
                values[i] = 929 + squareValues[rank * 8 + ((i - 1) % 10)];
            }
        }
    }

    private final Color color;

    public Queen(Color color) {
        this.color = color;
    }

    @Override
    public boolean isRay() {
        return ray;
    }

    @Override
    public int[] getDirections() {
        return directions;
    }

    @Override
    public int[] getValues() {
        return values;
    }

    @Override
    public Color getColor() {
        return this.color;
    }

    @Override
    public String toString() {
        return color == Color.WHITE ? "Q" : "q";
    }
}
