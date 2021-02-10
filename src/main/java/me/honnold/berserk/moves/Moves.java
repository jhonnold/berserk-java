package me.honnold.berserk.moves;

import me.honnold.berserk.search.PVS;

public class Moves {
    private static final Moves singleton = new Moves();
    private final int[][] moves = new int[PVS.MAX_DEPTH][128];
    private final int[] moveCount = new int[PVS.MAX_DEPTH];
    private int ply = 0;

    private Moves() {}

    public static Moves getInstance() {
        return singleton;
    }

    public void setPly(int ply) {
        this.ply = ply;
        moveCount[this.ply] = 0;
    }

    public void add(int move) {
        moves[ply][moveCount[ply]] = move;
        moveCount[ply]++;
    }

    public int[] getRawMoves(int ply) {
        return this.moves[ply];
    }

    public void setMove(int ply, int idx, int move) {
        this.moves[ply][idx] = move;
    }

    public int getMove(int ply, int idx) {
        return this.moves[ply][idx];
    }

    public int getMoveCount(int ply) {
        return this.moveCount[ply];
    }
}
