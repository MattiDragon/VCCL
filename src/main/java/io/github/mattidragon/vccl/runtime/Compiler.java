package io.github.mattidragon.vccl.runtime;

import io.github.mattidragon.vccl.data.Expression;
import io.github.mattidragon.vccl.util.ExpressionParser;
import io.github.mattidragon.vccl.util.exception.InterpreterException;
import io.github.mattidragon.vccl.util.exception.InvalidExpressionException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Compiler {
    private final File file;
    
    public Compiler(File file) {
        this.file = file;
    }
    
    public Expression[] compile() throws IOException, InterpreterException {
        Scanner scanner = new Scanner(file).useDelimiter("\\s*\\n\\s*");
        
        ArrayList<Expression> expressions = new ArrayList<>();
    
        int i = 1;
        
        while (scanner.hasNext()) {
            String data = scanner.next();
            if (data.startsWith("#") || data.isBlank()) {
                i++;
                continue;
            }
            Scanner command = new Scanner(data);
            String variable = command.next();
            
            if (!variable.endsWith(":")) {
                InvalidExpressionException e = new InvalidExpressionException(": expected");
                e.setLineNumber(i);
                throw e;
            }
            
            ArrayList<String> segments = new ArrayList<>();
            while (command.hasNext()) {
                segments.add(command.next());
            }
            Expression expr = ExpressionParser.parse(segments.toArray(new String[0]), i, variable.substring(0, variable.length()-1));
            expressions.add(expr);
            i++;
        }
        return expressions.toArray(new Expression[0]);
    }
}
