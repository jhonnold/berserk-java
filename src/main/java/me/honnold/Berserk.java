package me.honnold;

import me.honnold.piece.*;
import me.honnold.position.CastlingRights;
import me.honnold.position.Move;
import me.honnold.position.Position;
import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Berserk {
    private static final Pattern movePattern = Pattern.compile("^([a-h])([1-8])([a-h])([1-8])$");

    public static void main(String[] args) {
        Berserk berserk = new Berserk();
        berserk.start();

    }

    public void start() {
        LinkedList<Position> history = new LinkedList<>();
        SearchEngine engine = new SearchEngine();
        Position position;

        Scanner in = new Scanner(System.in);

        while (true) {
            history.clear();
            position = getInitialPosition();

            System.out.print("Would you like to play white (Y/n)? ");
            boolean playingWhite = !in.nextLine().equalsIgnoreCase("n");

            history.add(position);

            while (true) {
                System.out.println(position);

                if ((position.getMoving() == Color.WHITE && playingWhite) || (position.getMoving() == Color.BLACK && !playingWhite)) {
                    System.out.print("Enter your move (a1b1): ");
                    String input = in.nextLine();
                    input = input.toLowerCase();

                    if (input.equals("resign")) {
                        System.out.println("Good game!");
                        break;
                    } else if (input.equals("rollback")) {
                        history.removeLast();
                        history.removeLast();
                        position = history.getLast();
                    } else {
                        List<Move> validMoves = position.generateMoves();
                        Pair<Integer, Integer> enteredPair = getUserMove(playingWhite, input);

                        if (enteredPair == null) {
                            System.out.println("Invalid input");
                            continue;
                        }

                        Move foundMove = validMoves.stream()
                                .filter(m -> m.getStart() == enteredPair.getLeft() && m.getEnd() == enteredPair.getRight())
                                .findFirst()
                                .orElse(null);

                        if (foundMove == null) {
                            System.out.println("Illegal move");
                            continue;
                        }

                        position = position.move(foundMove);
                        history.addLast(position);

                        System.out.println(position.rotate());
                        System.out.println(position.rotate().getScore());
                        System.out.println();

                        if (position.getScore() <= -50170) {
                            System.out.println("You win!");
                            break;
                        }
                    }
                } else {
                    long startTime = System.nanoTime();
                    Pair<Move, Integer> moveResult = engine.bestMove(position);
                    long endTime = System.nanoTime();

                    System.out.println();
                    System.out.println("Score: " + moveResult.getRight());
                    System.out.println("Nodes: " + engine.nodes);
                    System.out.println("Hits: " + engine.hits);
                    System.out.println("Duration: " + (endTime - startTime) / 1000000);
                    System.out.println();

                    position = position.move(moveResult.getLeft());
                    history.add(position);

                    if (moveResult.getRight() == 69290) {
                        System.out.println("Game over, I win!");
                        break;
                    }
                }
            }

            System.out.print("Would you like to play again (N/y)? ");
            boolean again = in.nextLine().equalsIgnoreCase("y");

            if (!again) break;
        }

        in.close();
    }

    public Pair<Integer, Integer> getUserMove(boolean playingWhite, String input) {
        Matcher match = movePattern.matcher(input);
        if (!match.matches()) return null;

        int start, end;
        if (playingWhite) {
            start = 101 + match.group(1).charAt(0) - 'a' - 10 * Integer.parseInt(match.group(2));
            end = 101 + match.group(3).charAt(0) - 'a' - 10 * Integer.parseInt(match.group(4));
        } else {
            start = 108 - match.group(1).charAt(0) + 'a' - 10 * (9 - Integer.parseInt(match.group(2)));
            end = 108 - match.group(3).charAt(0) + 'a' - 10 * (9 - Integer.parseInt(match.group(4)));

            System.out.println(start);
            System.out.println(end);
        }

        return Pair.of(start, end);
    }

    public Position getInitialPosition() {
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

//        pieces[21] = new Rook(Color.BLACK);
//        pieces[22] = new Knight(Color.BLACK);
//        pieces[23] = new Bishop(Color.BLACK);
//        pieces[24] = new Queen(Color.BLACK);
        pieces[25] = new King(Color.BLACK);
//        pieces[26] = new Bishop(Color.BLACK);
//        pieces[27] = new Knight(Color.BLACK);
//        pieces[28] = new Rook(Color.BLACK);
//        pieces[31] = new Pawn(Color.BLACK);
//        pieces[32] = new Pawn(Color.BLACK);
//        pieces[33] = new Pawn(Color.BLACK);
//        pieces[34] = new Pawn(Color.BLACK);
//        pieces[35] = new Pawn(Color.BLACK);
//        pieces[36] = new Pawn(Color.BLACK);
//        pieces[37] = new Pawn(Color.BLACK);
//        pieces[38] = new Pawn(Color.BLACK);

        return new Position(
                pieces,
                0,
                new CastlingRights(true, true),
                new CastlingRights(true, true),
                0,
                0,
                Color.WHITE
        );
    }
}
