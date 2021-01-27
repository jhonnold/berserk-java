package me.honnold.util;

import me.honnold.piece.*;
import me.honnold.position.CastlingRights;
import me.honnold.position.Position;
import org.apache.commons.lang3.tuple.Pair;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FEN {
    private static final Pattern movePattern = Pattern.compile("^([a-h])([1-8])([a-h])([1-8])$");

    public static Position toPosition(String fen) {
        Piece[] pieces = new Piece[120];

        String[] parts = fen.split("\\s+");

        int square = 21, i = 0;
        while (square <= 98 && i < parts[0].length()) {

            char c = parts[0].charAt(i++);
            if (c == 'r') {
                pieces[square] = new Rook(Color.BLACK);
            } else if (c == 'R') {
                pieces[square] = new Rook(Color.WHITE);
            } else if (c == 'n') {
                pieces[square] = new Knight(Color.BLACK);
            } else if (c == 'N') {
                pieces[square] = new Knight(Color.WHITE);
            } else if (c == 'b') {
                pieces[square] = new Bishop(Color.BLACK);
            } else if (c == 'B') {
                pieces[square] = new Bishop(Color.WHITE);
            } else if (c == 'q') {
                pieces[square] = new Queen(Color.BLACK);
            } else if (c == 'Q') {
                pieces[square] = new Queen(Color.WHITE);
            } else if (c == 'k') {
                pieces[square] = new King(Color.BLACK);
            } else if (c == 'K') {
                pieces[square] = new King(Color.WHITE);
            } else if (c == 'p') {
                pieces[square] = new Pawn(Color.BLACK);
            } else if (c == 'P') {
                pieces[square] = new Pawn(Color.WHITE);
            } else if (c == '/') {
                square++;
            } else if (c == ' ') {
                break;
            } else {
                square += Integer.parseInt(String.valueOf(c));
                square--;
            }

            square++;
        }

        Color sideToMove = "w".equals(parts[1]) ? Color.WHITE : Color.BLACK;

        boolean canCastleEast = parts[2].contains("K");
        boolean canCastleWest = parts[2].contains("Q");
        CastlingRights whiteCR = new CastlingRights(canCastleEast, canCastleWest);

        canCastleEast = parts[2].contains("q");
        canCastleWest = parts[2].contains("k");
        CastlingRights blackCR = new CastlingRights(canCastleEast, canCastleWest);

        int epSquare = parts[3].equals("-") ? 0 : convertSquareToIdx(parts[3].charAt(0), parts[3].charAt(1), true);

        Position p = new Position(
                pieces,
                0, // TODO: Determine initial eval
                whiteCR,
                blackCR,
                epSquare,
                0,
                Color.WHITE
        );

        if (sideToMove == Color.WHITE)
            return p;
        else
            return p.rotate();
    }

    public static Position getInit() {
        return toPosition("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
    }

    public static int convertSquareToIdx(char column, char rank, boolean asWhite) {
        return asWhite ?
                101 + column - 'a' - 10 * Integer.parseInt(String.valueOf(rank)) :
                108 - column + 'a' - 10 * (9 - Integer.parseInt(String.valueOf(rank)));
    }

    public static String convertIdxToSquare(int idx, boolean asWhite) {
        return asWhite ?
                "" + (char) ('a' + (idx % 10 - 1)) + (10 - (idx / 10)) :
                "" + (char) ('a' + 8 - (idx % 10)) + ((idx / 10) - 1);
    }

    public static Pair<Integer, Integer> getUserMove(boolean playingWhite, String input) {
        Matcher match = movePattern.matcher(input);
        if (!match.matches()) return null;

        int start = convertSquareToIdx(match.group(1).charAt(0), match.group(2).charAt(0), playingWhite);
        int end = convertSquareToIdx(match.group(3).charAt(0), match.group(4).charAt(0), playingWhite);

        return Pair.of(start, end);
    }
}
