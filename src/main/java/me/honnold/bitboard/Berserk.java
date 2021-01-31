package me.honnold.bitboard;


import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Scanner;

public class Berserk {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        // force load
        AttackMasks.getQueenAttacks(0, 0);
        long castleKey = ZobristHash.castleKeys[0];

        SearchEngine engine = new SearchEngine();

        Position p = new Position("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        while (true) {
            String line = in.nextLine();

            if ("uci".equals(line)) {
                println("id name berserk");
                println("id author jhonnold");
                println("uciok");
            } else if ("isready".equals(line)) {
                println("readyok");
            } else if ("quit".equals(line)) {
                break;
            } else if ("print".equals(line)) {
                println(p);
            } else if ("perft".equals(line)) {
                Perft.runPerft("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -", 5);
            } else if (line.startsWith("position")) {
                line = line.substring(9).concat(" ");
                if (line.contains("startpos ")) {
                    line = line.substring(9);
                    p = new Position("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
                } else if (line.contains("fen")) {
                    line = line.substring(4);
                    p = new Position(line);
                }

                if (line.contains("moves")) {
                    line = line.substring(line.indexOf("moves") + 6);

                    String[] gameMoves = line.split("\\s+");
                    for (String moveString : gameMoves) {
                        moveString = moveString.toLowerCase();

                        int start = ArrayUtils.indexOf(Position.squares, moveString.substring(0, 2));
                        int end = ArrayUtils.indexOf(Position.squares, moveString.substring(2, 4));
                        int promotionPiece = -1;

                        if (moveString.length() == 5) {
                            promotionPiece = ArrayUtils.indexOf(Position.pieceSymbols, moveString.charAt(4));
                            promotionPiece -= (1 - p.sideToMove);
                        }

                        Move foundMove = null;
                        Iterable<Move> positionMoves = p.getMoves();
                        for (Move m : positionMoves) {
                            if (m.start == start && m.end == end && m.promotionPiece == promotionPiece) {
                                foundMove = m;
                                break;
                            }
                        }

                        if (foundMove == null) throw new RuntimeException("Move not found! " + moveString);
                        p.makeMove(foundMove);
                    }
                }
            } else if (line.startsWith("go")) {
                Pair<Move, Integer> bestMoveResult = engine.bestMove(p);
                Move m = bestMoveResult.getLeft();
//                int score = bestMoveResult.getRight();

                System.out.printf("bestmove %s%n", m);
            }
        }

        in.close();
    }

    private static void println(Object o) {
        System.out.println(o);
    }
}
