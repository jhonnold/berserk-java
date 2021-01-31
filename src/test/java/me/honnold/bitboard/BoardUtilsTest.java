package me.honnold.bitboard;

import org.junit.jupiter.api.Test;

import static me.honnold.bitboard.BoardUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class BoardUtilsTest {
    // region getBit
    @Test void getBit_off() {
        assertFalse(getBit(10L, 4));
    }

    @Test void getBit_on() {
        assertTrue(getBit(10L, 3));
    }
    // endregion

    // region setBit
    @Test void setBit_different() {
        long testResult = setBit(0L, 3);
        testResult = setBit(testResult, 1);

        assertEquals(10L, testResult);
    }

    @Test void setBit_same() {
        long testResult = setBit(0L, 3);
        testResult = setBit(testResult, 3);

        assertEquals(8L, testResult);
    }
    // endregion

    // region setBit
    @Test void popBit_on() {
        assertEquals(2L, popBit(10L, 3));
    }

    @Test void popBit_off() {
        assertEquals(10L, popBit(10L, 2));
    }
    // endregion

    // region countBits
    @Test void countBits_2() {
        assertEquals(2, countBits(10));
    }

    @Test void countBits_none() {
        assertEquals(0, countBits(0));
    }

    @Test void countBits_alot() {
        assertEquals(64, countBits(-1));
    }
    // endregion

    // region getLSBIndex
    @Test void getLSBIndex_0() {
        assertEquals(-1, getLSBIndex(0));
    }

    @Test void getLSBIndex_5() {
        assertEquals(5, getLSBIndex(96));
    }
    // endregion
}