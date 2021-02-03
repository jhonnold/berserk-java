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
        Position p = new Position("rnbqkbnr/pppppppp/8/8/8/8/P1P1P1P1/RNBQKBNR w KQkq - 0 1");

        assertEquals(0, evaluator.evaluatePawnStructure(p));
    }
}