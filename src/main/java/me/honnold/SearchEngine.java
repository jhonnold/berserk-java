package me.honnold;

import me.honnold.position.Move;
import me.honnold.position.Position;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.stream.Collectors;

public class SearchEngine {
    public int nodes = 0;

    private final PositionService service = new PositionService();

    public Pair<Move, Integer> getBestMove(Position position) {
        List<Move> validMoves = service.generateMoves(position);

        Move bestMove = null;
        int maxScore = -69290;

        for (Move m : validMoves) {
            Position movedPosition = position.move(m);
            int score = -1 * this.ab(maxScore, 69290, 5, movedPosition);

            if (score > maxScore) {
                maxScore = score;
                bestMove = m;
            }
        }

        return Pair.of(bestMove, maxScore);
    }

    private int quiesce(int a, int b, Position position) {
        int score = position.getScore();

        if (score >= b)
            return b;
        if (a < score)
            a = score;

        List<Move> moves = service.generateMoves(position);
        List<Move> captures = moves.stream().filter(Move::isCapture).collect(Collectors.toList());

        for (Move m : captures) {
            nodes++;
            Position newPosition = position.move(m);
            score = -1 * quiesce(-b, -a, newPosition);

            if (score >= b)
                return b;
            if (score > a)
                a = score;
        }

        return a;
    }

    private int ab(int a, int b, int depthLeft, Position p) {
        if (depthLeft == 0) return quiesce(a, b, p);

        for (Move m : service.generateMoves(p)) {
            nodes++;
            int score = -1 * ab(-b, -a, depthLeft - 1, p.move(m));

            if (score >= b)
                return b;
            if (score > a)
                a = score;
        }

        return a;
    }
}
