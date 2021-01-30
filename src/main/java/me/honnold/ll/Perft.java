package me.honnold.ll;

public class Perft {
    TranspositionTable table = new TranspositionTable();

    public long[] perft(int d, Board b) {
        long[] moveCount = new long[4];

        if (d == 0) return new long[] { 1, 0, 0, 0 };

        long startGen = System.nanoTime();
        int[][] moves = b.generateMoves();
        long endGen = System.nanoTime();

        moveCount[1] += endGen - startGen;

        for (int[] m : moves) {
            long startMove = System.nanoTime();
            b.move(m);
            long endMove = System.nanoTime();
            moveCount[2] += endMove - startMove;

            if (!b.isSquareAttackedBy(b.kings[1 - b.moving], b.moving)) {
                long[] perft = perft(d - 1, b);
                moveCount[0] += perft[0];
                moveCount[1] += perft[1];
                moveCount[2] += perft[2];
                moveCount[3] += perft[3];
            }

            long startUnmove = System.nanoTime();
            b.unmove(m);
            long endUnmove = System.nanoTime();

            moveCount[3] += endUnmove - startUnmove;
        }

        return moveCount;
    }
}
