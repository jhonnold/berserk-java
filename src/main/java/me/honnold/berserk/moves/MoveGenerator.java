package me.honnold.berserk.moves;

import me.honnold.berserk.board.GameStage;
import me.honnold.berserk.board.Piece;
import me.honnold.berserk.board.Position;
import me.honnold.berserk.tt.Transpositions;

import java.util.Arrays;

import static me.honnold.berserk.util.BBUtils.*;

public class MoveGenerator {
    private static final MoveGenerator singleton = new MoveGenerator();
    private static final int TT_MOVE = Integer.MAX_VALUE;
    private static final int PV_MOVE = Integer.MAX_VALUE - 1;
    private static final int CAPTURE = Integer.MAX_VALUE >> 1;
    private static final int KILLER_ONE = Integer.MAX_VALUE >> 2;
    private static final int KILLER_TWO = (Integer.MAX_VALUE >> 2) - 1;
    private final Moves moves = Moves.getInstance();
    private final AttackMasks masks = AttackMasks.getInstance();
    private final Transpositions transpositions = Transpositions.getInstance();
    private final long[] doubleMoveRanks = {masks.rowMasks[6], masks.rowMasks[1]};
    private final long[] promotionRanks = {doubleMoveRanks[1], doubleMoveRanks[0]};
    private int[][] historicalMoveScores = new int[2][64 * 64];
    private int[][] bfMoveScores = new int[2][64 * 64];
    private int[][] killers = new int[100][2];

    private MoveGenerator() {
    }

    public static MoveGenerator getInstance() {
        return singleton;
    }

    public void clearHistoricalMoveScores() {
        Arrays.fill(this.historicalMoveScores[0], 1);
        Arrays.fill(this.historicalMoveScores[1], 1);
        Arrays.fill(this.bfMoveScores[0], 1);
        Arrays.fill(this.bfMoveScores[1], 1);
    }

    public void clearKillers() {
        this.killers = new int[100][2];
    }

    public void setHistoricalMoveScore(int side, int move, int depth) {
        this.historicalMoveScores[side][Move.getStartEnd(move)] += (depth * depth);
    }

    public void setBFMove(int side, int move, int depth) {
        this.bfMoveScores[side][Move.getStartEnd(move)] += (depth * depth);
    }

    public void addKiller(int move, int ply) {
        if (!Move.equals(move, killers[ply][0])) killers[ply][1] = killers[ply][0];

        killers[ply][0] = move;
    }

    public void addAllMoves(Position position, int ply) {
        moves.setPly(ply);

        this.addAllPawnMoves(position);
        this.addAllKnightMoves(position);
        this.addAllBishopMoves(position);
        this.addAllRookMoves(position);
        this.addAllQueenMoves(position);
        this.addAllKingMoves(position);
    }

    private void addAllPawnMoves(Position position) {
        this.addAllPawnQuiets(position);
        this.addAllPawnCaptures(position);
        this.addAllPawnPromotions(position);
    }

    private void addAllKnightMoves(Position position) {
        this.addAllKnightCaptures(position);
        this.addAllKnightQuiets(position);
    }

    private void addAllBishopMoves(Position position) {
        this.addAllBishopCaptures(position);
        this.addAllBishopQuiets(position);
    }

    private void addAllRookMoves(Position position) {
        this.addAllRookCaptures(position);
        this.addAllRookQuiets(position);
    }

    private void addAllQueenMoves(Position position) {
        this.addAllQueenCaptures(position);
        this.addAllQueenQuiets(position);
    }

    private void addAllKingMoves(Position position) {
        this.addAllKingCastles(position);
        this.addAllKingCaptures(position);
        this.addAllKingQuiets(position);
    }

    private void addAllPawnQuiets(Position position) {
        int pawnIdx = position.sideToMove;
        long pieceBoard = position.pieceBitboards[pawnIdx];

        int pawnDirection = position.sideToMove == 0 ? -8 : 8;
        long normalPawns = pieceBoard & masks.middleFourRanks;
        while (normalPawns != 0) {
            int start = getLSBIndex(normalPawns);
            normalPawns = popLSB(normalPawns);
            int end = start + pawnDirection;

            if (!getBit(position.occupancyBitboards[2], end))
                moves.add(Move.createMove(start, end, pawnIdx, 0, false, false, false, false));
        }

        long doubleJumpPawns = pieceBoard & doubleMoveRanks[position.sideToMove];
        while (doubleJumpPawns != 0) {
            int start = getLSBIndex(doubleJumpPawns);
            doubleJumpPawns = popLSB(doubleJumpPawns);
            int end = start + pawnDirection;

            if (!getBit(position.occupancyBitboards[2], end)) {
                moves.add(Move.createMove(start, end, pawnIdx, 0, false, false, false, false));

                end += pawnDirection;
                if (!getBit(position.occupancyBitboards[2], end))
                    moves.add(Move.createMove(start, end, pawnIdx, 0, false, true, false, false));
            }
        }
    }

