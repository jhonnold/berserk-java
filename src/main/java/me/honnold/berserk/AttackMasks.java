package me.honnold.bitboard;

import java.util.Arrays;

import static me.honnold.bitboard.BoardUtils.*;

public class AttackMasks {
    public static final long[][] PAWN_ATTACKS;
    public static final long[] KNIGHT_ATTACKS;
    public static final long[] KING_ATTACKS;
    public static final long[][] ROOK_ATTACKS;
    public static final long[][] BISHOP_ATTACKS;

    public static final long[] ROOK_MASKS;
    public static final long[] BISHOP_MASKS;

    public static final long[] ROOK_MAGICS;
    public static final long[] BISHOP_MAGICS;

    public static final int[] ROOK_OCCUPANCY_BITS = {
            12, 11, 11, 11, 11, 11, 11, 12,
            11, 10, 10, 10, 10, 10, 10, 11,
            11, 10, 10, 10, 10, 10, 10, 11,
            11, 10, 10, 10, 10, 10, 10, 11,
            11, 10, 10, 10, 10, 10, 10, 11,
            11, 10, 10, 10, 10, 10, 10, 11,
            11, 10, 10, 10, 10, 10, 10, 11,
            12, 11, 11, 11, 11, 11, 11, 12
    };

    public static final int[] BISHOP_OCCUPANCY_BITS = {
            6, 5, 5, 5, 5, 5, 5, 6,
            5, 5, 5, 5, 5, 5, 5, 5,
            5, 5, 7, 7, 7, 7, 5, 5,
            5, 5, 7, 9, 9, 7, 5, 5,
            5, 5, 7, 9, 9, 7, 5, 5,
            5, 5, 7, 7, 7, 7, 5, 5,
            5, 5, 5, 5, 5, 5, 5, 5,
            6, 5, 5, 5, 5, 5, 5, 6
    };

    private static final long NOT_A_FILE = -72340172838076674L;
    private static final long NOT_H_FILE = 9187201950435737471L;
    private static final long NOT_AB_FILE = -217020518514230020L;
    private static final long NOT_GH_FILE = 4557430888798830399L;

    static {
        PAWN_ATTACKS = generatePawnAttacks();
        KNIGHT_ATTACKS = generateKnightAttacks();
        KING_ATTACKS = generateKingAttacks();

        BISHOP_MASKS = generateBishopMasks();
        ROOK_MASKS = generateRookMasks();

        ROOK_MAGICS = generateRookMagicNumbers();
        BISHOP_MAGICS = generateBishopMagicNumbers();

        ROOK_ATTACKS = generateRookAttacks();
        BISHOP_ATTACKS = generateBishopAttacks();
    }

    // region PAWNS
    private static long[][] generatePawnAttacks() {
        long[][] attacks = new long[2][64]; // color and square

        for (int sq = 0; sq < 64; sq++) {
            attacks[0][sq] = getPawnAttack(sq, 0);
            attacks[1][sq] = getPawnAttack(sq, 1);
        }

        return attacks;
    }

    private static long getPawnAttack(int square, int color) {
        long attacks = 0, board = setBit(0, square);

        if (color == 0) {
            if (((board >>> 7) & NOT_A_FILE) != 0) attacks |= (board >>> 7);
            if (((board >>> 9) & NOT_H_FILE) != 0) attacks |= (board >>> 9);
        } else {
            if (((board << 7) & NOT_H_FILE) != 0) attacks |= (board << 7);
            if (((board << 9) & NOT_A_FILE) != 0) attacks |= (board << 9);
        }

        return attacks;
    }
    // endregion

    // region KNIGHTS
    private static long[] generateKnightAttacks() {
        long[] attacks = new long[64];

        for (int sq = 0; sq < 64; sq++)
            attacks[sq] = getKnightAttack(sq);

        return attacks;
    }

    private static long getKnightAttack(int square) {
        long attacks = 0, board = setBit(0, square);

        if (((board >>> 17) & NOT_H_FILE) != 0) attacks |= (board >>> 17);
        if (((board >>> 15) & NOT_A_FILE) != 0) attacks |= (board >>> 15);
        if (((board >>> 10) & NOT_GH_FILE) != 0) attacks |= (board >>> 10);
        if (((board >>> 6) & NOT_AB_FILE) != 0) attacks |= (board >>> 6);

        if (((board << 17) & NOT_A_FILE) != 0) attacks |= (board << 17);
        if (((board << 15) & NOT_H_FILE) != 0) attacks |= (board << 15);
        if (((board << 10) & NOT_AB_FILE) != 0) attacks |= (board << 10);
        if (((board << 6) & NOT_GH_FILE) != 0) attacks |= (board << 6);

        return attacks;
    }
    // endregion

    // region KINGS
    private static long[] generateKingAttacks() {
        long[] attacks = new long[64];

        for (int sq = 0; sq < 64; sq++)
            attacks[sq] = getKingAttacks(sq);

        return attacks;
    }

