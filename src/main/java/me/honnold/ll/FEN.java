package me.honnold.ll;

import me.honnold.piece.*;
import me.honnold.position.CastlingRights;
import me.honnold.position.Move;
import me.honnold.position.Position;
import org.apache.commons.lang3.tuple.Pair;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FEN {
//    private static final Pattern movePattern = Pattern.compile("^([a-h])([1-8])([a-h])([1-8])$");
//
//    public static Board toBoard(String fen) {
//        int[] pieces = new int[64];
//        int[] colors = new int[64];
//        int[] pieceLocations = new int[32];
//
//        String[] parts = fen.split("\\s+");
//
//        int square = 0, i = 0;
//        while (square <= 63 && i < parts[0].length()) {
//
//            char c = parts[0].charAt(i++);
//            if (c == 'r') {
//                pieces[square] = Piece.ROOK;
//                colors[square] = Piece.BLACK;
//            } else if (c == 'R') {
//                pieces[square] = new Rook(Color.WHITE);
//            } else if (c == 'n') {
//                pieces[square] = new Knight(Color.BLACK);
//            } else if (c == 'N') {
//                pieces[square] = new Knight(Color.WHITE);
//            } else if (c == 'b') {
//                pieces[square] = new Bishop(Color.BLACK);
//            } else if (c == 'B') {
//                pieces[square] = new Bishop(Color.WHITE);
//            } else if (c == 'q') {
//                pieces[square] = new Queen(Color.BLACK);
//            } else if (c == 'Q') {
//                pieces[square] = new Queen(Color.WHITE);
//            } else if (c == 'k') {
//                pieces[square] = new King(Color.BLACK);
//            } else if (c == 'K') {
//                pieces[square] = new King(Color.WHITE);
//            } else if (c == 'p') {
//                pieces[square] = new Pawn(Color.BLACK);
//            } else if (c == 'P') {
//                pieces[square] = new Pawn(Color.WHITE);
//            } else if (c == '/') {
//                continue;
//            } else {
//                square += Integer.parseInt(String.valueOf(c)) - 1;
//            }
//
//            square++;
//        }
//
//        Color sideToMove = "w".equals(parts[1]) ? Color.WHITE : Color.BLACK;
//
//        boolean canCastleEast = parts[2].contains("K");
//        boolean canCastleWest = parts[2].contains("Q");
//        CastlingRights whiteCR = new CastlingRights(canCastleEast, canCastleWest);
//
//        canCastleEast = parts[2].contains("q");
//        canCastleWest = parts[2].contains("k");
//        CastlingRights blackCR = new CastlingRights(canCastleEast, canCastleWest);
//
//        int epSquare = parts[3].equals("-") ? 0 : convertSquareToIdx(parts[3].charAt(0), parts[3].charAt(1), true);
//
//        Position p = new Position(
//                pieces,
//                0, // TODO: Determine initial eval
//                whiteCR,
//                blackCR,
//                epSquare,
//                0,
//                Color.WHITE,
//                20
//        );
//
//        if (sideToMove == Color.WHITE)
//            return p;
//        else
//            return p.rotate();
//    }
//
//    public static Board getInit() {
//        return toBoard("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
//    }
}