    private void addAllPawnCaptures(Position position) {
        int pawnIdx = position.sideToMove;
        long pieceBoard = position.pieceBitboards[pawnIdx];
        long nonPromotingPawns = pieceBoard & ~promotionRanks[position.sideToMove];

        while (nonPromotingPawns != 0) {
            int start = getLSBIndex(nonPromotingPawns);
            nonPromotingPawns = popLSB(nonPromotingPawns);

            long attacks =
                    masks.getPawnAttacks(position.sideToMove, start)
                            & position.occupancyBitboards[1 - position.sideToMove];

            while (attacks != 0) {
                int end = getLSBIndex(attacks);
                attacks = popLSB(attacks);

                moves.add(Move.createMove(start, end, pawnIdx, 0, true, false, false, false));
            }

            if (position.epSquare != -1) {
                attacks =
                        masks.getPawnAttacks(position.sideToMove, start)
                                & (1L << position.epSquare);
                if (attacks != 0) {
                    moves.add(
                            Move.createMove(
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
    }

    private void addAllPawnPromotions(Position position) {
        int pawnIdx = position.sideToMove;
        long pieceBoard = position.pieceBitboards[pawnIdx];
        long promotingPawns = pieceBoard & promotionRanks[position.sideToMove];
        int pawnDirection = position.sideToMove == 0 ? -8 : 8;

        while (promotingPawns != 0) {
            int start = getLSBIndex(promotingPawns);
            promotingPawns = popLSB(promotingPawns);
            int end = start + pawnDirection;

            if (!getBit(position.occupancyBitboards[2], end)) {
                moves.add(
                        Move.createMove(
                                start,
                                end,
                                pawnIdx,
                                8 + position.sideToMove,
                                false,
                                false,
                                false,
                                false));
                moves.add(
                        Move.createMove(
                                start,
                                end,
                                pawnIdx,
                                6 + position.sideToMove,
                                false,
                                false,
                                false,
                                false));
                moves.add(
                        Move.createMove(
                                start,
                                end,
                                pawnIdx,
                                4 + position.sideToMove,
                                false,
                                false,
                                false,
                                false));
                moves.add(
                        Move.createMove(
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
                        Move.createMove(
                                start,
                                end,
                                pawnIdx,
                                8 + position.sideToMove,
                                true,
                                false,
                                false,
                                false));
                moves.add(
                        Move.createMove(
                                start,
                                end,
                                pawnIdx,
                                6 + position.sideToMove,
                                true,
                                false,
                                false,
                                false));
                moves.add(
                        Move.createMove(
                                start,
                                end,
                                pawnIdx,
                                4 + position.sideToMove,
                                true,
                                false,
                                false,
                                false));
                moves.add(
                        Move.createMove(
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

    private void addAllKnightCaptures(Position position) {
        int knightIdx = 2 + position.sideToMove;
        long pieceBoard = position.pieceBitboards[knightIdx];

        while (pieceBoard != 0) {
            int start = getLSBIndex(pieceBoard);
            pieceBoard = popLSB(pieceBoard);

            long attacks =
                    masks.getKnightAttacks(start)
                            & position.occupancyBitboards[1 - position.sideToMove];

            while (attacks != 0) {
                int end = getLSBIndex(attacks);
                attacks = popLSB(attacks);

                moves.add(Move.createMove(start, end, knightIdx, 0, true, false, false, false));
            }
        }
    }

    private void addAllKnightQuiets(Position position) {
        int knightIdx = 2 + position.sideToMove;
        long pieceBoard = position.pieceBitboards[knightIdx];

        while (pieceBoard != 0) {
            int start = getLSBIndex(pieceBoard);
            pieceBoard = popLSB(pieceBoard);

            long attacks =
                    masks.getKnightAttacks(start)
                            & ~position.occupancyBitboards[2];

            while (attacks != 0) {
                int end = getLSBIndex(attacks);
                attacks = popLSB(attacks);

                moves.add(
                        Move.createMove(start, end, knightIdx, 0, false, false, false, false));
            }
        }
    }

    private void addAllBishopCaptures(Position position) {
        int bishopIdx = 4 + position.sideToMove;
        long pieceBoard = position.pieceBitboards[bishopIdx];

        while (pieceBoard != 0) {
            int start = getLSBIndex(pieceBoard);
            pieceBoard = popLSB(pieceBoard);

            long attacks =
                    masks.getBishopAttacks(start, position.occupancyBitboards[2])
                            & position.occupancyBitboards[1 - position.sideToMove];

            while (attacks != 0) {
                int end = getLSBIndex(attacks);
                attacks = popLSB(attacks);

                moves.add(Move.createMove(start, end, bishopIdx, 0, true, false, false, false));
            }
        }
    }

    private void addAllBishopQuiets(Position position) {
        int bishopIdx = 4 + position.sideToMove;
        long pieceBoard = position.pieceBitboards[bishopIdx];

        while (pieceBoard != 0) {
            int start = getLSBIndex(pieceBoard);
            pieceBoard = popLSB(pieceBoard);

            long attacks =
                    masks.getBishopAttacks(start, position.occupancyBitboards[2])
                            & ~position.occupancyBitboards[2];

            while (attacks != 0) {
                int end = getLSBIndex(attacks);
                attacks = popLSB(attacks);

                moves.add(
                        Move.createMove(start, end, bishopIdx, 0, false, false, false, false));
            }
        }
    }

    private void addAllRookCaptures(Position position) {
        int rookIdx = 6 + position.sideToMove;
        long pieceBoard = position.pieceBitboards[rookIdx];

        while (pieceBoard != 0) {
            int start = getLSBIndex(pieceBoard);
            pieceBoard = popLSB(pieceBoard);

            long attacks =
                    masks.getRookAttacks(start, position.occupancyBitboards[2])
                            & position.occupancyBitboards[1 - position.sideToMove];

            while (attacks != 0) {
                int end = getLSBIndex(attacks);
                attacks = popLSB(attacks);

                moves.add(Move.createMove(start, end, rookIdx, 0, true, false, false, false));
            }
        }
    }

    private void addAllRookQuiets(Position position) {
        int rookIdx = 6 + position.sideToMove;
        long pieceBoard = position.pieceBitboards[rookIdx];

        while (pieceBoard != 0) {
            int start = getLSBIndex(pieceBoard);
            pieceBoard = popLSB(pieceBoard);

            long attacks =
                    masks.getRookAttacks(start, position.occupancyBitboards[2])
                            & ~position.occupancyBitboards[2];

            while (attacks != 0) {
                int end = getLSBIndex(attacks);
                attacks = popLSB(attacks);

                moves.add(Move.createMove(start, end, rookIdx, 0, false, false, false, false));
            }
        }
    }

    private void addAllQueenCaptures(Position position) {
        int queenIdx = 8 + position.sideToMove;
        long pieceBoard = position.pieceBitboards[queenIdx];

        while (pieceBoard != 0) {
            int start = getLSBIndex(pieceBoard);
            pieceBoard = popLSB(pieceBoard);

            long attacks =
                    masks.getQueenAttacks(start, position.occupancyBitboards[2])
                            & position.occupancyBitboards[1 - position.sideToMove];

            while (attacks != 0) {
                int end = getLSBIndex(attacks);
                attacks = popLSB(attacks);

                moves.add(Move.createMove(start, end, queenIdx, 0, true, false, false, false));
            }
        }
    }

    private void addAllQueenQuiets(Position position) {
        int queenIdx = 8 + position.sideToMove;
        long pieceBoard = position.pieceBitboards[queenIdx];

        while (pieceBoard != 0) {
            int start = getLSBIndex(pieceBoard);
            pieceBoard = popLSB(pieceBoard);

            long attacks =
                    masks.getQueenAttacks(start, position.occupancyBitboards[2])
                            & ~position.occupancyBitboards[2];

            while (attacks != 0) {
                int end = getLSBIndex(attacks);
                attacks = popLSB(attacks);

                moves.add(Move.createMove(start, end, queenIdx, 0, false, false, false, false));
            }
        }
    }

    private void addAllKingCastles(Position position) {
        int kingIdx = 10 + position.sideToMove;

        if (position.sideToMove == 0 && (position.castling & 0x8) == 8) {
            if (!getBit(position.occupancyBitboards[2], 61)
                    && !getBit(position.occupancyBitboards[2], 62)) {
                if (!position.isSquareAttacked(60, 1)
                        && !position.isSquareAttacked(61, 1)
                        && !position.isSquareAttacked(62, 1)) {
                    moves.add(Move.createMove(60, 62, kingIdx, 0, false, false, false, true));
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
                    moves.add(Move.createMove(60, 58, kingIdx, 0, false, false, false, true));
                }
            }
        }

        if (position.sideToMove == 1 && (position.castling & 0x2) == 2) {
            if (!getBit(position.occupancyBitboards[2], 5)
                    && !getBit(position.occupancyBitboards[2], 6)) {
                if (!position.isSquareAttacked(4, 0)
                        && !position.isSquareAttacked(5, 0)
                        && !position.isSquareAttacked(6, 0)) {
                    moves.add(Move.createMove(4, 6, kingIdx, 0, false, false, false, true));
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
                    moves.add(Move.createMove(4, 2, kingIdx, 0, false, false, false, true));
                }
            }
        }
    }

    private void addAllKingCaptures(Position position) {
        int kingIdx = 10 + position.sideToMove;
        long pieceBoard = position.pieceBitboards[kingIdx];

        while (pieceBoard != 0) {
            int start = getLSBIndex(pieceBoard);
            pieceBoard = popLSB(pieceBoard);

            long attacks =
                    masks.getKingAttacks(start) & position.occupancyBitboards[1 - position.sideToMove];

            while (attacks != 0) {
                int end = getLSBIndex(attacks);
                attacks = popLSB(attacks);

                moves.add(Move.createMove(start, end, kingIdx, 0, true, false, false, false));
            }
        }
    }

    private void addAllKingQuiets(Position position) {
        int kingIdx = 10 + position.sideToMove;
        long pieceBoard = position.pieceBitboards[kingIdx];

        while (pieceBoard != 0) {
            int start = getLSBIndex(pieceBoard);
            pieceBoard = popLSB(pieceBoard);

            long attacks =
                    masks.getKingAttacks(start) & ~position.occupancyBitboards[2];

            while (attacks != 0) {
                int end = getLSBIndex(attacks);
                attacks = popLSB(attacks);

                moves.add(Move.createMove(start, end, kingIdx, 0, false, false, false, false));
            }
        }
    }

    public void addAllCapturesAndPromotions(Position position, int ply) {
        moves.setPly(ply);

        this.addAllPawnPromotions(position);
        this.addAllPawnPromotions(position);
        this.addAllKnightCaptures(position);
        this.addAllBishopCaptures(position);
        this.addAllRookCaptures(position);
        this.addAllQueenCaptures(position);
        this.addAllKingCaptures(position);
    }

    public void sortMoves(int pv, int ply, Position position) {
        final long ttValue = transpositions.getEvaluationForPosition(position);
        final int ttMove = ttValue != 0 ? Transpositions.getMove(ttValue) : 0;

        for (int i = 1; i < moves.getMoveCount(ply) - 1; i++) {
            int m1 = moves.getMove(ply, i);
            int key = getMoveValue(m1, ttMove, pv, ply, position);

            int j = i - 1;
            while (j >= 0) {
                int m2 = this.moves.getMove(ply, j);
                int curr = getMoveValue(m2, ttMove, pv, ply, position);

                if (curr >= key) break;

                this.moves.setMove(ply, j + 1, m2);
                j--;
            }

            this.moves.setMove(ply, j + 1, m1);
        }
    }

    private int getMoveValue(int move, int ttMove, int pvMove, int ply, Position position) {
        if (Move.equals(move, ttMove)) return TT_MOVE;
        if (Move.equals(move, pvMove)) return PV_MOVE;

        if (Move.isCapture(move)) {
            for (int i = 1 - position.sideToMove; i < 12; i += 2) {
                long bb = position.pieceBitboards[i];

                if (getBit(bb, Move.getEnd(move)))
                    return CAPTURE + Piece.mvvLva[Move.getPieceIdx(move)][i];
            }
        }

        if (Move.isPromotion(move)) {
            return CAPTURE + Piece.getPieceValue(8, GameStage.OPENING);
        }

        if (isAKiller(move, ply))
            if (Move.equals(move, killers[ply][0])) return KILLER_ONE;
            else return KILLER_TWO;

        return 128 * this.historicalMoveScores[position.sideToMove][Move.getStartEnd(move)] /
                this.bfMoveScores[position.sideToMove][Move.getStartEnd(move)];
    }

    public boolean isAKiller(int move, int ply) {
        return Move.equals(move, killers[ply][0]) || Move.equals(move, killers[ply][1]);
    }
}
