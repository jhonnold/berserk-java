package me.honnold.berserk.moves;

import static me.honnold.berserk.util.BBUtils.*;

import java.util.*;
import me.honnold.berserk.board.Piece;
import me.honnold.berserk.board.Position;
import me.honnold.berserk.tt.Evaluation;
import me.honnold.berserk.tt.Transpositions;

public class MoveGenerator {
    private static final MoveGenerator singleton = new MoveGenerator();

    private final AttackMasks masks = AttackMasks.getInstance();
    private final Transpositions transpositions = Transpositions.getInstance();
    private int[][] historicalMoveScores = new int[12][64];
    private Move[][] killers = new Move[100][2];

    private MoveGenerator() {}

    public static MoveGenerator getInstance() {
        return singleton;
    }

    public void clearHistoricalMoveScores() {
        this.historicalMoveScores = new int[12][64];
    }

    public void clearKillers() {
        this.killers = new Move[100][2];
    }

    public void setHistoricalMoveScore(Move move, int score) {
        this.historicalMoveScores[move.getPieceIdx()][move.getEnd()] = score;
    }

    public void addKiller(Move move, int ply) {
        if (!move.equals(killers[ply][0])) killers[ply][1] = killers[ply][0];

        killers[ply][0] = move;
    }

    public List<Move> getAllMoves(final Position position) {
        final List<Move> moves = new ArrayList<>(128);

        this.addAllKingMoves(position, moves);
        this.addAllKnightMoves(position, moves);
        this.addAllBishopMoves(position, moves);
        this.addAllRookMoves(position, moves);
        this.addAllQueenMoves(position, moves);
        this.addAllPawnMoves(position, moves);

        return moves;
    }

    private void addAllPawnMoves(Position position, List<Move> moves) {
        int pawnIdx = position.sideToMove;
        long pieceBoard = position.pieceBitboards[pawnIdx];

        int pawnDirection = position.sideToMove == 0 ? -8 : 8;
        long[] promotionRanks = {masks.rowMasks[1], masks.rowMasks[6]};
        long[] doubleMoveRanks = {masks.rowMasks[6], masks.rowMasks[1]};

        long normalPawns = pieceBoard & masks.middleFourRanks;
        while (normalPawns != 0) {
            int start = getLSBIndex(normalPawns);
            normalPawns = popLSB(normalPawns);
            int end = start + pawnDirection;

            if (!getBit(position.occupancyBitboards[2], end))
                moves.add(new Move(start, end, pawnIdx, 0, false, false, false, false));

            long attacks =
                    masks.getPawnAttacks(position.sideToMove, start)
                            & position.occupancyBitboards[1 - position.sideToMove];

            while (attacks != 0) {
                end = getLSBIndex(attacks);
                attacks = popLSB(attacks);

                moves.add(new Move(start, end, pawnIdx, 0, true, false, false, false));
            }

            if (position.epSquare != -1) {
                attacks =
                        masks.getPawnAttacks(position.sideToMove, start)
                                & (1L << position.epSquare);
                if (attacks != 0) {
                    moves.add(
                            new Move(
                                    start,
                                    position.epSquare,
                                    pawnIdx,
                                    0,
                                    false,
                                    false,
                                    true,
                                    false));
                }
            }
        }

        long doubleJumpPawns = pieceBoard & doubleMoveRanks[position.sideToMove];
        while (doubleJumpPawns != 0) {
            int start = getLSBIndex(doubleJumpPawns);
            doubleJumpPawns = popLSB(doubleJumpPawns);
            int end = start + pawnDirection;

            if (!getBit(position.occupancyBitboards[2], end)) {
                moves.add(new Move(start, end, pawnIdx, 0, false, false, false, false));

                end += pawnDirection;
                if (!getBit(position.occupancyBitboards[2], end))
                    moves.add(new Move(start, end, pawnIdx, 0, false, true, false, false));
            }

            long attacks =
                    masks.getPawnAttacks(position.sideToMove, start)
                            & position.occupancyBitboards[1 - position.sideToMove];

            while (attacks != 0) {
                end = getLSBIndex(attacks);
                attacks = popLSB(attacks);

                moves.add(new Move(start, end, pawnIdx, 0, true, false, false, false));
            }
        }

        long promotingPawns = pieceBoard & promotionRanks[position.sideToMove];

        while (promotingPawns != 0) {
            int start = getLSBIndex(promotingPawns);
            promotingPawns = popLSB(promotingPawns);
            int end = start + pawnDirection;

            if (!getBit(position.occupancyBitboards[2], end)) {
                moves.add(
                        new Move(
                                start,
                                end,
                                pawnIdx,
                                8 + position.sideToMove,
                                false,
                                false,
                                false,
                                false));
                moves.add(
                        new Move(
                                start,
                                end,
                                pawnIdx,
                                6 + position.sideToMove,
                                false,
                                false,
                                false,
                                false));
                moves.add(
                        new Move(
                                start,
                                end,
                                pawnIdx,
                                4 + position.sideToMove,
                                false,
                                false,
                                false,
                                false));
                moves.add(
                        new Move(
                                start,
                                end,
                                pawnIdx,
                                2 + position.sideToMove,
                                false,
                                false,
                                false,
                                false));
            }

            long attacks =
                    masks.getPawnAttacks(position.sideToMove, start)
                            & position.occupancyBitboards[1 - position.sideToMove];
            while (attacks != 0) {
                end = getLSBIndex(attacks);
                attacks = popLSB(attacks);

                moves.add(
                        new Move(
                                start,
                                end,
                                pawnIdx,
                                8 + position.sideToMove,
                                true,
                                false,
                                false,
                                false));
                moves.add(
                        new Move(
                                start,
                                end,
                                pawnIdx,
                                6 + position.sideToMove,
                                true,
                                false,
                                false,
                                false));
                moves.add(
                        new Move(
                                start,
                                end,
                                pawnIdx,
                                4 + position.sideToMove,
                                true,
                                false,
                                false,
                                false));
                moves.add(
                        new Move(
                                start,
                                end,
                                pawnIdx,
                                2 + position.sideToMove,
                                true,
                                false,
                                false,
                                false));
            }
        }
    }

