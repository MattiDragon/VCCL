package io.github.mattidragon.vccl.data;

import io.github.mattidragon.vccl.util.ExpressionParser;
import io.github.mattidragon.vccl.util.exception.InterpreterException;
import io.github.mattidragon.vccl.util.exception.InvalidNameException;
import io.github.mattidragon.vccl.util.exception.InvalidVariableTypeException;
import io.github.mattidragon.vccl.util.exception.UnknownVariableException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Objects;

public class Expression {
    private static final BufferedReader IN = new BufferedReader(new InputStreamReader(System.in));
    private final Type type;
    private final String[] variables;
    private final String target;
    private final int lineNumber;
    
    public Expression(Type type, int lineNumber, String target, String... variables) throws InterpreterException {
        this.type = type;
        this.variables = variables;
        this.lineNumber = lineNumber;
        this.target = target;
        for (String variable : variables) {
            if (variable.startsWith("#")) continue;
            if (!variable.matches("[a-zA-Z0-9_]+")) throw new InvalidNameException("Non [a-zA-Z0-9_] character in variable name " + variable).setLineNumber(lineNumber);
        }
    }
    
    public String getTarget() {
        return target;
    }
    
    public Object evaluate(Map<String, Object> currentVars) throws InterpreterException {
        for (String variable : this.variables) {
            if (variable.equals("unicode_in"))
                try {
                    currentVars.put("unicode_in", IN.read());
                } catch (IOException e) {
                    throw new InterpreterException("Failed to read input", e).setLineNumber(lineNumber);
                }
            if (variable.equals("string_in"))
                try {
                    String line = IN.readLine();
                    if (line != null) currentVars.put("string_in", line);
                } catch (IOException e) {
                    throw new InterpreterException("Failed to read input", e).setLineNumber(lineNumber);
                }
            
            if (variable.startsWith("#")) {
                currentVars.put(variable, ExpressionParser.resolveConstant(variable.substring(1)));
            }
            
            if (!currentVars.containsKey(variable))
                throw new UnknownVariableException("Unknown variable " + variable).setLineNumber(lineNumber);
        }
        return switch (type) {
            case REFERENCE -> currentVars.get(variables[0]);
            case IF -> {
                if (!(currentVars.get(variables[0]) instanceof Boolean)) throw new InvalidVariableTypeException("Expected variable " + variables[0] + "to be of type Boolean but found" + variables[0].getClass().getSimpleName()).setLineNumber(lineNumber);
                yield currentVars.get((boolean)currentVars.get(variables[0]) ? variables[1] : variables [2]);
            }
            case ADDITION -> {
                if (currentVars.get(variables[0]) instanceof String || currentVars.get(variables[1]) instanceof String) {
                    yield currentVars.get(variables[0]).toString() + currentVars.get(variables[1]).toString();
                }
                assertOfType(variables[0], currentVars, Double.class);
                assertOfType(variables[1], currentVars, Double.class);
                yield (double)currentVars.get(variables[0]) + (double)currentVars.get(variables[1]);
            }
            case MULTIPLICATION -> {
                if (currentVars.get(variables[0]) instanceof String) {
                    assertOfType(variables[1], currentVars, Double.class);
                    yield currentVars.get(variables[0]).toString().repeat((int)currentVars.get(variables[1]));
                }
                if (currentVars.get(variables[1]) instanceof String) {
                    assertOfType(variables[0], currentVars, Double.class);
                    yield currentVars.get(variables[0]).toString().repeat((int)currentVars.get(variables[1]));
                }
                assertOfType(variables[0], currentVars, Double.class);
                assertOfType(variables[1], currentVars, Double.class);
                yield (double)currentVars.get(variables[0]) * (double)currentVars.get(variables[1]);
            }
            case SUBTRACTION -> {
                assertOfType(variables[0], currentVars, Double.class);
                assertOfType(variables[1], currentVars, Double.class);
                yield (double)currentVars.get(variables[0]) - (double)currentVars.get(variables[1]);
            }
            case DIVISION -> {
                assertOfType(variables[0], currentVars, Double.class);
                assertOfType(variables[1], currentVars, Double.class);
                yield (double)currentVars.get(variables[0]) / (double)currentVars.get(variables[1]);
            }
            case OR -> {
                assertOfType(variables[0], currentVars, Boolean.class);
                assertOfType(variables[1], currentVars, Boolean.class);
                yield (boolean)currentVars.get(variables[0]) || (boolean)currentVars.get(variables[1]);
            }
            case AND -> {
                assertOfType(variables[0], currentVars, Boolean.class);
                assertOfType(variables[1], currentVars, Boolean.class);
                yield (boolean)currentVars.get(variables[0]) && (boolean)currentVars.get(variables[1]);
            }
            case NOT -> {
                assertOfType(variables[0], currentVars, Boolean.class);
                boolean out = !((boolean)currentVars.get(variables[0]));
                yield out;
            }
            case EQUALS -> {
                Object a = currentVars.get(variables[0]);
                Object b = currentVars.get(variables[1]);
                yield Objects.equals(a, b);
            }
            case LESS_THAN -> {
                assertOfType(variables[0], currentVars, Double.class);
                assertOfType(variables[1], currentVars, Double.class);
                yield (double)currentVars.get(variables[0]) < (double)currentVars.get(variables[1]);
            }
            case GREATER_THAN -> {
                assertOfType(variables[0], currentVars, Double.class);
                assertOfType(variables[1], currentVars, Double.class);
                yield (double)currentVars.get(variables[0]) > (double)currentVars.get(variables[1]);
            }
        };
    }
    
    private void assertOfType(String variable, Map<String, Object> currentVars, Class<?> type) throws InterpreterException {
        if (!(type.isAssignableFrom(currentVars.get(variable).getClass())))
            throw new InvalidVariableTypeException("Expected variable " + variable + " to be of type " + type.getSimpleName() + " but found" + variable.getClass().getSimpleName()).setLineNumber(lineNumber);
    }
    
    public enum Type {
        //Misc
        REFERENCE, // a
        //Math
        ADDITION, // a + b
        SUBTRACTION, // a - b
        MULTIPLICATION, // a * b
        DIVISION, // a / b
        //Logic
        OR, // a | b
        AND, // a & b
        NOT, // ! a
        //Control
        IF, // a ? b : c
        //Comparison
        EQUALS, // a = b
        LESS_THAN, // a < b
        GREATER_THAN, // a > b
    }
    
    @Override
    public String toString() {
        String expr = switch (type) {
            case REFERENCE -> variables[0];
            case ADDITION -> variables[0] + " + " + variables[1];
            case SUBTRACTION -> variables[0] + " - " + variables[1];
            case MULTIPLICATION -> variables[0] + " * " + variables[1];
            case DIVISION -> variables[0] + " / " + variables[1];
            case OR -> variables[0] + " | " + variables[1];
            case AND -> variables[0] + " & " + variables[1];
            case NOT -> "! " + variables[1];
            case IF -> variables[0] + " ? " + variables[1] + " : " + variables[2];
            case EQUALS -> variables[0] + " = " + variables[1];
            case LESS_THAN -> variables[0] + " < " + variables[1];
            case GREATER_THAN -> variables[0] + " > " + variables[1];
        };
        
        return getClass().getSimpleName() + "[" + expr + "]";
    }
}
