package com.calc.service;

import com.calc.exception.CalculatorException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExpressionEngineTest {

    private final ExpressionEngine engine = new ExpressionEngine();

    @Test
    void addSubtractMultiplyDivide() {
        assertEquals(20, engine.evaluate("12+8"));
        assertEquals(4, engine.evaluate("10-6"));
        assertEquals(40, engine.evaluate("5*8"));
        assertEquals(5, engine.evaluate("10/2"));
    }

    @Test
    void precedenceAndParentheses() {
        assertEquals(7, engine.evaluate("1+2*3"));
        assertEquals(9, engine.evaluate("(1+2)*3"));
        assertEquals(12, engine.evaluate("10/2+7"));
        assertEquals(2, engine.evaluate("8-3*2"));
        assertEquals(20, engine.evaluate("(2+3)*4"));
        assertEquals(7, engine.evaluate(" 1 + 2 * 3 "));
        assertEquals(9, engine.evaluate("（1+2）*3"));
    }

    @Test
    void unaryAndDecimal() {
        assertEquals(3, engine.evaluate("-5+8"));
        assertEquals(-6, engine.evaluate("3*-2"));
        assertEquals(-6, engine.evaluate("3 * -2"));
        assertEquals(3.5, engine.evaluate("1.5+2"));
        assertEquals(0.3, engine.evaluate("0.1+0.2"), 1e-9);
    }

    @Test
    void unicodeOperators() {
        assertEquals(96, engine.evaluate("12×8"));
        assertEquals(6, engine.evaluate("12÷2"));
        assertEquals(1, engine.evaluate("8−7"));
    }

    @Test
    void divisionByZero() {
        CalculatorException ex = assertThrows(CalculatorException.class, () -> engine.evaluate("8/0"));
        assertEquals("Division by zero", ex.getMessage());
        assertThrows(CalculatorException.class, () -> engine.evaluate("10÷0"));
    }

    @Test
    void invalidExpressions() {
        assertThrows(CalculatorException.class, () -> engine.evaluate("1++2"));
        assertThrows(CalculatorException.class, () -> engine.evaluate("(1+2"));
        assertThrows(CalculatorException.class, () -> engine.evaluate("1+2)"));
        assertThrows(CalculatorException.class, () -> engine.evaluate(""));
        assertThrows(CalculatorException.class, () -> engine.evaluate("   "));
        assertThrows(CalculatorException.class, () -> engine.evaluate("1+"));
        assertThrows(CalculatorException.class, () -> engine.evaluate("*3"));
        assertThrows(CalculatorException.class, () -> engine.evaluate("abc"));
        assertThrows(CalculatorException.class, () -> engine.evaluate("1..2"));
        assertThrows(CalculatorException.class, () -> engine.evaluate("()"));
    }

    @Test
    void functionsAndPower() {
        assertEquals(4, engine.evaluate("sqrt(16)"));
        assertEquals(0.5, engine.evaluate("sin(30)"), 1e-9);
        assertEquals(8, engine.evaluate("2^3"));
        assertEquals(Math.PI, engine.evaluate("pi"), 1e-9);
        assertEquals(0.5, engine.evaluate("50%"));
    }

    @Test
    void rejectsIllegalTokens() {
        assertThrows(CalculatorException.class, () -> engine.evaluate("1;System.exit(0)"));
        assertThrows(CalculatorException.class, () -> engine.evaluate("Math.pow(2,3)"));
        assertEquals(6, engine.evaluate("2*3"));
    }
}
