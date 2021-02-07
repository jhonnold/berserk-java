package me.honnold.berserk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import me.honnold.berserk.board.Position;
import me.honnold.berserk.moves.Move;
import me.honnold.berserk.util.BBUtils;
import me.honnold.berserk.util.EPD;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

public class BerserkTest {
    private Berserk berserk;

    @BeforeEach
    void setup() {
        berserk = new Berserk();
    }

    @ParameterizedTest
    @CsvFileSource(resources = {"bratko-kopec-positions.csv"})
    void search(String fen, int runTime, String expected) throws InterruptedException {
        Position position = new Position(fen);

        Thread wait = berserk.searchForTime(position, runTime);
        wait.join();

        Move testResult = berserk.getSearchResults().getBestMove();
        assertEquals(expected, testResult.toString());
    }

    @ParameterizedTest
    @MethodSource("getEigenmannRapidEngineTest")
    void eigenmannRapidEngineTest(String id, Position position, Move bestMove) throws InterruptedException {
        System.out.println("Running test for " + id);
        System.out.println(position);
        System.out.println("Expected best move: " + bestMove);

        Thread wait = berserk.searchForTime(position, 15000);
        wait.join();

        Move testResult = berserk.getSearchResults().getBestMove();
        assertEquals(bestMove, testResult);
    }

    @ParameterizedTest
    @MethodSource("getKaufmanTest")
    void kaufmanTest(String id, Position position, Move bestMove) throws InterruptedException {
        System.out.println("Running test for " + id);
        System.out.println(position);
        System.out.println("Expected best move: " + bestMove);

        Thread wait = berserk.searchForTime(position, 15000);
        wait.join();

        Move testResult = berserk.getSearchResults().getBestMove();
        assertEquals(bestMove, testResult);
    }

    @ParameterizedTest
    @MethodSource("getSBD")
    void sbdTest(String id, Position position, Move bestMove) throws InterruptedException {
        System.out.println("Running test for " + id);
        System.out.println(position);
        System.out.println("Expected best move: " + bestMove);

        Thread wait = berserk.searchForTime(position, 2500);
        wait.join();

        Move testResult = berserk.getSearchResults().getBestMove();
        assertEquals(bestMove, testResult);
    }

    private static Stream<Arguments> getEigenmannRapidEngineTest() throws IOException {
        List<Triple<String, Position, Move>> pairs = EPD.epdReader("/eigenmannRapidEngineTest.epd");

        return pairs.stream().map(t -> Arguments.of(t.getLeft(), t.getMiddle(), t.getRight()));
    }

    private static Stream<Arguments> getKaufmanTest() throws IOException {
        List<Triple<String, Position, Move>> pairs = EPD.epdReader("/kaufmanTest.epd");

        return pairs.stream().map(t -> Arguments.of(t.getLeft(), t.getMiddle(), t.getRight()));
    }

    private static Stream<Arguments> getSBD() throws IOException {
        List<Triple<String, Position, Move>> pairs = EPD.epdReader("/sbd.epd");

        return pairs.stream().map(t -> Arguments.of(t.getLeft(), t.getMiddle(), t.getRight()));
    }
}
