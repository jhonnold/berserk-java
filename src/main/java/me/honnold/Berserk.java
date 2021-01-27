package me.honnold;

import me.honnold.piece.Color;
import me.honnold.position.Move;
import me.honnold.position.Position;
import me.honnold.util.FEN;
import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Berserk {
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
                        Pair<Integer, Integer> enteredPair = FEN.getUserMove(playingWhite, input);

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
                    System.out.println("My move: " + FEN.convertIdxToSquare(moveResult.getLeft().getStart(), !playingWhite) + FEN.convertIdxToSquare(moveResult.getLeft().getEnd(), !playingWhite));
                    System.out.println();

                    position = position.move(moveResult.getLeft());
                    history.add(position);

                    if (moveResult.getRight() == 69290) {
                        System.out.println(position);
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

    public Position getInitialPosition() {
//        return FEN.getInit();
        return FEN.toPosition("r3kr2/p1q2p2/2p1bP2/1p4Q1/2p5/6pP/P5B1/3RR2K w q - 0 1");
    }
}
