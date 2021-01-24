package me.honnold.piece;

public class Knight implements Piece {
    private static final boolean ray = false;
    private static final int[] directions = new int[]{-19, -8, 12, 21, 19, 8, -12, -21};
    private static final int[] values;

    static {
        int[] squareValues = {
                -66, -53, -75, -75, -10, -55, -58, -70,
                -3, -6, 100, -36, 4, 62, -4, -14,
                10, 67, 1, 74, 73, 27, 62, -2,
                24, 24, 45, 37, 33, 41, 25, 17,
                -1, 5, 31, 21, 22, 35, 2, 0,
                -18, 10, 13, 22, 18, 15, 11, -14,
                -23, -15, 2, 0, 2, 0, -23, -20,
                -74, -23, -26, -24, -19, -35, -22, -69
        };

        values = new int[120];
        for (int i = 0; i < 120; i++) {
            if (i < 21 || i > 98) {
                values[i] = 0;
            } else if (i % 10 == 0 || i % 10 == 9) {
                values[i] = 0;
            } else {
                int rank = (i - 21) / 10;
                values[i] = 280 + squareValues[rank * 8 + ((i - 1) % 10)];
            }
        }
    }

    private final Color color;

    public Knight(Color color) {
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
        return color == Color.WHITE ? "N" : "n";
    }
}
