package me.honnold.berserk.util;

import me.honnold.berserk.board.Position;
import me.honnold.berserk.moves.Move;
import me.honnold.berserk.moves.MoveGenerator;

public class Perft {
    private static final MoveGenerator moveGenerator = MoveGenerator.getInstance();

    public static long runPerft(String fen, int depth) {
        if (depth == 0) return 1;

        Position startingPos = new Position(fen);

        Perft perft = new Perft();

        long result = 0;

        long start = System.nanoTime();
        for (Move m : moveGenerator.getAllMoves(startingPos)) {
            boolean validMove = startingPos.makeMove(m);

            if (validMove) {
                long nodes = perft.perft(startingPos, depth - 1);
                System.out.printf("%s: %d%n", m, nodes);
                result += nodes;
            }

            startingPos.undoMove(m);
        }
        long end = System.nanoTime();

        System.out.printf("Result: %d\n", result);
        System.out.printf("Total duration (ms): %.2f\n", (end - start) / 1000000.0);
        return result;
    }

    public long perft(Position position, int depth) {
        long nodes = 0;

        if (depth == 0) return 1;

        for (Move m : moveGenerator.getAllMoves(position)) {
            boolean validMove = position.makeMove(m);

            if (validMove) nodes += perft(position, depth - 1);

            position.undoMove(m);
        }

        return nodes;
    }
}