    private void addAllKingMoves(Position position, List<Move> moves) {
        int kingIdx = 10 + position.sideToMove;
        long pieceBoard = position.pieceBitboards[kingIdx];

        if (position.sideToMove == 0 && (position.castling & 0x8) == 8) {
            if (!getBit(position.occupancyBitboards[2], 61)
                    && !getBit(position.occupancyBitboards[2], 62)) {
                if (!position.isSquareAttacked(60, 1)
                        && !position.isSquareAttacked(61, 1)
                        && !position.isSquareAttacked(62, 1)) {
                    moves.add(new Move(60, 62, kingIdx, 0, false, false, false, true));
                }
            }
        }

        if (position.sideToMove == 0 && (position.castling & 0x4) == 4) {
            if (!getBit(position.occupancyBitboards[2], 57)
                    && !getBit(position.occupancyBitboards[2], 58)
                    && !getBit(position.occupancyBitboards[2], 59)) {
                if (!position.isSquareAttacked(60, 1)
                        && !position.isSquareAttacked(59, 1)
                        && !position.isSquareAttacked(58, 1)) {
                    moves.add(new Move(60, 58, kingIdx, 0, false, false, false, true));
                }
            }
        }

        if (position.sideToMove == 1 && (position.castling & 0x2) == 2) {
            if (!getBit(position.occupancyBitboards[2], 5)
                    && !getBit(position.occupancyBitboards[2], 6)) {
                if (!position.isSquareAttacked(4, 0)
                        && !position.isSquareAttacked(5, 0)
                        && !position.isSquareAttacked(6, 0)) {
                    moves.add(new Move(4, 6, kingIdx, 0, false, false, false, true));
                }
            }
        }

        if (position.sideToMove == 1 && (position.castling & 0x1) == 1) {
            if (!getBit(position.occupancyBitboards[2], 3)
                    && !getBit(position.occupancyBitboards[2], 2)
                    && !getBit(position.occupancyBitboards[2], 1)) {
                if (!position.isSquareAttacked(4, 0)
                        && !position.isSquareAttacked(3, 0)
                        && !position.isSquareAttacked(2, 0)) {
                    moves.add(new Move(4, 2, kingIdx, 0, false, false, false, true));
                }
            }
        }

        while (pieceBoard != 0) {
            int start = getLSBIndex(pieceBoard);
            pieceBoard = popLSB(pieceBoard);

            long attacks =
                    masks.getKingAttacks(start) & ~position.occupancyBitboards[position.sideToMove];

            while (attacks != 0) {
                int end = getLSBIndex(attacks);
                attacks = popLSB(attacks);

                if (getBit(position.occupancyBitboards[1 - position.sideToMove], end))
                    moves.add(new Move(start, end, kingIdx, 0, true, false, false, false));
                else moves.add(new Move(start, end, kingIdx, 0, false, false, false, false));
            }
        }
    }

    private void addAllKnightMoves(Position position, List<Move> moves) {
        int knightIdx = 2 + position.sideToMove;
        long pieceBoard = position.pieceBitboards[knightIdx];

        while (pieceBoard != 0) {
            int start = getLSBIndex(pieceBoard);
            pieceBoard = popLSB(pieceBoard);

            long attacks =
                    masks.getKnightAttacks(start)
                            & ~position.occupancyBitboards[position.sideToMove];

            while (attacks != 0) {
                int end = getLSBIndex(attacks);
                attacks = popLSB(attacks);

                if (getBit(position.occupancyBitboards[1 - position.sideToMove], end))
                    moves.add(new Move(start, end, knightIdx, 0, true, false, false, false));
                else moves.add(new Move(start, end, knightIdx, 0, false, false, false, false));
            }
        }
    }

