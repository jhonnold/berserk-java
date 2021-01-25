package me.honnold.position;

import me.honnold.piece.*;

import java.util.LinkedList;
import java.util.List;

public class Position {
    private final Piece[] pieces;
    private final int score;
    private final CastlingRights movingCastlingRights;
    private final CastlingRights opponentCastlingRights;
    private final int epSquare;
    private final int kpSquare;
    private final Color moving;

    public Position(Piece[] pieces, int score, CastlingRights movingCastlingRights, CastlingRights opponentCastlingRights, int epSquare, int kpSquare, Color moving) {
        this.pieces = pieces;
        this.score = score;
        this.movingCastlingRights = movingCastlingRights;
        this.opponentCastlingRights = opponentCastlingRights;
        this.epSquare = epSquare;
        this.kpSquare = kpSquare;
        this.moving = moving;
    }

    public Piece getPiece(int square) {
        return this.pieces[square];
    }

    public Color getMoving() {
        return this.moving;
    }

    public int getScore() {
        return this.score;
    }

    public int getEpSquare() {
        return this.epSquare;
    }

    public CastlingRights getMovingCastlingRights() {
        return this.movingCastlingRights;
    }

    public CastlingRights getOpponentCastlingRights() {
        return this.opponentCastlingRights;
    }

    public List<Move> generateMoves() {
        List<Move> moves = new LinkedList<>();

        for (int start = 21; start <= 98; start++) {
            if (!this.isOnBoard(start)) continue;

            Piece piece = this.pieces[start];
            if (piece == null || piece.getColor() != this.moving) continue;

            for (int movement : piece.getDirections()) {
                int end = start + movement;

                while (this.isOnBoard(end)) {
                    Piece capturedPiece = this.pieces[end];
                    if (capturedPiece != null && capturedPiece.getColor() == this.moving) break;

                    if (piece instanceof Pawn) {
                        // can't push a pawn into a piece (invalid capture)
                        if ((movement == -20 || movement == -10) && capturedPiece != null) break;

                        // can't double move if not on home square or blocked
                        if (movement == -20 && (start < 81 || this.pieces[start - 10] != null)) break;

                        if ((movement == -9 || movement == -11) && capturedPiece == null &&
                                end != this.epSquare &&
                                end != this.kpSquare &&
                                end != this.kpSquare - 1 &&
                                end != this.kpSquare + 1)
                            break;
                    }

                    Move move = new Move(start, end, capturedPiece != null);
                    moves.add(move);

                    if (!piece.isRay() || capturedPiece != null) break;

                    if (start == 91 && this.pieces[end + 1] instanceof King && this.movingCastlingRights.canWestSide())
                        moves.add(new Move(end + 1, end - 1, false));

                    if (start == 98 && this.pieces[end - 1] instanceof King && this.movingCastlingRights.canEastSide())
                        moves.add(new Move(end - 1, end + 1, false));

                    end += movement;
                }
            }
        }

        moves.sort((o1, o2) -> {
            if (o1.isCapture()) return -1;
            if (o2.isCapture()) return 1;
            return 0;
        });

        return moves;
    }

    public Position rotate() {
        Piece[] reversed = new Piece[120];
        for (int i = 0; i < 120; i++)
            reversed[119 - i] = this.pieces[i];

        return new Position(
                reversed,
                -this.score,
                this.opponentCastlingRights.copy(),
                this.movingCastlingRights.copy(),
                this.epSquare != 0 ? 119 - this.epSquare : 0,
                this.kpSquare != 0 ? 119 - this.kpSquare : 0,
                this.moving == Color.WHITE ? Color.BLACK : Color.WHITE
        );
    }

    public Position move(Move m) {
        int start = m.getStart();
        int end = m.getEnd();

        int newEpSquare = 0;
        int newKpSquare = 0;

        int newScore = this.score + this.value(m);

        Piece movingPiece = this.pieces[start];
        Piece[] moved = new Piece[120];
        for (int i = 0; i < 120; i++) {
            if (i == start) {
                moved[i] = null;
            } else if (i == end) {
                moved[i] = movingPiece;
            } else {
                moved[i] = this.pieces[i];
            }
        }

        CastlingRights newMovingCastlingRights = this.movingCastlingRights.copy();
        CastlingRights newOpponentCastlingRights = this.opponentCastlingRights.copy();

        if (start == 91) newMovingCastlingRights = new CastlingRights(false, newMovingCastlingRights.canWestSide());
        if (start == 98) newMovingCastlingRights = new CastlingRights(newMovingCastlingRights.canEastSide(), false);
        if (end == 21) newOpponentCastlingRights = new CastlingRights(newOpponentCastlingRights.canEastSide(), false);
        if (end == 28) newOpponentCastlingRights = new CastlingRights(false, newOpponentCastlingRights.canWestSide());

        if (movingPiece instanceof King) {
            newMovingCastlingRights = new CastlingRights(false, false);
            if (Math.abs(start - end) == 2) {
                newKpSquare = (start + end) / 2;
                int movingRookSquare = end < start ? 91 : 98;
                Piece movingRook = moved[movingRookSquare];
                moved[movingRookSquare] = null;
                moved[newKpSquare] = movingRook;
            }
        } else if (movingPiece instanceof Pawn) {
            if (end >= 21 && end <= 28) {
                moved[end] = new Queen(moved[end].getColor());
            }
            if (end - start == -20) {
                newEpSquare = start - 10;
            }
            if (end == this.epSquare) {
                moved[end + 10] = null;
            }
        }

        return new Position(moved, newScore, newMovingCastlingRights, newOpponentCastlingRights, newEpSquare, newKpSquare, this.moving).rotate();
    }

    public int value(Move m) {
        int start = m.getStart();
        int end = m.getEnd();

        Piece movingPiece = this.pieces[start];
        Piece capturedPiece = this.pieces[end];

        int score = movingPiece.getValues()[end] - movingPiece.getValues()[start];
        if (capturedPiece != null)
            score += capturedPiece.getValues()[119 - end];

        if (Math.abs(end - this.kpSquare) < 2)
            score += new King(Color.WHITE).getValues()[119 - end];

        if (movingPiece instanceof King && Math.abs(start - end) == 2) {
            score += new Rook(Color.WHITE).getValues()[(start + end) / 2];
            score -= new Rook(Color.WHITE).getValues()[(end < start ? 91 : 98)];
        }

        if (movingPiece instanceof Pawn) {
            if (end >= 21 && end <= 28)
                score += new Queen(Color.WHITE).getValues()[end] - movingPiece.getValues()[end];

            if (end == this.epSquare)
                score += movingPiece.getValues()[119 - (end + 10)];
        }

        return score;
    }

    private boolean isOnBoard(int square) {
        return square % 10 != 0 && square % 10 != 9 && square >= 21 && square <= 98;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < 120; i++) {
            if (i < 21 || i > 98 || i % 10 == 0) continue;

            if (i % 10 == 9) {
                builder.append("\n");
            } else {
                Piece p = this.pieces[i];
                builder.append(p != null ? p.toString() : ".").append(" ");
            }
        }

        return builder.toString();
    }
}
