package io.github.mattidragon.vccl.runtime;

import io.github.mattidragon.vccl.data.Expression;
import io.github.mattidragon.vccl.util.exception.InterpreterException;
import io.github.mattidragon.vccl.util.exception.InvalidVariableTypeException;

import java.util.HashMap;
import java.util.Map;

public class Runner {
    private final Map<String, Object> variables = new HashMap<>();
    private final Expression[] commands;
    private int pointer = -2;
    
    public Runner(Expression[] commands) {
        this.commands = commands;
    }
    
    public void run() {
        if (pointer != -2) {
            variables.clear();
        }
        pointer = 0;
        
        while (true) {
            if (pointer >= commands.length || pointer < 0) return;
            Expression expr = commands[pointer];
            if (expr != null) {
                try {
                    variables.put(expr.getTarget(), expr.evaluate(variables));
                    if (variables.containsKey("string_out")) {
                        System.out.print(variables.get("string_out"));
                        variables.remove("string_out");
                    }
                    if (variables.containsKey("unicode_out")) {
                        if (variables.get("unicode_out") instanceof Double)
                            System.out.print((char) variables.get("unicode-out"));
                        else
                            throw new InvalidVariableTypeException("Expected variable unicode_out to be of type Double but found" + variables.get("ascii-io").getClass().getSimpleName());
                        variables.remove("unicode_out");
                    }
                    if (variables.containsKey("pointer")) {
                        if (variables.get("pointer") instanceof Double) {
                            pointer = (int) (double) variables.get("pointer") - 2;
                            variables.remove("pointer");
                        } else {
                            throw new InvalidVariableTypeException("Expected variable pointer to be of type Double but found" + variables.get("pointer").getClass().getSimpleName());
                        }
                    }
        
                } catch (InterpreterException e) {
                    System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage());
                    return;
                }
            }
            pointer++;
        }
    }
}
