package me.honnold.berserk.moves;

import static me.honnold.berserk.util.BBUtils.*;

import java.util.Arrays;
import me.honnold.berserk.util.Random;

public class AttackMasks {
    private static final AttackMasks singleton = new AttackMasks();
    public final long NOT_A_FILE = -72340172838076674L;
    public final long NOT_H_FILE = 9187201950435737471L;
    public final long[] rowMasks = {
        255L,
        65280L,
        16711680L,
        4278190080L,
        1095216660480L,
        280375465082880L,
        71776119061217280L,
        -72057594037927936L
    };
    public final long middleFourRanks = rowMasks[2] | rowMasks[3] | rowMasks[4] | rowMasks[5];
    private final Random random = Random.getInstance();
    private final long[][] PAWN_ATTACKS;
    private final long[] KNIGHT_ATTACKS;
    private final long[] KING_ATTACKS;
    private final long[][] ROOK_ATTACKS;
    // Queen attacks = ROOK + BISHOP
    private final long[][] BISHOP_ATTACKS;
    private final long[] ROOK_MASKS;
    private final long[] BISHOP_MASKS;
    private final long[] ROOK_MAGICS;
    private final long[] BISHOP_MAGICS;
    private final int[] ROOK_OCCUPANCY_BITS = {
        12, 11, 11, 11, 11, 11, 11, 12,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        11, 10, 10, 10, 10, 10, 10, 11,
        12, 11, 11, 11, 11, 11, 11, 12
    };
    private final int[] BISHOP_OCCUPANCY_BITS = {
        6, 5, 5, 5, 5, 5, 5, 6,
        5, 5, 5, 5, 5, 5, 5, 5,
        5, 5, 7, 7, 7, 7, 5, 5,
        5, 5, 7, 9, 9, 7, 5, 5,
        5, 5, 7, 9, 9, 7, 5, 5,
        5, 5, 7, 7, 7, 7, 5, 5,
        5, 5, 5, 5, 5, 5, 5, 5,
        6, 5, 5, 5, 5, 5, 5, 6
    };
    private final long NOT_AB_FILE = -217020518514230020L;
    private final long NOT_GH_FILE = 4557430888798830399L;
    private final long[] columnMasks = {
        72340172838076673L,
        144680345676153346L,
        289360691352306692L,
        578721382704613384L,
        1157442765409226768L,
        2314885530818453536L,
        4629771061636907072L,
        -9187201950435737472L
    };

