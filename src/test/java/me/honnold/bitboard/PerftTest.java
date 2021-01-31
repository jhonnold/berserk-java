package me.honnold.bitboard;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PerftTest {
    // found here: https://www.chessprogramming.org/Perft_Results

    @BeforeAll
    static void setup() {
        // Force load the static
        AttackMasks.getQueenAttacks(0, 0);
    }

    // region startpos
    @Test
    void perft_0() {
        long result = Perft.runPerft("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 0);

        assertEquals(1, result);
    }

    @Test
    void perft_1() {
        long result = Perft.runPerft("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 1);

        assertEquals(20, result);
    }

    @Test
    void perft_2() {
        long result = Perft.runPerft("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 2);

        assertEquals(400, result);
    }

    @Test
    void perft_3() {
        long result = Perft.runPerft("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 3);

        assertEquals(8902, result);
    }

    @Test
    void perft_4() {
        long result = Perft.runPerft("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 4);

        assertEquals(197281, result);
    }

    @Test
    void perft_5() {
        long result = Perft.runPerft("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 5);

        assertEquals(4865609, result);
    }

    // 14577.44
    @Test
    void perft_6() {
        long result = Perft.runPerft("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 6);

        assertEquals(119060324, result);
    }
    // endregion

    // region kiwipete
    @Test
    void perft_kiwipete1() {
        long result = Perft.runPerft("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -", 1);

        assertEquals(48, result);
    }

    @Test
    void perft_kiwipete2() {
        long result = Perft.runPerft("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -", 2);

        assertEquals(2039, result);
    }

    @Test
    void perft_kiwipete3() {
        long result = Perft.runPerft("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -", 3);

        assertEquals(97862, result);
    }

    @Test
    void perft_kiwipete4() {
        long result = Perft.runPerft("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -", 4);

        assertEquals(4085603, result);
    }

    // 31905.16
    @Test
    void perft_kiwipete5() {
        long result = Perft.runPerft("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -", 5);

        assertEquals(193690690, result);
    }
    // endregion

    // region 3
    @Test
    void perft_rooks1() {
        long result = Perft.runPerft("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", 1);

        assertEquals(14, result);
    }

    @Test
    void perft_rooks2() {
        long result = Perft.runPerft("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", 2);

        assertEquals(191, result);
    }

    @Test
    void perft_rooks3() {
        long result = Perft.runPerft("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", 3);

        assertEquals(2812, result);
    }

    @Test
    void perft_rooks4() {
        long result = Perft.runPerft("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", 4);

        assertEquals(43238, result);
    }

    @Test
    void perft_rooks5() {
        long result = Perft.runPerft("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", 5);

        assertEquals(674624, result);
    }

    // 1780.06
    @Test
    void perft_rooks6() {
        long result = Perft.runPerft("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -", 6);

        assertEquals(11030083, result);
    }
    // endregion

    // region 4
    @Test
    void perft_weird1() {
        long result = Perft.runPerft("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1", 1);

        assertEquals(6, result);
    }

    @Test
    void perft_weird2() {
        long result = Perft.runPerft("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1", 2);

        assertEquals(264, result);
    }

    @Test
    void perft_weird3() {
        long result = Perft.runPerft("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1", 3);

        assertEquals(9467, result);
    }

    @Test
    void perft_weird4() {
        long result = Perft.runPerft("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1", 4);

        assertEquals(422333, result);
    }

    @Test
    void perft_weird5() {
        long result = Perft.runPerft("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1", 5);

        assertEquals(15833292, result);
    }

    // 89088.77
    @Test
    void perft_weird6() {
        long result = Perft.runPerft("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1", 6);

        assertEquals(706045033, result);
    }
    // endregion

    // region bugfinder
    @Test
    void perft_bugfinder1() {
        long result = Perft.runPerft("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", 1);

        assertEquals(44, result);
    }

    @Test
    void perft_bugfinder2() {
        long result = Perft.runPerft("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", 2);

        assertEquals(1486, result);
    }

    @Test
    void perft_bugfinder3() {
        long result = Perft.runPerft("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", 3);

        assertEquals(62379, result);
    }

    @Test
    void perft_bugfinder4() {
        long result = Perft.runPerft("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", 4);

        assertEquals(2103487, result);
    }

    // 14516.84
    @Test
    void perft_bugfinder5() {
        long result = Perft.runPerft("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8", 5);

        assertEquals(89941194, result);
    }
    // endregion

    // region kiwi2
    @Test
    void perft_kiwi2_1() {
        long result = Perft.runPerft("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10", 1);

        assertEquals(46, result);
    }

    @Test
    void perft_kiwi2_2() {
        long result = Perft.runPerft("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10", 2);

        assertEquals(2079, result);
    }

    @Test
    void perft_kiwi2_3() {
        long result = Perft.runPerft("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10", 3);

        assertEquals(89890, result);
    }

    @Test
    void perft_kiwi2_4() {
        long result = Perft.runPerft("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10", 4);

        assertEquals(3894594, result);
    }

    // 31209.59
    @Test
    void perft_kiwi2_5() {
        long result = Perft.runPerft("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10", 5);

        assertEquals(164075551, result);
    }
    // endregion
}