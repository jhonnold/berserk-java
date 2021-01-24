package me.honnold;

import me.honnold.piece.Color;
import me.honnold.piece.King;
import me.honnold.piece.Pawn;
import me.honnold.piece.Piece;
import me.honnold.position.Move;
import me.honnold.position.Position;

import java.util.ArrayList;
import java.util.List;

public class PositionService {
    public List<Move> generateMoves(Position position) {
        return new Moves(position, position.getMoving());
    }

    private static class Moves extends ArrayList<Move> {
        private final Position position;
        private final Color color;

        private Moves(Position position, Color color) {
            this.position = position;
            this.color = color;

            this.generate();
            this.sortMoves();
        }

        private void sortMoves() {
            this.sort((o1, o2) -> {
                if (o1.isCapture()) return -1;
                if (o2.isCapture()) return 1;
                return 0;
            });
        }

        private void generate() {
            int square = 0;

            Piece piece;
            while (square < 120) {
                piece = this.position.getPiece(square);

                if (piece == null || piece.getColor() != this.color) {
                    square++;
                    continue;
                }

                int[] directions = piece.getDirections();
                int direction = 0;

                while (direction < directions.length) {
                    int movement = directions[direction];
                    int gotoSquare = square + movement;

                    while (this.isOnBoard(gotoSquare)) {
                        Piece gotoPiece = this.position.getPiece(gotoSquare);
                        if (gotoPiece != null && gotoPiece.getColor() == this.color) break;

                        if (piece instanceof Pawn) {
                            // can't push a pawn into a piece (invalid capture)
                            if ((movement == -20 || movement == -10) && gotoPiece != null) break;

                            // can't double move if not on home square or blocked
                            if (movement == -20 && (square < 81 || this.position.getPiece(square - 10) != null)) break;

                            if ((movement == -9 || movement == -11) && gotoPiece == null &&
                                    gotoSquare != this.position.getEpSquare() &&
                                    gotoSquare != this.position.getKpSquare() &&
                                    gotoSquare != this.position.getKpSquare() - 1 &&
                                    gotoSquare != this.position.getKpSquare() + 1)
                                break;
                        }

                        this.add(new Move(square, gotoSquare, gotoPiece != null));

                        if (!piece.isRay() || gotoPiece != null) break;

                        if (square == 91 && this.position.getPiece(gotoSquare + 1) instanceof King && this.position.getMovingCastlingRights().canWestSide())
                            this.add(new Move(gotoSquare + 1, gotoSquare - 1, false));

                        if (square == 98 && this.position.getPiece(gotoSquare - 1) instanceof King && this.position.getMovingCastlingRights().canEastSide())
                            this.add(new Move(gotoSquare - 1, gotoSquare + 1, false));

                        gotoSquare += movement;
                    }

                    direction++;
                }

                square++;
            }
        }

        private boolean isOnBoard(int square) {
            return square >= 21 && square <= 98 && square % 10 != 0 && square % 10 != 9;
        }
    }
}
