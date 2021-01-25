package me.honnold;

import me.honnold.piece.*;
import me.honnold.position.CastlingRights;
import me.honnold.position.Move;
import me.honnold.position.Position;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Berserk {
    private static final Pattern p = Pattern.compile("^([a-h])([1-8])([a-h])([1-8])$");

    public static void main(String[] args) {
        List<Position> history = new ArrayList<>(1024);
        SearchEngine engine = new SearchEngine();

        Piece[] pieces = new Piece[120];
        pieces[91] = new Rook(Color.WHITE);
        pieces[92] = new Knight(Color.WHITE);
        pieces[93] = new Bishop(Color.WHITE);
        pieces[94] = new Queen(Color.WHITE);
        pieces[95] = new King(Color.WHITE);
        pieces[96] = new Bishop(Color.WHITE);
        pieces[97] = new Knight(Color.WHITE);
        pieces[98] = new Rook(Color.WHITE);
        pieces[81] = new Pawn(Color.WHITE);
        pieces[82] = new Pawn(Color.WHITE);
        pieces[83] = new Pawn(Color.WHITE);
        pieces[84] = new Pawn(Color.WHITE);
        pieces[85] = new Pawn(Color.WHITE);
        pieces[86] = new Pawn(Color.WHITE);
        pieces[87] = new Pawn(Color.WHITE);
        pieces[88] = new Pawn(Color.WHITE);

        pieces[21] = new Rook(Color.BLACK);
        pieces[22] = new Knight(Color.BLACK);
        pieces[23] = new Bishop(Color.BLACK);
        pieces[24] = new Queen(Color.BLACK);
        pieces[25] = new King(Color.BLACK);
        pieces[26] = new Bishop(Color.BLACK);
        pieces[27] = new Knight(Color.BLACK);
        pieces[28] = new Rook(Color.BLACK);
        pieces[31] = new Pawn(Color.BLACK);
        pieces[32] = new Pawn(Color.BLACK);
        pieces[33] = new Pawn(Color.BLACK);
        pieces[34] = new Pawn(Color.BLACK);
        pieces[35] = new Pawn(Color.BLACK);
        pieces[36] = new Pawn(Color.BLACK);
        pieces[37] = new Pawn(Color.BLACK);
        pieces[38] = new Pawn(Color.BLACK);

        Position position = new Position(
                pieces,
                0,
                new CastlingRights(true, true),
                new CastlingRights(true, true),
                0,
                0,
                Color.WHITE
        );

        Scanner in = new Scanner(System.in);

        history.add(position);

        while (true) {
            System.out.println(position);

            System.out.print("Enter your move: ");
            String input = in.nextLine();

            if (input.equals("end"))
                break;

            if (input.equals("rollback")) {
                history.remove(position);
                history.remove(history.size() - 1);
                position = history.get(history.size() - 1);
                continue;
            }

            Matcher match = p.matcher(input);
            if (!match.matches()) {
                System.out.println("Invalid input");
                continue;
            }

            int start = 101 + match.group(1).charAt(0) - 'a' - 10 * Integer.parseInt(match.group(2));
            int end = 101 + match.group(3).charAt(0) - 'a' - 10 * Integer.parseInt(match.group(4));

            List<Move> validMoves = position.generateMoves();

            Move move = validMoves.stream()
                    .filter(m -> m.getStart() == start && m.getEnd() == end)
                    .findFirst()
                    .orElse(null);

            if (move == null) {
                System.out.println("Invalid move!");
                continue;
            }

            position = position.move(move);

            history.add(position);

            System.out.println(position.rotate());

            long startTime = System.nanoTime();
            Pair<Move, Integer> moveResult = engine.bestMove(position);
            long endTime = System.nanoTime();

            System.out.println();
            System.out.println("Score: " + moveResult.getRight());
            System.out.println("Nodes: " + engine.nodes);
            System.out.println("Hits: " + engine.hits);
            System.out.println("Duration: " + (endTime - startTime) / 1000000);

            position = position.move(moveResult.getLeft());
            history.add(position);
        }

        in.close();
    }
}
