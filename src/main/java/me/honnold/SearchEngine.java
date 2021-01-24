package me.honnold;

import me.honnold.position.Move;
import me.honnold.position.Position;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class SearchEngine {
    public int nodes = 0;

    private final PositionService service = new PositionService();

    public Pair<Move, Integer> getBestMove(Position position) {
        List<Move> validMoves = service.generateMoves(position);

        Move bestMove = null;
        int maxScore = -69290;

        for (Move m : validMoves) {
            Position movedPosition = position.move(m);
            int score = -1 * this.abMax(maxScore, 69290, 5, movedPosition);

            if (score > maxScore) {
                maxScore = score;
                bestMove = m;
            }
        }

        return Pair.of(bestMove, maxScore);
    }

    private int abMax(int a, int b, int left, Position position) {
        nodes++;

        if (left == 0) return position.getScore();

        for (Move m : service.generateMoves(position)) {
            int score = abMin(a, b, left - 1, position.move(m));
            if (score >= b) return b;
            if (score > a) a = score;
        }

        return a;
    }

    private int abMin(int a, int b, int left, Position position) {
        nodes++;

        if (left == 0) return -position.getScore();

        for (Move m : service.generateMoves(position)) {
            int score = abMax(a, b, left - 1, position.move(m));
            if (score <= a) return a;
            if (score < b) b = score;
        }

        return b;
    }
}