    private static long getKingAttacks(int square) {
        long attacks = 0, board = setBit(0, square);

        attacks |= (board >>> 8);
        if (((board >>> 7) & NOT_A_FILE) != 0) attacks |= (board >>> 7);
        if (((board >>> 9) & NOT_H_FILE) != 0) attacks |= (board >>> 9);
        if (((board >>> 1) & NOT_H_FILE) != 0) attacks |= (board >>> 1);

        attacks |= (board << 8);
        if (((board << 7) & NOT_H_FILE) != 0) attacks |= (board << 7);
        if (((board << 9) & NOT_A_FILE) != 0) attacks |= (board << 9);
        if (((board << 1) & NOT_A_FILE) != 0) attacks |= (board << 1);

        return attacks;
    }
    // endregion

    // region BISHOPS
    private static long[] generateBishopMasks() {
        long[] attacks = new long[64];

        for (int sq = 0; sq < 64; sq++)
            attacks[sq] = getBishopMask(sq);

        return attacks;
    }

    private static long getBishopMask(int square) {
        long attacks = 0;

        int startRank = square / 8;
        int startFile = square % 8;

        for (int r = startRank + 1, f = startFile + 1; r <= 6 && f <= 6; r++, f++) attacks |= (1L << (r * 8 + f));
        for (int r = startRank - 1, f = startFile + 1; r >= 1 && f <= 6; r--, f++) attacks |= (1L << (r * 8 + f));
        for (int r = startRank + 1, f = startFile - 1; r <= 6 && f >= 1; r++, f--) attacks |= (1L << (r * 8 + f));
        for (int r = startRank - 1, f = startFile - 1; r >= 1 && f >= 1; r--, f--) attacks |= (1L << (r * 8 + f));

        return attacks;
    }

    public static long getBishopAttacksOnTheFly(int square, long blocking) {
        long attacks = 0;

        int startRank = square / 8;
        int startFile = square % 8;

        for (int r = startRank + 1, f = startFile + 1; r <= 7 && f <= 7; r++, f++) {
            attacks |= (1L << (r * 8 + f));
            if (((1L << (r * 8 + f)) & blocking) != 0) break;
        }

        for (int r = startRank - 1, f = startFile + 1; r >= 0 && f <= 7; r--, f++) {
            attacks |= (1L << (r * 8 + f));
            if (((1L << (r * 8 + f)) & blocking) != 0) break;
        }

        for (int r = startRank + 1, f = startFile - 1; r <= 7 && f >= 0; r++, f--) {
            attacks |= (1L << (r * 8 + f));
            if (((1L << (r * 8 + f)) & blocking) != 0) break;
        }

        for (int r = startRank - 1, f = startFile - 1; r >= 0 && f >= 0; r--, f--) {
            attacks |= (1L << (r * 8 + f));
            if (((1L << (r * 8 + f)) & blocking) != 0) break;
        }

        return attacks;
    }

    public static long[][] generateBishopAttacks() {
        long[][] bishopAttacks = new long[64][512];
        for (int sq = 0; sq < 64; sq++) {
            long mask = BISHOP_MASKS[sq];
            int bitCount = BISHOP_OCCUPANCY_BITS[sq];
            int numOccupancies = (1 << bitCount);

            for (int i = 0; i < numOccupancies; i++) {
                long occupancy = setOccupancy(i, bitCount, mask);
                long magicIdx = (occupancy * BISHOP_MAGICS[sq]) >>> (64 - bitCount);

                bishopAttacks[sq][(int) (magicIdx & 0xFFF)] = getBishopAttacksOnTheFly(sq, occupancy);
            }
        }
        return bishopAttacks;
    }

    public static long getBishopAttacks(int square, long occupancy) {
        occupancy &= BISHOP_MASKS[square];
        occupancy *= BISHOP_MAGICS[square];
        occupancy >>>= 64 - BISHOP_OCCUPANCY_BITS[square];

        return BISHOP_ATTACKS[square][(int) (occupancy & 0xFFF)];
    }
    // endregion

    // region ROOKS
    private static long[] generateRookMasks() {
        long[] attacks = new long[64];

        for (int sq = 0; sq < 64; sq++)
            attacks[sq] = getRooksMask(sq);

        return attacks;
    }

    private static long getRooksMask(int square) {
        long attacks = 0;

        int startRank = square / 8;
        int startFile = square % 8;

        for (int r = startRank + 1; r <= 6; r++) attacks |= (1L << (r * 8 + startFile));
        for (int r = startRank - 1; r >= 1; r--) attacks |= (1L << (r * 8 + startFile));
        for (int f = startFile + 1; f <= 6; f++) attacks |= (1L << (startRank * 8 + f));
        for (int f = startFile - 1; f >= 1; f--) attacks |= (1L << (startRank * 8 + f));

        return attacks;
    }

