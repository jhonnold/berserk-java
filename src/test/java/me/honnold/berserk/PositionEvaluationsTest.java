package me.honnold.berserk;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PositionEvaluationsTest {
    PositionEvaluations evaluator;

    @BeforeEach void setup() {
        evaluator = PositionEvaluations.getInstance();
        evaluator.clearPawnTT();
    }

    @Test void evaluatePawnStructure() {
        Position p = new Position("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

        assertEquals(0, evaluator.evaluatePawnStructure(p));
    }

    @Test void evaluatePawnStructure_isolated() {
        Position p = new Position("rnbqkbnr/pppppppp/8/8/8/8/P1PPPPPP/RNBQKBNR w KQkq - 0 1");

        assertEquals(-20, evaluator.evaluatePawnStructure(p));
    }

    @Test void evaluatePawnStructure_backwards() {
        Position p = new Position("rnbqkbnr/pp3ppp/3p4/4p3/4P3/2N2N2/PP3PPP/R1BQKB1R b KQkq - 0 6");

        assertEquals(-30, evaluator.evaluatePawnStructure(p));
    }

    @Test void evaluatePawnStructure_doubled() {
        Position p = new Position("rnbqkbnr/pppppppp/8/8/8/P7/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

        assertEquals(-40, evaluator.evaluatePawnStructure(p));
    }
}