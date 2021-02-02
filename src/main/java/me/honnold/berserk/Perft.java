package me.honnold.berserk;

public class Perft {
    public static long runPerft(String fen, int depth) {
        if (depth == 0) return 1;

        Position startingPos = new Position(fen);

        Perft perft = new Perft();

        long result = 0;

        long start = System.nanoTime();
        for (Move m : startingPos.getMoves()) {
            Position next = new Position(startingPos);
            boolean validMove = next.makeMove(m);

            if (validMove) {
                long nodes = perft.perft(next, depth - 1);
                System.out.printf("%s: %d%n", m, nodes);
                result += nodes;
            }

        }
        long end = System.nanoTime();

        System.out.printf("Result: %d\n", result);
        System.out.printf("Total duration (ms): %.2f\n", (end - start) / 1000000.0);
        return result;
    }

    public long perft(Position p, int depth) {
        long nodes = 0;

        if (depth == 0) return 1;

        for (Move m : p.getMoves()) {
            Position next = new Position(p);
            boolean validMove = next.makeMove(m);

            if (validMove) nodes += perft(next, depth - 1);
        }

        return nodes;
    }
}
