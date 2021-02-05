package me.honnold.berserk;

import static org.junit.jupiter.api.Assertions.*;

import me.honnold.berserk.board.Position;
import me.honnold.berserk.eval.PositionEvaluations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PositionEvaluationsTest {
    PositionEvaluations evaluator;

    @BeforeEach
    void setup() {
        evaluator = PositionEvaluations.getInstance();
        evaluator.clearPawnTT();
    }

    @Test
    void evaluatePawnStructure() {
        Position p = new Position("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

        assertEquals(0, evaluator.evaluatePawnStructure(p));
    }

    @Test
    void evaluatePawnStructure_isolated() {
        Position p = new Position("rnbqkbnr/pppppppp/8/8/8/8/P1PPPPPP/RNBQKBNR w KQkq - 0 1");

        assertEquals(-20, evaluator.evaluatePawnStructure(p));
    }

    @Test
    void evaluatePawnStructure_backwards() {
        Position p = new Position("rnbqkbnr/pp3ppp/3p4/4p3/4P3/2N2N2/PP3PPP/R1BQKB1R b KQkq - 0 6");

        assertEquals(-30, evaluator.evaluatePawnStructure(p));
    }

    @Test
    void evaluatePawnStructure_doubled() {
        Position p = new Position("rnbqkbnr/pppppppp/8/8/8/P7/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

        assertEquals(-40, evaluator.evaluatePawnStructure(p));
    }

    @Test
    void evaluateKnights_missingPawns() {
        Position p = new Position("8/8/8/8/8/8/8/N7 w - - 0 1");

        assertEquals(-74, evaluator.evaluateKnights(p));
    }

    @Test
    void evaluateKnights_blocker() {
        Position p = new Position("rnbqkbnr/pppppppp/8/8/3P4/1PN5/P1P1PPPP/R1BQKBNR w KQkq - 0 1");

        assertEquals(-40, evaluator.evaluateKnights(p));
    }

    @Test
    void evaluateKnights_blocker_not() {
        Position p = new Position("rnbqkbnr/pppppppp/8/8/3PP3/1PN5/P1P2PPP/R1BQKBNR w KQkq - 0 1");

        assertEquals(0, evaluator.evaluateKnights(p));
    }

    @Test
    void evaluateKnights_controlled() {
        Position p = new Position("rnbqkbnr/pp4pp/5p2/4p3/3p2P1/NP2pP1N/P1PPP2P/R1BQKB1R w KQkq - 0 1");

        assertEquals(-30, evaluator.evaluateKnights(p));
    }

    @Test
    void evaluateKnights_blocker_black() {
        Position p = new Position("r1bqkbnr/ppp1pppp/2n5/3p4/3P4/2P2N2/PP2PPPP/RNBQKB1R b KQkq - 0 3");

        assertEquals(-14, evaluator.evaluateKnights(p));
    }
}
