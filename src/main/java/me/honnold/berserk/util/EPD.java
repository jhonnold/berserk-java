package me.honnold.berserk.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.honnold.berserk.board.Position;
import me.honnold.berserk.moves.Move;
import me.honnold.berserk.moves.MoveGenerator;
import me.honnold.berserk.moves.Moves;
import org.apache.commons.lang3.ArrayUtils;

public class EPD {
    private static final Moves moves = Moves.getInstance();
    private static final MoveGenerator moveGenerator = MoveGenerator.getInstance();

    public static List<EPDAnalysis> epdReader(String filename) throws IOException {
        File file = new File(EPD.class.getResource(filename).getFile());
        List<String> lines = Files.readAllLines(file.toPath());

        return lines.stream()
                .flatMap(
                        line -> {
                            String[] parts = line.split(";");
                            String[] fenParts = parts[0].split("\\s+");

                            boolean isBm = true;
                            int moveIdx = ArrayUtils.indexOf(fenParts, "bm");
                            if (moveIdx < 0) {
                                moveIdx = ArrayUtils.indexOf(fenParts, "am");
                                isBm = false;
                            }

                            if (moveIdx < 0) return Stream.empty();

                            StringBuilder fenBuilder = new StringBuilder();
                            for (int i = 0; i < moveIdx; i++) {
                                fenBuilder.append(fenParts[i]).append(" ");
                            }

                            String fen = fenBuilder.toString();

                            Position position = new Position(fen);

                            String move = fenParts[moveIdx + 1];
                            moveGenerator.addAllMoves(position, 0);
                            int bestMove = epdMoveToMove(move);
                            if (bestMove == 0) {
                                System.out.println("Unable to parse: " + move);
                                return Stream.empty();
                            }

                            return Stream.of(new EPDAnalysis(parts[1], position, bestMove, isBm));
                        })
                .collect(Collectors.toList());
    }

    private static int epdMoveToMove(String moveString) {
        moveString = moveString.replaceAll("x", "").replaceAll("\\+", "");

        if (moveString.equals("O-O")) {
            return Arrays.stream(moves.getRawMoves(0), 0, moves.getMoveCount(0))
                    .filter(move -> Move.getPieceIdx(move) >= 10)
                    .filter(move -> Move.getEnd(move) - Move.getStart(move) == 2)
                    .findFirst()
                    .orElse(0);
        } else if (moveString.equals("O-O-O")) {
            return Arrays.stream(moves.getRawMoves(0), 0, moves.getMoveCount(0))
                    .filter(move -> Move.getPieceIdx(move) >= 10)
                    .filter(move -> Move.getStart(move) - Move.getEnd(move) == 2)
                    .findFirst()
                    .orElse(0);
        }

        if (moveString.length() == 2) {
            int end = ArrayUtils.indexOf(BBUtils.squares, moveString);

            return Arrays.stream(moves.getRawMoves(0), 0, moves.getMoveCount(0))
                    .filter(move -> Move.getPieceIdx(move) <= 1)
                    .filter(move -> Move.getEnd(move) == end)
                    .findFirst()
                    .orElse(0);
        } else if (moveString.length() == 3 && moveString.charAt(0) > 96) {
            int end = ArrayUtils.indexOf(BBUtils.squares, moveString.substring(1));
            int column = moveString.charAt(0) - 'a';

            return Arrays.stream(moves.getRawMoves(0), 0, moves.getMoveCount(0))
                    .filter(move -> Move.getPieceIdx(move) <= 1)
                    .filter(move -> Move.getEnd(move) == end)
                    .filter(move -> Move.getStart(move) % 8 == column)
                    .findFirst()
                    .orElse(0);
        } else {
            char pieceChar = moveString.substring(0, 1).toUpperCase().charAt(0);
            if (moveString.length() == 3) {
                int end = ArrayUtils.indexOf(BBUtils.squares, moveString.substring(1));

                int piece = ArrayUtils.indexOf(BBUtils.pieceSymbols, pieceChar);

                return Arrays.stream(moves.getRawMoves(0), 0, moves.getMoveCount(0))
                        .filter(
                                move ->
                                        Move.getPieceIdx(move) == piece
                                                || Move.getPieceIdx(move) == piece + 1)
                        .filter(move -> Move.getEnd(move) == end)
                        .findFirst()
                        .orElse(0);
            } else if (moveString.length() == 4 && moveString.charAt(1) > 57) {
                int end = ArrayUtils.indexOf(BBUtils.squares, moveString.substring(2));
                int column = moveString.charAt(1) - 'a';

                int piece = ArrayUtils.indexOf(BBUtils.pieceSymbols, pieceChar);
                return Arrays.stream(moves.getRawMoves(0), 0, moves.getMoveCount(0))
                        .filter(
                                move ->
                                        Move.getPieceIdx(move) == piece
                                                || Move.getPieceIdx(move) == piece + 1)
                        .filter(move -> Move.getEnd(move) == end)
                        .filter(move -> Move.getStart(move) % 8 == column)
                        .findFirst()
                        .orElse(0);
            } else if (moveString.length() == 4) {
                int end = ArrayUtils.indexOf(BBUtils.squares, moveString.substring(2));
                int rank = Integer.parseInt(moveString.substring(1, 2));

                int piece = ArrayUtils.indexOf(BBUtils.pieceSymbols, pieceChar);
                return Arrays.stream(moves.getRawMoves(0), 0, moves.getMoveCount(0))
                        .filter(
                                move ->
                                        Move.getPieceIdx(move) == piece
                                                || Move.getPieceIdx(move) == piece + 1)
                        .filter(move -> Move.getEnd(move) == end)
                        .filter(move -> (8 - Move.getStart(move) / 8) == rank)
                        .findFirst()
                        .orElse(0);
            }
        }

        return 0;
    }

    public static class EPDAnalysis {
        public String id;
        public Position position;
        public int move;
        public boolean isBest;

        public EPDAnalysis(String id, Position position, int move, boolean isBest) {
            this.id = id.replace("id ", "");
            this.position = position;
            this.move = move;
            this.isBest = isBest;
        }

        @Override
        public String toString() {
            return "EPDAnalysis{" + "id='" + id + '\'' + '}';
        }
    }
}
