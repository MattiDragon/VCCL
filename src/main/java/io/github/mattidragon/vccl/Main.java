package io.github.mattidragon.vccl;

import io.github.mattidragon.vccl.data.Expression;
import io.github.mattidragon.vccl.runtime.Compiler;
import io.github.mattidragon.vccl.runtime.Runner;
import io.github.mattidragon.vccl.util.exception.InterpreterException;

import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please specify a file a a command line argument!");
            return;
        }
        
        if (args[0].equals("-version")) {
            System.out.println("VCCL version 0.1");
            return;
        }
        
        Expression[] data;
        try {
            data = new Compiler(new File(String.join(" ", args))).compile();
        } catch (IOException e) {
            System.err.println("Failed to compile: IO Error:");
            System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage());
            return;
        } catch (InterpreterException e) {
            System.err.println("Failed to compile: Syntax Error:");
            System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage());
            return;
        }
        new Runner(data).run();
    }
}
