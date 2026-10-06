package com.calc.service;

import com.calc.exception.CalculatorException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ExpressionEngine {

    private static final Set<String> FUNCTIONS = Set.of(
            "sqrt", "sin", "cos", "tan", "ln", "log", "abs", "floor", "ceil"
    );

    private static final Map<String, Double> CONSTANTS = Map.of(
            "pi", Math.PI,
            "e", Math.E
    );

    public double evaluate(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new CalculatorException("Invalid expression");
        }
        String normalized = normalize(raw);
        List<Token> tokens = tokenize(normalized);
        List<Token> rpn = toRpn(tokens);
        return evalRpn(rpn);
    }

    private String normalize(String raw) {
        return raw.replace("×", "*")
                .replace("÷", "/")
                .replace("−", "-")
                .replace("（", "(")
                .replace("）", ")")
                .trim();
    }

    private List<Token> tokenize(String expr) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        Token previous = null;
        while (i < expr.length()) {
            char c = expr.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            if (Character.isDigit(c) || c == '.') {
                int start = i;
                boolean dot = false;
                while (i < expr.length()) {
                    char n = expr.charAt(i);
                    if (n == '.') {
                        if (dot) {
                            throw new CalculatorException("Invalid expression");
                        }
                        dot = true;
                        i++;
                    } else if (Character.isDigit(n)) {
                        i++;
                    } else {
                        break;
                    }
                }
                String number = expr.substring(start, i);
                if (number.equals(".")) {
                    throw new CalculatorException("Invalid expression");
                }
                Token token = Token.number(Double.parseDouble(number));
                tokens.add(token);
                previous = token;
                continue;
            }
            if (Character.isLetter(c)) {
                int start = i;
                while (i < expr.length() && Character.isLetter(expr.charAt(i))) {
                    i++;
                }
                String name = expr.substring(start, i).toLowerCase(Locale.ROOT);
                if (CONSTANTS.containsKey(name)) {
                    Token token = Token.number(CONSTANTS.get(name));
                    tokens.add(token);
                    previous = token;
                    continue;
                }
                if (!FUNCTIONS.contains(name)) {
                    throw new CalculatorException("Invalid expression");
                }
                Token token = Token.function(name);
                tokens.add(token);
                previous = token;
                continue;
            }
            if (c == '(') {
                Token token = Token.lparen();
                tokens.add(token);
                previous = token;
                i++;
                continue;
            }
            if (c == ')') {
                Token token = Token.rparen();
                tokens.add(token);
                previous = token;
                i++;
                continue;
            }
            if (c == ',') {
                Token token = Token.comma();
                tokens.add(token);
                previous = token;
                i++;
                continue;
            }
            if (c == '%') {
                Token token = Token.percent();
                tokens.add(token);
                previous = token;
                i++;
                continue;
            }
            if ("+-*/^".indexOf(c) >= 0) {
                boolean prefix = previous == null
                        || previous.type == TokenType.OPERATOR
                        || previous.type == TokenType.LPAREN
                        || previous.type == TokenType.COMMA
                        || previous.type == TokenType.FUNCTION;
                Token token;
                if (prefix && c == '-') {
                    token = Token.unary("u-");
                } else if (prefix && c == '+') {
                    boolean afterOperator = previous != null && previous.type == TokenType.OPERATOR;
                    if (afterOperator) {
                        throw new CalculatorException("Invalid expression");
                    }
                    token = Token.unary("u+");
                } else if (prefix) {
                    throw new CalculatorException("Invalid expression");
                } else {
                    token = Token.operator(String.valueOf(c));
                }
                tokens.add(token);
                previous = token;
                i++;
                continue;
            }
            throw new CalculatorException("Invalid expression");
        }
        if (tokens.isEmpty()) {
            throw new CalculatorException("Invalid expression");
        }
        return tokens;
    }

    private List<Token> toRpn(List<Token> tokens) {
        List<Token> output = new ArrayList<>();
        Deque<Token> stack = new ArrayDeque<>();
        for (Token token : tokens) {
            switch (token.type) {
                case NUMBER -> output.add(token);
                case FUNCTION -> stack.push(token);
                case COMMA -> {
                    while (!stack.isEmpty() && stack.peek().type != TokenType.LPAREN) {
                        output.add(stack.pop());
                    }
                    if (stack.isEmpty()) {
                        throw new CalculatorException("Invalid expression");
                    }
                }
                case OPERATOR -> {
                    while (!stack.isEmpty() && stack.peek().type == TokenType.OPERATOR
                            && shouldPopOperator(stack.peek(), token)) {
                        output.add(stack.pop());
                    }
                    stack.push(token);
                }
                case PERCENT -> output.add(token);
                case LPAREN -> stack.push(token);
                case RPAREN -> {
                    while (!stack.isEmpty() && stack.peek().type != TokenType.LPAREN) {
                        output.add(stack.pop());
                    }
                    if (stack.isEmpty()) {
                        throw new CalculatorException("Invalid expression");
                    }
                    stack.pop();
                    if (!stack.isEmpty() && stack.peek().type == TokenType.FUNCTION) {
                        output.add(stack.pop());
                    }
                }
                default -> throw new CalculatorException("Invalid expression");
            }
        }
        while (!stack.isEmpty()) {
            Token token = stack.pop();
            if (token.type == TokenType.LPAREN || token.type == TokenType.RPAREN) {
                throw new CalculatorException("Invalid expression");
            }
            output.add(token);
        }
        return output;
    }

    private boolean shouldPopOperator(Token top, Token incoming) {
        int topPrec = precedence(top.value);
        int inPrec = precedence(incoming.value);
        if (isRightAssoc(incoming.value)) {
            return topPrec > inPrec;
        }
        return topPrec >= inPrec;
    }

    private int precedence(String op) {
        return switch (op) {
            case "u+", "u-" -> 5;
            case "^" -> 4;
            case "*", "/" -> 3;
            case "+", "-" -> 2;
            default -> 0;
        };
    }

    private boolean isRightAssoc(String op) {
        return "u+".equals(op) || "u-".equals(op) || "^".equals(op);
    }

    private double evalRpn(List<Token> rpn) {
        Deque<Double> stack = new ArrayDeque<>();
        for (Token token : rpn) {
            switch (token.type) {
                case NUMBER -> stack.push(token.number);
                case PERCENT -> {
                    if (stack.isEmpty()) {
                        throw new CalculatorException("Invalid expression");
                    }
                    stack.push(stack.pop() / 100.0);
                }
                case OPERATOR -> {
                    if ("u-".equals(token.value) || "u+".equals(token.value)) {
                        if (stack.isEmpty()) {
                            throw new CalculatorException("Invalid expression");
                        }
                        double v = stack.pop();
                        stack.push("u-".equals(token.value) ? -v : v);
                    } else {
                        if (stack.size() < 2) {
                            throw new CalculatorException("Invalid expression");
                        }
                        double b = stack.pop();
                        double a = stack.pop();
                        stack.push(applyBinary(token.value, a, b));
                    }
                }
                case FUNCTION -> {
                    if (stack.isEmpty()) {
                        throw new CalculatorException("Invalid expression");
                    }
                    stack.push(applyFunction(token.value, stack.pop()));
                }
                default -> throw new CalculatorException("Invalid expression");
            }
        }
        if (stack.size() != 1) {
            throw new CalculatorException("Invalid expression");
        }
        double result = stack.pop();
        if (Double.isNaN(result) || Double.isInfinite(result)) {
            throw new CalculatorException("Invalid expression");
        }
        return result;
    }

    private double applyBinary(String op, double a, double b) {
        return switch (op) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> {
                if (b == 0.0) {
                    throw new CalculatorException("Division by zero");
                }
                yield a / b;
            }
            case "^" -> Math.pow(a, b);
            default -> throw new CalculatorException("Invalid expression");
        };
    }

    private double applyFunction(String name, double x) {
        return switch (name) {
            case "sqrt" -> {
                if (x < 0) {
                    throw new CalculatorException("Invalid expression");
                }
                yield Math.sqrt(x);
            }
            case "sin" -> Math.sin(Math.toRadians(x));
            case "cos" -> Math.cos(Math.toRadians(x));
            case "tan" -> Math.tan(Math.toRadians(x));
            case "ln" -> {
                if (x <= 0) {
                    throw new CalculatorException("Invalid expression");
                }
                yield Math.log(x);
            }
            case "log" -> {
                if (x <= 0) {
                    throw new CalculatorException("Invalid expression");
                }
                yield Math.log10(x);
            }
            case "abs" -> Math.abs(x);
            case "floor" -> Math.floor(x);
            case "ceil" -> Math.ceil(x);
            default -> throw new CalculatorException("Invalid expression");
        };
    }

    private enum TokenType {
        NUMBER, OPERATOR, FUNCTION, LPAREN, RPAREN, COMMA, PERCENT
    }

    private static final class Token {
        private final TokenType type;
        private final String value;
        private final double number;

        private Token(TokenType type, String value, double number) {
            this.type = type;
            this.value = value;
            this.number = number;
        }

        static Token number(double n) {
            return new Token(TokenType.NUMBER, null, n);
        }

        static Token operator(String op) {
            return new Token(TokenType.OPERATOR, op, 0);
        }

        static Token unary(String op) {
            return new Token(TokenType.OPERATOR, op, 0);
        }

        static Token function(String name) {
            return new Token(TokenType.FUNCTION, name, 0);
        }

        static Token lparen() {
            return new Token(TokenType.LPAREN, "(", 0);
        }

        static Token rparen() {
            return new Token(TokenType.RPAREN, ")", 0);
        }

        static Token comma() {
            return new Token(TokenType.COMMA, ",", 0);
        }

        static Token percent() {
            return new Token(TokenType.PERCENT, "%", 0);
        }
    }
}
