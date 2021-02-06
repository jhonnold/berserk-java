package me.honnold.berserk;

import java.util.List;
import java.util.Scanner;
import me.honnold.berserk.board.Position;
import me.honnold.berserk.moves.Move;
import me.honnold.berserk.moves.MoveGenerator;
import me.honnold.berserk.search.PVS;
import me.honnold.berserk.search.Repetitions;
import me.honnold.berserk.util.Perft;
import me.honnold.berserk.util.TimeManager;
import org.apache.commons.lang3.ArrayUtils;

public class Berserk {
    private final Repetitions repetitions = Repetitions.getInstance();
    private final MoveGenerator moveGenerator = MoveGenerator.getInstance();
    private Position currentPosition =
            new Position("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    private PVS search;
    private TimeManager timeManager;

    public static void main(String[] args) {
        Berserk berserk = new Berserk();

        berserk.run();
    }

    public void run() {
        printUci();

        Scanner in = new Scanner(System.in);

        while (true) {
            String line = in.nextLine();
            String[] tokens = line.split("\\s+");

            switch (tokens[0]) {
                case "uci":
                    printUci();
                    break;
                case "isready":
                    println("readyok");
                    break;
                case "ucinewgame":
                    repetitions.clearPreviousPositions();
                    break;
                case "quit":
                    System.exit(0);
                    break;
                case "stop":
                    timeManager.stop();
                    search.stop();
                    break;
                case "position":
                    position(tokens);
                    break;
                case "go":
                    go(tokens);
                    break;
                case "perft":
                    Perft.runPerft("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 6);
                    break;
            }
        }
    }

    private void println(Object o) {
        System.out.println(o);
    }

    private void go(String[] tokens) {
        int timeToUse = Integer.MAX_VALUE;

        int infinite = ArrayUtils.indexOf(tokens, "infinite");
        if (infinite == -1) {
            // TODO: Come up with something better for timing
            int incIdx =
                    ArrayUtils.indexOf(tokens, currentPosition.sideToMove == 0 ? "winc" : "binc")
                            + 1;
            int timeIdx =
                    ArrayUtils.indexOf(tokens, currentPosition.sideToMove == 0 ? "wtime" : "btime")
                            + 1;

            if (timeIdx > 0) {
                int time = Integer.parseInt(tokens[timeIdx]);
                boolean hasInc = incIdx > 0 && !"0".equals(tokens[incIdx]);

                int movesToGoIdx = ArrayUtils.indexOf(tokens, "movestogo") + 1;
                int divisor =
                        movesToGoIdx >= 0
                                ? Math.max(2, Integer.parseInt(tokens[movesToGoIdx]))
                                : (hasInc ? 10 : 40);

                timeToUse = time / divisor;
                if (timeToUse > 60000) timeToUse = 60000;
                if (timeToUse < 150) timeToUse = 150;

                if (incIdx > 0) timeToUse += (Integer.parseInt(tokens[incIdx]) / 2);
            }
        }

        this.searchForTime(currentPosition, timeToUse);
    }

    public Thread searchForTime(Position position, int time) {
        Thread runner =
                new Thread(
                        () -> {
                            search = new PVS(position);
                            timeManager = new TimeManager(search, time);

                            Thread searchThread = new Thread(search);
                            Thread timeThread = new Thread(timeManager);

                            searchThread.start();
                            timeThread.start();

                            try {
                                searchThread.join();
                                timeThread.join();
                            } catch (InterruptedException ignored) {
                            } finally {
                                System.out.println("bestmove " + search.getResults().getBestMove());
                            }
                        });

        runner.start();
        return runner;
    }

    private void position(String[] tokens) {
        repetitions.clearPreviousPositions();

        if (tokens[1].equals("startpos")) {
            currentPosition =
                    new Position("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        } else if (tokens[1].equals("fen")) {
            StringBuilder fen = new StringBuilder();
            for (int i = 2; i < tokens.length; i++) fen.append(tokens[i]).append(" ");

            currentPosition = new Position(fen.toString());
        }

        // TODO: Handle moves after FEN
        if (tokens.length < 3 || !tokens[2].equals("moves")) return;

        for (int i = 3; i < tokens.length; i++) {
            String moveString = tokens[i].toLowerCase();

            int start = ArrayUtils.indexOf(Position.squares, moveString.substring(0, 2));
            int end = ArrayUtils.indexOf(Position.squares, moveString.substring(2, 4));
            int promotionPiece = -1;

            if (moveString.length() == 5) {
                promotionPiece = ArrayUtils.indexOf(Position.pieceSymbols, moveString.charAt(4));
                promotionPiece -= (1 - currentPosition.sideToMove);
            }

            Move foundMove = null;
            List<Move> positionMoves = moveGenerator.getAllMoves(currentPosition);
            for (Move m : positionMoves) {
                if (m.start == start && m.end == end && m.promotionPiece == promotionPiece) {
                    foundMove = m;
                    break;
                }
            }

            if (foundMove == null) throw new RuntimeException("Move not found! " + moveString);
            currentPosition.makeMove(foundMove);
            repetitions.add(currentPosition.zHash);
        }
    }

    private void printUci() {
        println("id name berserk");
        println("id author jhonnold");
        println("uciok");
    }

    public PVS.Results getSearchResults() {
        return search.getResults();
    }
}
