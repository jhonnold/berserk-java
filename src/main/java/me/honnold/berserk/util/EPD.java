package me.honnold.berserk.util;

import me.honnold.berserk.board.Position;
import me.honnold.berserk.moves.Move;
import me.honnold.berserk.moves.MoveGenerator;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EPD {
    private static MoveGenerator moveGenerator = MoveGenerator.getInstance();

    public static List<Triple<String, Position, Move>> epdReader(String filename) throws IOException {
        File file = new File(EPD.class.getResource(filename).getFile());
        List<String> lines = Files.readAllLines(file.toPath());

        return lines.stream()
                .flatMap(
                        line -> {
                            String[] parts = line.split(";");
                            String[] fenParts = parts[0].split("\\s+");

                            StringBuilder fenBuilder = new StringBuilder();
                            int bm = ArrayUtils.indexOf(fenParts, "bm");
                            if (bm < 0) return Stream.empty();

                            for (int i = 0; i < bm; i++) {
                                fenBuilder.append(fenParts[i]).append(" ");
                            }

                            String fen = fenBuilder.toString();

                            Position position = new Position(fen);

                            String move = fenParts[bm + 1];
                            Move bestMove =
                                    epdMoveToMove(moveGenerator.getAllMoves(position), move);
                            if (bestMove == null) {
                                System.out.println("Unable to parse: " + move);
                                return Stream.empty();
                            }

                            return Stream.of(Triple.of(parts[1], position, bestMove));
                        })
                .collect(Collectors.toList());
    }

    private static Move epdMoveToMove(List<Move> moves, String moveString) {
        moveString = moveString.replace("x", "");

        if (moveString.length() == 2) {
            int end = ArrayUtils.indexOf(BBUtils.squares, moveString);

            return moves.stream()
                    .filter(m -> m.getPieceIdx() <= 1)
                    .filter(m -> m.getEnd() == end)
                    .findFirst()
                    .orElse(null);
        } else if (moveString.length() == 3 && moveString.charAt(0) > 96) {
            int end = ArrayUtils.indexOf(BBUtils.squares, moveString.substring(1));
            int column = moveString.charAt(0) - 'a';

            return moves.stream()
                    .filter(m -> m.getPieceIdx() <= 1)
                    .filter(m -> m.getEnd() == end)
                    .filter(m -> m.getStart() % 8 == column)
                    .findFirst()
                    .orElse(null);
        } else {
            char pieceChar = moveString.substring(0, 1).toUpperCase().charAt(0);
            if (moveString.length() == 3) {
                int end = ArrayUtils.indexOf(BBUtils.squares, moveString.substring(1));

                int piece = ArrayUtils.indexOf(BBUtils.pieceSymbols, pieceChar);

                return moves.stream()
                        .filter(m -> m.getPieceIdx() == piece || m.getPieceIdx() == piece + 1)
                        .filter(m -> m.getEnd() == end)
                        .findFirst()
                        .orElse(null);
            } else if (moveString.length() == 4 && moveString.charAt(1) > 57) {
                int end = ArrayUtils.indexOf(BBUtils.squares, moveString.substring(2));
                int column = moveString.charAt(1) - 'a';

                int piece = ArrayUtils.indexOf(BBUtils.pieceSymbols, pieceChar);
                return moves.stream()
                        .filter(m -> m.getPieceIdx() == piece || m.getPieceIdx() == piece + 1)
                        .filter(m -> m.getEnd() == end)
                        .filter(m -> m.getStart() % 8 == column)
                        .findFirst()
                        .orElse(null);
            } else if (moveString.length() == 4) {
                int end = ArrayUtils.indexOf(BBUtils.squares, moveString.substring(2));
                int rank = Integer.parseInt(moveString.substring(1, 2));

                int piece = ArrayUtils.indexOf(BBUtils.pieceSymbols, pieceChar);
                return moves.stream()
                        .filter(m -> m.getPieceIdx() == piece || m.getPieceIdx() == piece + 1)
                        .filter(m -> m.getEnd() == end)
                        .filter(m -> (8 - m.getStart() / 8) == rank)
                        .findFirst()
                        .orElse(null);
            }
        }

        return null;
    }
}
