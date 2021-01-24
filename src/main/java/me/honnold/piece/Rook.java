package me.honnold.piece;

public class Rook implements Piece {
    private static final boolean ray = true;
    private static final int[] directions = new int[]{-10, 1, 10, -1};
    private static final int[] values;

    static {
        int[] squareValues = {
                35, 29, 33, 4, 37, 33, 56, 50,
                55, 29, 56, 67, 55, 62, 34, 60,
                19, 35, 28, 33, 45, 27, 25, 15,
                0, 5, 16, 13, 18, -4, -9, -6,
                -28, -35, -16, -21, -13, -29, -46, -30,
                -42, -28, -42, -25, -25, -35, -26, -46,
                -53, -38, -31, -26, -29, -43, -44, -53,
                -30, -24, -18, 5, -2, -18, -31, -32
        };

        values = new int[120];
        for (int i = 0; i < 120; i++) {
            if (i < 21 || i > 98) {
                values[i] = 0;
            } else if (i % 10 == 0 || i % 10 == 9) {
                values[i] = 0;
            } else {
                int rank = (i - 21) / 10;
                values[i] = 479 + squareValues[rank * 8 + ((i - 1) % 10)];
            }
        }
    }

    private final Color color;

    public Rook(Color color) {
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
        return color == Color.WHITE ? "R" : "r";
    }
}
