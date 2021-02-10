package me.honnold.berserk.util;

import me.honnold.berserk.board.Position;
import me.honnold.berserk.moves.Move;
import me.honnold.berserk.moves.MoveGenerator;
import me.honnold.berserk.moves.Moves;

public class Perft {
    private static final Moves moves = Moves.getInstance();
    private static final MoveGenerator moveGenerator = MoveGenerator.getInstance();

    public static long runPerft(String fen, int depth) {
        if (depth == 0) return 1;

        Position startingPos = new Position(fen);

        Perft perft = new Perft();

        long result = 0;

        long start = System.nanoTime();
        moveGenerator.addAllMoves(startingPos, depth);
        for (int i = 0; i < moves.getMoveCount(depth); i++) {
            int move = moves.getMove(depth, i);
            boolean validMove = startingPos.makeMove(move);

            if (validMove) {
                long nodes = perft.perft(startingPos, depth - 1);
                System.out.printf("%s: %d%n", Move.toString(move), nodes);
                result += nodes;
            }

            startingPos.undoMove(move);
        }
        long end = System.nanoTime();

        System.out.printf("Result: %d\n", result);
        System.out.printf("Total duration (ms): %.2f\n", (end - start) / 1000000.0);
        return result;
    }

    public long perft(Position position, int depth) {
        long nodes = 0;

        if (depth == 0) return 1;

        moveGenerator.addAllMoves(position, depth);
        for (int i = 0; i < moves.getMoveCount(depth); i++) {
            int move = moves.getMove(depth, i);
            boolean validMove = position.makeMove(move);

            if (validMove) nodes += perft(position, depth - 1);

            position.undoMove(move);
        }

        return nodes;
    }
}
