package me.honnold.ll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PerftTest {
    Perft perfter;

    @BeforeEach void before() {
        perfter = new Perft();
    }

    @Test void perft_0() {
        long[] result = perfter.perft(0, new Board());

        System.out.println(Arrays.toString(result));

        assertEquals(1, result[0]);
    }

    @Test void perft_1() {
        long result[] = perfter.perft(1, new Board());

        System.out.println(Arrays.toString(result));

        assertEquals(20, result[0]);
    }

    @Test void perft_2() {
        long result[] = perfter.perft(2, new Board());

        System.out.println(Arrays.toString(result));

        assertEquals(400, result[0]);
    }

    @Test void perft_3() {
        long result[] = perfter.perft(3, new Board());

        System.out.println(Arrays.toString(result));

        assertEquals(8902, result[0]);
    }

    @Test void perft_4() {
        long result[] = perfter.perft(4, new Board());

        System.out.println(Arrays.toString(result));

        assertEquals(197281, result[0]);
    }

    @Test void perft_5() {
        long result[] = perfter.perft(5, new Board());

        System.out.println(Arrays.toString(result));

        assertEquals(4865609, result[0]);
    }
}
