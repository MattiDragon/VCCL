package io.github.mattidragon.vccl.util.exception;

public class InterpreterException extends Exception {
    private int lineNumber = -1;
    
    public InterpreterException(String message) {
        super(message);
    }
    
    public InterpreterException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public InterpreterException setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
        return this;
    }
    
    @Override
    public String getMessage() {
        return (lineNumber == -1 ? "" : "Line " + lineNumber + ": ") + super.getMessage();
    }
}
