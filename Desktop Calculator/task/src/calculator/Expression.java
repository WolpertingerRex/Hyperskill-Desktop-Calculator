package calculator;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

public class Expression {
    enum TokenType {
        OPENING_BRACKET, CLOSING_BRACKET,
        ROOT,
        POW,
        MULT, DIV,
        PLUS, MINUS,
        NUMBER
    }

    static class Token {
        private String value;
        private TokenType type;

        public Token(String value, TokenType type) {
            this.value = value;
            this.type = type;
        }

        @Override
        public String toString() {
            return value + " ";
        }

        public boolean isFunction() {
            return this.getPriority() == 4;
        }

        public int getPriority() {
            switch (type) {
                case ROOT:
                    return 4;
                case POW:
                    return 3;
                case MULT:
                case DIV:
                    return 2;
                case PLUS:
                case MINUS:
                    return 1;
                default:
                    return -1;
            }
        }
    }

    public static String calculate(String input) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.ENGLISH);
        numberFormat.setRoundingMode(RoundingMode.HALF_EVEN);
        DecimalFormat df = (DecimalFormat) numberFormat;
        df.applyPattern("#.########");

        List<Token> tokens = getTokens(input);
        Double result = computeAll(getRPN(tokens));
        return df.format(result);
    }

    private static Double computeAll(List<Token> RPN) {
        Stack<Token> tokens = new Stack<>();

        for (Token token : RPN) {
            if (token.type == TokenType.NUMBER) {
                tokens.add(token);
            } else if (token.isFunction()) {
                double num = Double.parseDouble(tokens.pop().value);
                TokenType operator = token.type;
                double result = 0;
                if (operator == TokenType.ROOT) {
                    result = Math.pow(num, 0.5);
                }
                tokens.push(new Token(String.valueOf(result), TokenType.NUMBER));

            } else {
                double right = Double.parseDouble(tokens.pop().value);
                double left = Double.parseDouble(tokens.pop().value);
                double result = 0;
                TokenType operator = token.type;
                switch (operator) {
                    case PLUS:
                        result = right + left;
                        break;
                    case MINUS:
                        result = left - right;
                        break;
                    case MULT:
                        result = left * right;
                        break;
                    case DIV:
                        result = left / right;
                        break;
                    case POW:
                        result = Math.pow(left, right);
                        break;
                }
                tokens.push(new Token(String.valueOf(result), TokenType.NUMBER));
            }
        }
        return Double.parseDouble(tokens.pop().value);
    }


    private static List<Token> getRPN(List<Token> tokens) {
        Stack<Token> operators = new Stack<>();
        List<Token> RPN = new LinkedList<>();
        for (Token token : tokens) {
            if (token.type == TokenType.NUMBER) {
                RPN.add(token);
                continue;
            }

            if (operators.isEmpty()) {
                operators.push(token);
                continue;
            }

            if (token.type == TokenType.OPENING_BRACKET) {
                operators.push(token);
                continue;
            }

            if (token.type == TokenType.CLOSING_BRACKET) {
                Token op2 = operators.pop();
                while (op2.type != TokenType.OPENING_BRACKET) {
                    RPN.add(op2);
                    if (!operators.isEmpty())
                        op2 = operators.pop();
                }
            }

            int priority = token.getPriority();

            if (priority > 0) {
                Token op2 = operators.peek();
                while (op2.getPriority() >= priority) {
                    RPN.add(op2);
                    operators.pop();
                    if (!operators.isEmpty())
                        op2 = operators.peek();
                    else break;
                }
                operators.push(token);
                continue;
            }
        }
        while (!operators.isEmpty()) {
            RPN.add(operators.pop());
        }

        return RPN;
    }

    public static LinkedList<Token> getTokens(String s) {
        s = normalize(s);

        LinkedList<Token> tokens = new LinkedList<>();
        int pos = 0;
        while (pos < s.length()) {
            char ch = s.charAt(pos);
            if (!Character.isDigit(ch) && ch != '.') {
                switch (ch) {
                    case '+':
                        tokens.add(new Token(String.valueOf(ch), TokenType.PLUS));
                        break;
                    case '-':
                        tokens.add(new Token(String.valueOf(ch), TokenType.MINUS));
                        break;
                    case '\u00D7':
                        tokens.add(new Token(String.valueOf(ch), TokenType.MULT));
                        break;
                    case '\u00F7':
                        tokens.add(new Token(String.valueOf(ch), TokenType.DIV));
                        break;
                    case '^':
                        tokens.add(new Token(String.valueOf(ch), TokenType.POW));
                        break;
                    case '(':
                        tokens.add(new Token(String.valueOf(ch), TokenType.OPENING_BRACKET));
                        break;
                    case ')':
                        tokens.add(new Token(String.valueOf(ch), TokenType.CLOSING_BRACKET));
                        break;
                    case '\u221A':
                        tokens.add(new Token(String.valueOf(ch), TokenType.ROOT));
                        break;
                }
                pos++;
            } else {
                StringBuilder sb = new StringBuilder();
                do {
                    sb.append(ch);
                    pos++;
                    if (pos >= s.length()) break;
                    ch = s.charAt(pos);
                } while (Character.isDigit(ch) || ch == '.');
                tokens.add(new Token(sb.toString(), TokenType.NUMBER));
            }
        }

        return tokens;
    }

    private static String normalize(String s) {
        return s.replaceAll(" ", "")
                .replaceAll("\\(-", "(0-")
                .replaceAll("^-", "0-");
    }


}