    public static long getRookAttacksOnTheFly(int square, long blocking) {
        long attacks = 0;

        int startRank = square / 8;
        int startFile = square % 8;

        for (int r = startRank + 1; r <= 7; r++) {
            attacks |= (1L << (r * 8 + startFile));
            if (((1L << (r * 8 + startFile)) & blocking) != 0) break;
        }

        for (int r = startRank - 1; r >= 0; r--) {
            attacks |= (1L << (r * 8 + startFile));
            if (((1L << (r * 8 + startFile)) & blocking) != 0) break;
        }

        for (int f = startFile + 1; f <= 7; f++) {
            attacks |= (1L << (startRank * 8 + f));
            if (((1L << (startRank * 8 + f)) & blocking) != 0) break;
        }

        for (int f = startFile - 1; f >= 0; f--) {
            attacks |= (1L << (startRank * 8 + f));
            if (((1L << (startRank * 8 + f)) & blocking) != 0) break;
        }

        return attacks;
    }

    public static long[][] generateRookAttacks() {
        long[][] rookAttacks = new long[64][4096];
        for (int sq = 0; sq < 64; sq++) {
            long mask = ROOK_MASKS[sq];
            int bitCount = ROOK_OCCUPANCY_BITS[sq];
            int numOccupancies = (1 << bitCount);

            for (int i = 0; i < numOccupancies; i++) {
                long occupancy = setOccupancy(i, bitCount, mask);
                long magicIdx = (occupancy * ROOK_MAGICS[sq]) >>> (64 - bitCount);

                rookAttacks[sq][(int) (magicIdx & 0xFFF)] = getRookAttacksOnTheFly(sq, occupancy);
            }
        }
        return rookAttacks;
    }

    public static long getRookAttacks(int square, long occupancy) {
        occupancy &= ROOK_MASKS[square];
        occupancy *= ROOK_MAGICS[square];
        occupancy >>>= 64 - ROOK_OCCUPANCY_BITS[square];

        return ROOK_ATTACKS[square][(int) (occupancy & 0xFFF)];
    }
    // endregion

    // region QUEENS
    public static long getQueenAttacks(int square, long occupancy) {
        return getBishopAttacks(square, occupancy) | getRookAttacks(square, occupancy);
    }
    // endregion

    // region MAGICS
    public static long setOccupancy(int occupancyIdx, int bitCount, long attacks) {
        long occupancy = 0;

        for (int i = 0; i < bitCount; i++) {
            int square = getLSBIndex(attacks);

            attacks = popBit(attacks, square);

            if ((occupancyIdx & (1 << i)) != 0)
                occupancy |= (1L << square);
        }

        return occupancy;
    }

    public static long[] generateBishopMagicNumbers() {
        long[] bishopMagicNumbers = new long[64];

        for (int sq = 0; sq < 64; sq++)
            bishopMagicNumbers[sq] = findMagicNumber(sq, BISHOP_OCCUPANCY_BITS[sq], true);

        return bishopMagicNumbers;
    }

    public static long[] generateRookMagicNumbers() {
        long[] rookMagicNumbers = new long[64];

        for (int sq = 0; sq < 64; sq++)
            rookMagicNumbers[sq] = findMagicNumber(sq, ROOK_OCCUPANCY_BITS[sq], false);

        return rookMagicNumbers;
    }

    public static long findMagicNumber(int square, int bitCount, boolean isBishop) {
        int numOccupancies = 1 << bitCount;

        long[] occupancies = new long[numOccupancies];
        long[] attacks = new long[numOccupancies];
        long[] usedAttacks = new long[numOccupancies];

        long mask = isBishop ? BISHOP_MASKS[square] : ROOK_MASKS[square];

        for (int i = 0; i < numOccupancies; i++) {
            occupancies[i] = setOccupancy(i, bitCount, mask);
            attacks[i] = isBishop ? getBishopAttacksOnTheFly(square, occupancies[i]) : getRookAttacksOnTheFly(square, occupancies[i]);
        }

        for (int count = 0; count < 10000000; count++) {
            long magic = Random.getMagic();
            if (countBits((mask * magic) & 0xFF00000000000000L) < 6) continue;

            Arrays.fill(usedAttacks, 0L);

            boolean failed = false;
            for (int i = 0; !failed && i < numOccupancies; i++) {
                long magicIdx = (occupancies[i] * magic) >>> (64 - bitCount);
                int idx = (int) (magicIdx & 0xFFF);

                if (usedAttacks[idx] == 0)
                    usedAttacks[idx] = attacks[i];
                else if (usedAttacks[idx] != attacks[i])
                    failed = true;
            }

            if (!failed)
                return magic;
        }

        throw new RuntimeException("Unable to generate magic number!");
    }
    // endregion
}
