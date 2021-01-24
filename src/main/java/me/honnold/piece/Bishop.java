package me.honnold.piece;

public class Bishop implements Piece {
    private static final boolean ray = true;
    private static final int[] directions = new int[]{-9, 11, 9, -11};
    private static final int[] values;

    static {
        int[] squareValues = {
                -59, -78, -82, -76, -23, -107, -37, -50,
                -11, 20, 35, -42, -39, 31, 2, -22,
                -9, 39, -32, 41, 52, -10, 28, -14,
                25, 17, 20, 34, 26, 25, 15, 10,
                13, 10, 17, 23, 17, 16, 0, 7,
                14, 25, 24, 15, 8, 25, 20, 15,
                19, 20, 11, 6, 7, 6, 20, 16,
                -7, 2, -15, -12, -14, -15, -10, -10
        };

        values = new int[120];
        for (int i = 0; i < 120; i++) {
            if (i < 21 || i > 98) {
                values[i] = 0;
            } else if (i % 10 == 0 || i % 10 == 9) {
                values[i] = 0;
            } else {
                int rank = (i - 21) / 10;
                values[i] = 320 + squareValues[rank * 8 + ((i - 1) % 10)];
            }
        }
    }

    private final Color color;

    public Bishop(Color color) {
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
        return color == Color.WHITE ? "B" : "b";
    }
}
