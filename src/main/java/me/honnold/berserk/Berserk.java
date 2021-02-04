package me.honnold.berserk;


import org.apache.commons.lang3.ArrayUtils;

import java.util.Scanner;

public class Berserk implements Runnable {
    private static final Object sync = new Object();
    private static final TranspositionTable transpositionTable = TranspositionTable.getInstance();
    private static final Repetitions repetitions = Repetitions.getInstance();
    private static final SearchEngine engine = new SearchEngine(transpositionTable, repetitions);
    private static Position currentPosition = new Position("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    private static boolean calculating = false;
    private final static Thread searchThread = new Thread(() -> {
        while (true) {
            try {
                synchronized (sync) {
                    while (!calculating) sync.wait();
                }

                engine.search(currentPosition);
                calculating = false;

                System.out.printf("bestmove %s%n", transpositionTable.getMoveForPosition(currentPosition));
            } catch (InterruptedException ignored) {
            }
        }
    });
    private static long endTime = Long.MAX_VALUE;
    private final static Thread timeThread = new Thread(() -> {
        while (true) {
            try {
                synchronized (sync) {
                    while (!calculating) sync.wait();
                }

                Thread.sleep(Math.max(50, endTime - System.currentTimeMillis()));
                if (engine.isRunning()) engine.interrupt();
            } catch (InterruptedException ignored) {
            }
        }
    });


    public static void main(String[] args) {
        // Force some static initialization
        // TODO: do this better?
        AttackMasks.getQueenAttacks(0, 0);
        ZobristHash.generate(currentPosition);

        Berserk berserk = new Berserk();

        searchThread.start();
        timeThread.start();

        Thread input = new Thread(berserk);
        input.start();

        try {
            searchThread.join();
            timeThread.join();
            input.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void println(Object o) {
        System.out.println(o);
    }

    @Override
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
                    transpositionTable.clear();
                    repetitions.clear();
                    break;
                case "quit":
                    System.exit(0);
                    break;
                case "stop":
                    if (engine.isRunning()) engine.interrupt();
                    break;
                case "position":
                    position(tokens);
                    break;
                case "go":
                    go(tokens);
                    break;
            }
        }
    }

    private void go(String[] tokens) {
        calculating = true;
        endTime = Long.MAX_VALUE;

        int infinite = ArrayUtils.indexOf(tokens, "infinite");
        if (infinite == -1) {
            // TODO: Come up with something better for timing
            int incIdx = ArrayUtils.indexOf(tokens, currentPosition.sideToMove == 0 ? "winc" : "binc") + 1;
            int timeIdx = ArrayUtils.indexOf(tokens, currentPosition.sideToMove == 0 ? "wtime" : "btime") + 1;

            if (timeIdx > 0) {
                int time = Integer.parseInt(tokens[timeIdx]);
                boolean hasInc = incIdx > 0 && !"0".equals(tokens[incIdx]);

                long timeToUse = time / (hasInc ? 10 : 40);
                if (timeToUse > 30000) timeToUse = 30000;
                if (timeToUse < 150) timeToUse = 150;

                endTime = System.currentTimeMillis() + timeToUse;
            }

            if (incIdx > 0) {
                int inc = Integer.parseInt(tokens[incIdx]);

                endTime += (inc / 2);
            }
        }

        synchronized (sync) {
            sync.notifyAll();
        }
    }

    private void position(String[] tokens) {
        repetitions.clear();

        if (tokens[1].equals("startpos")) {
            currentPosition = new Position("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        } else if (tokens[1].equals("fen")) {
            StringBuilder fen = new StringBuilder();
            for (int i = 2; i < tokens.length; i++)
                fen.append(tokens[i]).append(" ");

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
            Move[] positionMoves = currentPosition.getMoves();
            for (Move m : positionMoves) {
                if (m.start == start && m.end == end && m.promotionPiece == promotionPiece) {
                    foundMove = m;
                    break;
                }
            }

            if (foundMove == null) throw new RuntimeException("Move not found! " + moveString);
            currentPosition.makeMove(foundMove);
            repetitions.setCurrentPosition(currentPosition.zHash);
        }
    }

    private void printUci() {
        println("id name berserk");
        println("id author jhonnold");
        println("uciok");
    }
}
