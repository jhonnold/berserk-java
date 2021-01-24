package me.honnold.piece;

public interface Piece {
    boolean isRay();

    int[] getDirections();

    int[] getValues();

    Color getColor();
}