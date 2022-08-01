package lox;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Stack;


public class Resolver {
    private final Interpreter interpreter;
    
    Resolver (Interpreter interpreter) {
        this.interpreter = interpreter;
    }
}