    private AttackMasks() {
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

    public static AttackMasks getInstance() {
        return singleton;
    }

    public long getColumnMask(int column) {
        if (column < 0 || column > 7) return 0L;

        return columnMasks[column];
    }

    // region accessors
    public long getPawnAttacks(int side, int square) {
        return PAWN_ATTACKS[side][square];
    }

    public long getKnightAttacks(int square) {
        return KNIGHT_ATTACKS[square];
    }

    public long getKingAttacks(int square) {
        return KING_ATTACKS[square];
    }

    public long getQueenAttacks(int square, long occupancy) {
        return getBishopAttacks(square, occupancy) | getRookAttacks(square, occupancy);
    }

    public long getBishopAttacks(int square, long occupancy) {
        occupancy &= BISHOP_MASKS[square];
        occupancy *= BISHOP_MAGICS[square];
        occupancy >>>= 64 - BISHOP_OCCUPANCY_BITS[square];

        return BISHOP_ATTACKS[square][(int) (occupancy & 0xFFF)];
    }

    public long getRookAttacks(int square, long occupancy) {
        occupancy &= ROOK_MASKS[square];
        occupancy *= ROOK_MAGICS[square];
        occupancy >>>= 64 - ROOK_OCCUPANCY_BITS[square];

        return ROOK_ATTACKS[square][(int) (occupancy & 0xFFF)];
    }
    // endregion

    // region initializers
    // region PAWNS
    private long[][] generatePawnAttacks() {
        long[][] attacks = new long[2][64]; // color and square

        for (int sq = 0; sq < 64; sq++) {
            attacks[0][sq] = getGeneratedPawnAttacks(sq, 0);
            attacks[1][sq] = getGeneratedPawnAttacks(sq, 1);
        }

        return attacks;
    }

    private long getGeneratedPawnAttacks(int square, int color) {
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
    private long[] generateKnightAttacks() {
        long[] attacks = new long[64];

        for (int sq = 0; sq < 64; sq++) attacks[sq] = getGeneratedKnightAttacks(sq);

        return attacks;
    }

    private long getGeneratedKnightAttacks(int square) {
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
    private long[] generateKingAttacks() {
        long[] attacks = new long[64];

        for (int sq = 0; sq < 64; sq++) attacks[sq] = getGeneratedKingAttacks(sq);

        return attacks;
    }

    private long getGeneratedKingAttacks(int square) {
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
    private long[] generateBishopMasks() {
        long[] attacks = new long[64];

        for (int sq = 0; sq < 64; sq++) attacks[sq] = getBishopMask(sq);

        return attacks;
    }

    private long getBishopMask(int square) {
        long attacks = 0;

        int startRank = square / 8;
        int startFile = square % 8;

        for (int r = startRank + 1, f = startFile + 1; r <= 6 && f <= 6; r++, f++)
            attacks |= (1L << (r * 8 + f));
        for (int r = startRank - 1, f = startFile + 1; r >= 1 && f <= 6; r--, f++)
            attacks |= (1L << (r * 8 + f));
        for (int r = startRank + 1, f = startFile - 1; r <= 6 && f >= 1; r++, f--)
            attacks |= (1L << (r * 8 + f));
        for (int r = startRank - 1, f = startFile - 1; r >= 1 && f >= 1; r--, f--)
            attacks |= (1L << (r * 8 + f));

        return attacks;
    }

    private long getBishopAttacksOnTheFly(int square, long blocking) {
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

    private long[][] generateBishopAttacks() {
        long[][] bishopAttacks = new long[64][512];
        for (int sq = 0; sq < 64; sq++) {
            long mask = BISHOP_MASKS[sq];
            int bitCount = BISHOP_OCCUPANCY_BITS[sq];
            int numOccupancies = (1 << bitCount);

            for (int i = 0; i < numOccupancies; i++) {
                long occupancy = setOccupancy(i, bitCount, mask);
                long magicIdx = (occupancy * BISHOP_MAGICS[sq]) >>> (64 - bitCount);

                bishopAttacks[sq][(int) (magicIdx & 0xFFF)] =
                        getBishopAttacksOnTheFly(sq, occupancy);
            }
        }
        return bishopAttacks;
    }
    // endregion

    // region ROOKS
    private long[] generateRookMasks() {
        long[] attacks = new long[64];

        for (int sq = 0; sq < 64; sq++) attacks[sq] = getRooksMask(sq);

        return attacks;
    }

    private long getRooksMask(int square) {
        long attacks = 0;

        int startRank = square / 8;
        int startFile = square % 8;

        for (int r = startRank + 1; r <= 6; r++) attacks |= (1L << (r * 8 + startFile));
        for (int r = startRank - 1; r >= 1; r--) attacks |= (1L << (r * 8 + startFile));
        for (int f = startFile + 1; f <= 6; f++) attacks |= (1L << (startRank * 8 + f));
        for (int f = startFile - 1; f >= 1; f--) attacks |= (1L << (startRank * 8 + f));

        return attacks;
    }

    private long getRookAttacksOnTheFly(int square, long blocking) {
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

    private long[][] generateRookAttacks() {
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
    // endregion

    // region MAGICS
    private long setOccupancy(int occupancyIdx, int bitCount, long attacks) {
        long occupancy = 0;

        for (int i = 0; i < bitCount; i++) {
            int square = getLSBIndex(attacks);

            attacks = popBit(attacks, square);

            if ((occupancyIdx & (1 << i)) != 0) occupancy |= (1L << square);
        }

        return occupancy;
    }

    private long[] generateBishopMagicNumbers() {
        long[] bishopMagicNumbers = new long[64];

        for (int sq = 0; sq < 64; sq++)
            bishopMagicNumbers[sq] = findMagicNumber(sq, BISHOP_OCCUPANCY_BITS[sq], true);

        return bishopMagicNumbers;
    }

    private long[] generateRookMagicNumbers() {
        long[] rookMagicNumbers = new long[64];

        for (int sq = 0; sq < 64; sq++)
            rookMagicNumbers[sq] = findMagicNumber(sq, ROOK_OCCUPANCY_BITS[sq], false);

        return rookMagicNumbers;
    }

    private long findMagicNumber(int square, int bitCount, boolean isBishop) {
        int numOccupancies = 1 << bitCount;

        long[] occupancies = new long[numOccupancies];
        long[] attacks = new long[numOccupancies];
        long[] usedAttacks = new long[numOccupancies];

        long mask = isBishop ? BISHOP_MASKS[square] : ROOK_MASKS[square];

        for (int i = 0; i < numOccupancies; i++) {
            occupancies[i] = setOccupancy(i, bitCount, mask);
            attacks[i] =
                    isBishop
                            ? getBishopAttacksOnTheFly(square, occupancies[i])
                            : getRookAttacksOnTheFly(square, occupancies[i]);
        }

        for (int count = 0; count < 10000000; count++) {
            long magic = random.getMagic();
            if (countBits((mask * magic) & 0xFF00000000000000L) < 6) continue;

            Arrays.fill(usedAttacks, 0L);

            boolean failed = false;
            for (int i = 0; !failed && i < numOccupancies; i++) {
                long magicIdx = (occupancies[i] * magic) >>> (64 - bitCount);
                int idx = (int) (magicIdx & 0xFFF);

                if (usedAttacks[idx] == 0) usedAttacks[idx] = attacks[i];
                else if (usedAttacks[idx] != attacks[i]) failed = true;
            }

            if (!failed) return magic;
        }

        throw new RuntimeException("Unable to generate magic number!");
    }
    // endregion
    // endregion
}
