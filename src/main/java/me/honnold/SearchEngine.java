package me.honnold;

import me.honnold.position.Move;
import me.honnold.position.Position;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.stream.Collectors;

public class SearchEngine {
    public int nodes = 0;

    public Pair<Move, Integer> getBestMove(Position position) {
        List<Move> validMoves = position.generateMoves();

        Move bestMove = null;
        int maxScore = -69290;

        for (Move m : validMoves) {
            Position movedPosition = position.move(m);
            int score = -1 * this.ab(-69290, 69290, 5, movedPosition);

            if (score > maxScore) {
                maxScore = score;
                bestMove = m;
            }
        }

        return Pair.of(bestMove, maxScore);
    }

    private int quiesce(int a, int b, Position position) {
        nodes++;

        int score = position.getScore();

        if (score >= b)
            return b;
        if (a < score)
            a = score;

        List<Move> moves = position.generateMoves();
        List<Move> captures = moves.stream().filter(Move::isCapture).collect(Collectors.toList());

        for (Move m : captures) {
            Position newPosition = position.move(m);
            score = -1 * quiesce(-b, -a, newPosition);

            if (score >= b)
                return b;
            if (score > a)
                a = score;
        }

        return a;
    }

    private int ab(int a, int b, int depthLeft, Position position) {
        nodes++;

        if (depthLeft == 0) return quiesce(a, b, position);

        List<Move> moves = position.generateMoves();
        for (Move m : moves) {
            int score = -1 * ab(-b, -a, depthLeft - 1, position.move(m));

            if (score >= b)
                return b;
            if (score > a)
                a = score;
        }

        return a;
    }
}
