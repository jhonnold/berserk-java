package me.honnold;

import me.honnold.piece.Color;
import me.honnold.position.Move;
import me.honnold.position.Position;
import me.honnold.tt.ZobristHash;
import me.honnold.util.FEN;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

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
        try {
            fh = new FileHandler("C:/temp/berserk/out.log");
            LOGGER.addHandler(fh);

            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
                Pair<Move, Integer> result = engine.bestMove(p);
                Move move = result.getLeft();

                boolean asWhite = p.getMoving() == Color.WHITE;

                println("bestmove " + FEN.convertIdxToSquare(move.getStart(), asWhite) + FEN.convertIdxToSquare(move.getEnd(), asWhite));
            }
        }

        if (fh != null) fh.close();
        in.close();
    }
}
