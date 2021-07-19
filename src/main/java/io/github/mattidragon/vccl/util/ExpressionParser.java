package io.github.mattidragon.vccl.util;

import io.github.mattidragon.vccl.data.Expression;
import io.github.mattidragon.vccl.util.exception.InterpreterException;
import io.github.mattidragon.vccl.util.exception.InvalidExpressionException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ExpressionParser {
    private static final Predicate<String> NUMBER_PATTERN = Pattern.compile("[-+]?\\d+(\\.\\d+)?").asMatchPredicate();
    private static final Predicate<String> STRING_PATTERN = Pattern.compile("([\"'])[^\1]*\\1").asMatchPredicate();
    
    public static Expression parse(String[] parts, int lineNumber, String target) throws InterpreterException, IOException {
        String data = String.join(" ", parts).strip();
        parts = rereadParts(parts).toArray(new String[0]);
        
        if (data.isBlank()) throw new InvalidExpressionException("Empty expression").setLineNumber(lineNumber);
        
        if (parts.length == 1) {
            return new Expression(Expression.Type.REFERENCE, lineNumber, target, parts[0]);
        }

        if (parts.length == 5 && parts[1].equals("?")) {
            if (!parts[3].equals(":")) throw new InvalidExpressionException("Unable to resolve expression " + data).setLineNumber(lineNumber);
            return new Expression(Expression.Type.IF, lineNumber, target,
                    parts[0], parts[2], parts[4]);
        }
        if (parts[0].equals("!")) {
            if (parts.length != 2) throw new InvalidExpressionException("Unable to resolve expression " + data).setLineNumber(lineNumber);
            return new Expression(Expression.Type.NOT, lineNumber, target,
                    parts[1]);
        }
        if (parts.length == 3) {
            Expression.Type type = switch (parts[1]) {
                case "+" -> Expression.Type.ADDITION;
                case "-" -> Expression.Type.SUBTRACTION;
                case "*" -> Expression.Type.MULTIPLICATION;
                case "/" -> Expression.Type.DIVISION;
                case "|" -> Expression.Type.OR;
                case "&" -> Expression.Type.AND;
                case "=" -> Expression.Type.EQUALS;
                case ">" -> Expression.Type.GREATER_THAN;
                case "<" -> Expression.Type.LESS_THAN;
                default -> throw new InvalidExpressionException("Unable to resolve expression " + data).setLineNumber(lineNumber);
            };
            return new Expression(type, lineNumber, target, parts[0], parts[2]);
        }
        
        throw new InvalidExpressionException("Unable to resolve expression " + data).setLineNumber(lineNumber);
    }
    
    private static ArrayList<String> rereadParts(String[] oldParts) throws IOException, InvalidExpressionException {
        StringReader reader = new StringReader(String.join(" ", oldParts));
        StringBuilder result = new StringBuilder();
        ArrayList<String> newParts = new ArrayList<>();
        boolean isString = false;
        boolean isEscaped = false;
        while (true) {
            int i = reader.read();
            if (i == -1) {
                newParts.add(result.toString());
                break;
            }
            if (i == ' ' && !isString && !isEscaped) {
                newParts.add(result.toString());
                result.delete(0, result.length());
                continue;
            }
            if (i == '\\') {
                if (isEscaped) result.append('\\');
                isEscaped = !isEscaped;
                continue;
            }
            if (i == '"') {
                if (!isEscaped) {
                    isString = !isString;
                }
                isEscaped = false;
                result.append('"');
                continue;
            }
            if (isEscaped) {
                result.append(switch (i) {
                    case 'n' -> '\n';
                    case 'r' -> '\r';
                    case 't' -> '\t';
                    case ' ' -> ' ';
                    default -> throw new InvalidExpressionException("Unable to escape '" + (char)i + "'");
                });
                isEscaped = false;
                continue;
            }
            result.append((char) i);
        }
        return newParts;
    }
    
    public static Object resolveConstant(String input) throws InvalidExpressionException {
        if (NUMBER_PATTERN.test(input)) return Double.parseDouble(input);
        if (STRING_PATTERN.test(input)) return input.substring(1, input.length() - 1)
                .replaceAll("\\\\n", "\n")
                .replaceAll("\\\\r", "\r")
                .replaceAll("\\\\t", "\t");
        return switch (input) {
            case "true" -> true;
            case "false" -> false;
            default -> throw new InvalidExpressionException("Unknown constant: " + input);
        };
    }
}
