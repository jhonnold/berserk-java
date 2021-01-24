package me.honnold.piece;

public class King implements Piece {
    private static final boolean ray = false;
    private static final int[] directions = new int[]{-10, 1, 10, -1, -9, 11, 9, -11};
    private static final int[] values;

    static {
        int[] squareValues = {
                4, 54, 47, -99, -99, 60, 83, -62,
                -32, 10, 55, 56, 56, 55, 10, 3,
                -62, 12, -57, 44, -67, 28, 37, -31,
                -55, 50, 11, -4, -19, 13, 0, -49,
                -55, -43, -52, -28, -51, -47, -8, -50,
                -47, -42, -43, -79, -64, -32, -29, -32,
                -4, 3, -14, -50, -57, -18, 13, 4,
                17, 30, -3, -14, 6, -1, 40, 18
        };

        values = new int[120];
        for (int i = 0; i < 120; i++) {
            if (i < 21 || i > 98) {
                values[i] = 0;
            } else if (i % 10 == 0 || i % 10 == 9) {
                values[i] = 0;
            } else {
                int rank = (i - 21) / 10;
                values[i] = 60000 + squareValues[rank * 8 + ((i - 1) % 10)];
            }
        }
    }

    private final Color color;

    public King(Color color) {
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
        return color == Color.WHITE ? "K" : "k";
    }
}
