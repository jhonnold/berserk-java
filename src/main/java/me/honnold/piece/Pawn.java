package me.honnold.piece;

public class Pawn implements Piece {
    private static final boolean ray = false;
    private static final int[] directions = new int[]{-20, -10, -11, -9};
    private static final int[] values;

    static {
        int[] squareValues = {
                0, 0, 0, 0, 0, 0, 0, 0,
                78, 83, 86, 73, 102, 82, 85, 90,
                7, 29, 21, 44, 40, 31, 44, 7,
                -17, 16, -2, 15, 14, 0, 15, -13,
                -26, 3, 10, 9, 6, 1, 0, -23,
                -22, 9, 5, -11, -10, -2, 3, -19,
                -31, 8, -7, -37, -36, -14, 3, -31,
                0, 0, 0, 0, 0, 0, 0, 0
        };

        values = new int[120];
        for (int i = 0; i < 120; i++) {
            if (i < 21 || i > 98) {
                values[i] = 0;
            } else if (i % 10 == 0 || i % 10 == 9) {
                values[i] = 0;
            } else {
                int rank = (i - 21) / 10;
                values[i] = 100 + squareValues[rank * 8 + ((i - 1) % 10)];
            }
        }
    }

    private final Color color;

    public Pawn(Color color) {
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
        return color == Color.WHITE ? "P" : "p";
    }
}
