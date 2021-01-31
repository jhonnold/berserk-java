package me.honnold;

import me.honnold.piece.Color;
import me.honnold.position.Move;
import me.honnold.position.Position;
import me.honnold.tt.ZobristHash;
import me.honnold.util.FEN;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class UCI {
    private static Logger LOGGER = Logger.getLogger(UCI.class.getName());

    static {
        LOGGER.setUseParentHandlers(false);
    }

    private static void println(Object o) {
        LOGGER.info("OUT: " + o);
        System.out.println(o);
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        FileHandler fh = null;
//        try {
//            fh = new FileHandler("C:/temp/berserk/out.log");
//            LOGGER.addHandler(fh);
//
//            SimpleFormatter formatter = new SimpleFormatter();
//            fh.setFormatter(formatter);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        Position p = FEN.getInit();
        SearchEngine engine = new SearchEngine();
        while (true) {
            String line = in.nextLine();

            LOGGER.info("IN: " + line);

            if ("uci".equals(line)) {
                println("id name berserk");
                println("id author jhonnold");
                println("uciok");
            } else if ("isready".equals(line)) {
                println("readyok");
            } else if ("quit".equals(line)) {
                break;
            } else if ("print".equals(line)) {
                println(p.toString());
                println("zHash: " + ZobristHash.hash(p));
            } else if (line.startsWith("position")) {
                line = line.substring(9).concat(" ");
                if (line.contains("startpos ")) {
                    line = line.substring(9);
                    p = FEN.getInit();
                } else if (line.contains("fen")) {
                    line = line.substring(4);
                    p = FEN.toPosition(line);
                }

                if (line.contains("moves")) {
                    line = line.substring(line.indexOf("moves") + 6);

                    int mover = p.getMoving() == Color.WHITE ? 1 : 0;
                    while (!line.isEmpty()) {
                        Pair<Integer, Integer> move = FEN.getUserMove(mover == 1, line.substring(0, 4));
                        Move foundMove = p.generateMoves().stream()
                                .filter(m -> m.getStart() == move.getLeft() && m.getEnd() == move.getRight())
                                .findFirst()
                                .orElse(null);


                        p = p.move(foundMove);

                        mover = 1 - mover;
                        line = line.substring(line.indexOf(' ') + 1);
                    }
                }
            } else if (line.startsWith("go")) {
                if (line.equals("go")) line += " ";

                line = line.substring(3);
                String[] goArgs = line.split("\\s+");

                int relevantArg;
                if ((relevantArg = ArrayUtils.indexOf(goArgs, "perft")) >= 0) {
                    int depth = Integer.parseInt(goArgs[relevantArg + 1]);

                    long startAll = System.nanoTime();
                    long totalNodes = 0;
                    Position init = FEN.getInit();
                    for (Move m : init.generateMoves()) {
                        long start = System.nanoTime();
                        long nodes = Perft.perft(depth - 1, init.move(m));
                        long end = System.nanoTime();

                        totalNodes += nodes;
                        println(FEN.moveToString(m, true) + " nodes " + nodes + " duration " + (end - start) / 1000000);
                    }

                    long endAll = System.nanoTime();
                    println("nodes " + totalNodes + " duration " + (endAll - startAll) / 1000000);
                } else {
                    String timeArg = p.getMoving() == Color.WHITE ? "wtime" : "btime";

                    int idx = ArrayUtils.indexOf(goArgs, timeArg);
                    int time = idx >= 0 ? Integer.parseInt(goArgs[idx + 1]) : 60000;

                    boolean infinite = ArrayUtils.indexOf(goArgs, "infinite") >= 0;

                    Pair<Move, Integer> result = engine.bestMove(p, time, infinite);
                    Move move = result.getLeft();

                    boolean asWhite = p.getMoving() == Color.WHITE;

                    println("bestmove " + FEN.convertIdxToSquare(move.getStart(), asWhite) + FEN.convertIdxToSquare(move.getEnd(), asWhite));
                }
            }
        }

        if (fh != null) fh.close();
        in.close();
    }
}
