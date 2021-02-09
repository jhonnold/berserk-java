package me.honnold.berserk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import me.honnold.berserk.board.Position;
import me.honnold.berserk.moves.Move;
import me.honnold.berserk.util.EPD;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.MethodSource;

public class BerserkTest {
    private Berserk berserk;

    private static Stream<Arguments> getEigenmannRapidEngineTest() throws IOException {
        List<EPD.EPDAnalysis> pairs = EPD.epdReader("/eigenmannRapidEngineTest.epd");

        return pairs.stream().map(Arguments::of);
    }

    private static Stream<Arguments> getKaufmanTest() throws IOException {
        List<EPD.EPDAnalysis> pairs = EPD.epdReader("/kaufmanTest.epd");

        return pairs.stream().map(Arguments::of);
    }

    private static Stream<Arguments> getSBD() throws IOException {
        List<EPD.EPDAnalysis> pairs = EPD.epdReader("/sbd.epd");

        return pairs.stream().map(Arguments::of);
    }

    private static Stream<Arguments> getWAC() throws IOException {
        List<EPD.EPDAnalysis> pairs = EPD.epdReader("/winAtChess.epd");

        return pairs.stream().map(Arguments::of);
    }

    private static Stream<Arguments> getSTS7() throws IOException {
        List<EPD.EPDAnalysis> pairs = EPD.epdReader("/STS7.epd");

        return pairs.stream().map(Arguments::of);
    }

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
    void eigenmannRapidEngineTest(EPD.EPDAnalysis epdAnalysis) throws InterruptedException {
        System.out.println("Running test for " + epdAnalysis.id);
        System.out.println(epdAnalysis.position);
        System.out.println(
                "Expected "
                        + (epdAnalysis.isBest ? "best" : "avoid")
                        + " move: "
                        + epdAnalysis.move);

        Thread wait = berserk.searchForTime(epdAnalysis.position, 15000);
        wait.join();

        Move testResult = berserk.getSearchResults().getBestMove();

        if (epdAnalysis.isBest) {
            assertEquals(epdAnalysis.move, testResult);
        } else {
            assertNotEquals(epdAnalysis.move, testResult);
        }
    }

    @ParameterizedTest
    @MethodSource("getKaufmanTest")
    void kaufmanTest(EPD.EPDAnalysis epdAnalysis) throws InterruptedException {
        System.out.println("Running test for " + epdAnalysis.id);
        System.out.println(epdAnalysis.position);
        System.out.println(
                "Expected "
                        + (epdAnalysis.isBest ? "best" : "avoid")
                        + " move: "
                        + epdAnalysis.move);

        Thread wait = berserk.searchForTime(epdAnalysis.position, 15000);
        wait.join();

        Move testResult = berserk.getSearchResults().getBestMove();

        if (epdAnalysis.isBest) {
            assertEquals(epdAnalysis.move, testResult);
        } else {
            assertNotEquals(epdAnalysis.move, testResult);
        }
    }

    @ParameterizedTest
    @MethodSource("getSBD")
    void sbdTest(EPD.EPDAnalysis epdAnalysis) throws InterruptedException {
        System.out.println("Running test for " + epdAnalysis.id);
        System.out.println(epdAnalysis.position);
        System.out.println(
                "Expected "
                        + (epdAnalysis.isBest ? "best" : "avoid")
                        + " move: "
                        + epdAnalysis.move);

        Thread wait = berserk.searchForTime(epdAnalysis.position, 2500);
        wait.join();

        Move testResult = berserk.getSearchResults().getBestMove();

        if (epdAnalysis.isBest) {
            assertEquals(epdAnalysis.move, testResult);
        } else {
            assertNotEquals(epdAnalysis.move, testResult);
        }
    }

    @ParameterizedTest
    @MethodSource("getWAC")
    void wacTest(EPD.EPDAnalysis epdAnalysis) throws InterruptedException {
        System.out.println("Running test for " + epdAnalysis.id);
        System.out.println(epdAnalysis.position);
        System.out.println(
                "Expected "
                        + (epdAnalysis.isBest ? "best" : "avoid")
                        + " move: "
                        + epdAnalysis.move);

        Thread wait = berserk.searchForTime(epdAnalysis.position, 5000);
        wait.join();

        Move testResult = berserk.getSearchResults().getBestMove();

        if (epdAnalysis.isBest) {
            assertEquals(epdAnalysis.move, testResult);
        } else {
            assertNotEquals(epdAnalysis.move, testResult);
        }
    }

    @ParameterizedTest
    @MethodSource("getSTS7")
    void sts7Test(EPD.EPDAnalysis epdAnalysis) throws InterruptedException {
        System.out.println("Running test for " + epdAnalysis.id);
        System.out.println(epdAnalysis.position);
        System.out.println(
                "Expected "
                        + (epdAnalysis.isBest ? "best" : "avoid")
                        + " move: "
                        + epdAnalysis.move);

        Thread wait = berserk.searchForTime(epdAnalysis.position, 10000);
        wait.join();

        Move testResult = berserk.getSearchResults().getBestMove();

        if (epdAnalysis.isBest) {
            assertEquals(epdAnalysis.move, testResult);
        } else {
            assertNotEquals(epdAnalysis.move, testResult);
        }
    }
}
