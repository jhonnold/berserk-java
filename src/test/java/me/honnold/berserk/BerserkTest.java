package me.honnold.berserk;

import static org.junit.jupiter.api.Assertions.assertEquals;

import me.honnold.berserk.board.Position;
import me.honnold.berserk.moves.Move;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

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
}
