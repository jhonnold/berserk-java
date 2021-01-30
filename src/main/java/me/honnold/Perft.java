package me.honnold;

import me.honnold.position.Move;
import me.honnold.position.Position;
import me.honnold.util.FEN;

import java.util.List;

public class Perft {
    public static long perft(int depth) {
        return perft(depth, FEN.getInit());
    }

    public static long perft(int depth, Position p) {
        long moveCount = 0;

        if (depth == 0)
            return 1L;

        List<Move> moves = p.generateMoves();
        for (Move m : moves) {
            Position next = p.move(m);
            if (!next.canCaptureKing())
                moveCount += perft(depth - 1, next);
        }

        return moveCount;
    }
}