    private void addAllBishopMoves(Position position, List<Move> moves) {
        int bishopIdx = 4 + position.sideToMove;
        long pieceBoard = position.pieceBitboards[bishopIdx];

        while (pieceBoard != 0) {
            int start = getLSBIndex(pieceBoard);
            pieceBoard = popLSB(pieceBoard);

            long attacks =
                    masks.getBishopAttacks(start, position.occupancyBitboards[2])
                            & ~position.occupancyBitboards[position.sideToMove];

            while (attacks != 0) {
                int end = getLSBIndex(attacks);
                attacks = popLSB(attacks);

                if (getBit(position.occupancyBitboards[1 - position.sideToMove], end))
                    moves.add(new Move(start, end, bishopIdx, 0, true, false, false, false));
                else moves.add(new Move(start, end, bishopIdx, 0, false, false, false, false));
            }
        }
    }

    private void addAllRookMoves(Position position, List<Move> moves) {
        int rookIdx = 6 + position.sideToMove;
        long pieceBoard = position.pieceBitboards[rookIdx];

        while (pieceBoard != 0) {
            int start = getLSBIndex(pieceBoard);
            pieceBoard = popLSB(pieceBoard);

            long attacks =
                    masks.getRookAttacks(start, position.occupancyBitboards[2])
                            & ~position.occupancyBitboards[position.sideToMove];

            while (attacks != 0) {
                int end = getLSBIndex(attacks);
                attacks = popLSB(attacks);

                if (getBit(position.occupancyBitboards[1 - position.sideToMove], end))
                    moves.add(new Move(start, end, rookIdx, 0, true, false, false, false));
                else moves.add(new Move(start, end, rookIdx, 0, false, false, false, false));
            }
        }
    }

    private void addAllQueenMoves(Position position, List<Move> moves) {
        int queenIdx = 8 + position.sideToMove;
        long pieceBoard = position.pieceBitboards[queenIdx];

        while (pieceBoard != 0) {
            int start = getLSBIndex(pieceBoard);
            pieceBoard = popLSB(pieceBoard);

            long attacks =
                    masks.getQueenAttacks(start, position.occupancyBitboards[2])
                            & ~position.occupancyBitboards[position.sideToMove];

            while (attacks != 0) {
                int end = getLSBIndex(attacks);
                attacks = popLSB(attacks);

                if (getBit(position.occupancyBitboards[1 - position.sideToMove], end))
                    moves.add(new Move(start, end, queenIdx, 0, true, false, false, false));
                else moves.add(new Move(start, end, queenIdx, 0, false, false, false, false));
            }
        }
    }

    public void sortMoves(List<Move> moves, Move pv, Position position, int ply) {
        Evaluation ttEval = transpositions.getEvaluationForPosition(position);
        Move ttMove = null;
        if (ttEval != null) ttMove = ttEval.getMove();

        final Move finalTtMove = ttMove;
        moves.sort(
                (moveOne, moveTwo) -> {
                    if (moveOne.equals(moveTwo)) return 0;

                    if (moveOne.equals(finalTtMove)) return -1;
                    if (moveTwo.equals(finalTtMove)) return 1;

                    if (moveOne.equals(pv)) return -1;
                    if (moveTwo.equals(pv)) return 1;

                    if (moveOne.isCapture() && moveTwo.isCapture()) {
                        int moveOneCapturedPiece = -1, moveTwoCapturedPiece = -1;
                        for (int i = 0; i < 12; i++) {
                            long bb = position.pieceBitboards[i];

                            if (getBit(bb, moveOne.getEnd())) moveOneCapturedPiece = i;

                            if (getBit(bb, moveTwo.getEnd())) moveTwoCapturedPiece = i;

                            if (moveOneCapturedPiece >= 0 && moveTwoCapturedPiece >= 0) break;
                        }

                        return Piece.mvvLva[moveTwo.getPieceIdx()][moveTwoCapturedPiece]
                                - Piece.mvvLva[moveOne.getPieceIdx()][moveOneCapturedPiece];
                    } else if (moveOne.isCapture()) {
                        return -1;
                    } else if (moveTwo.isCapture()) {
                        return 1;
                    } else {
                        if (ply < 1000) {
                            if (isAKiller(moveOne, ply) && isAKiller(moveTwo, ply)) {
                                boolean moveOneFirst = moveOne.equals(killers[ply][0]);

                                return moveOneFirst ? -1 : 1;
                            } else if (isAKiller(moveOne, ply)) {
                                return -1;
                            } else if (isAKiller(moveTwo, ply)) {
                                return 1;
                            }
                        }

                        return this.historicalMoveScores[moveTwo.getPieceIdx()][moveTwo.getEnd()]
                                - this.historicalMoveScores[moveOne.getPieceIdx()][
                                        moveOne.getEnd()];
                    }
                });
    }

    public boolean isAKiller(Move move, int ply) {
        return move.equals(killers[ply][0]) || move.equals(killers[ply][1]);
    }
}
