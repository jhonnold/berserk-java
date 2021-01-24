package me.honnold.position;

public class CastlingRights {
    private final boolean eastSide;
    private final boolean westSide;

    public CastlingRights(boolean eastSide, boolean westSide) {
        this.eastSide = eastSide;
        this.westSide = westSide;
    }

    public boolean canEastSide() {
        return this.eastSide;
    }

    public boolean canWestSide() {
        return this.westSide;
    }

    public CastlingRights copy() {
        return new CastlingRights(this.eastSide, this.westSide);
    }
}
