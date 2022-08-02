package lox;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Stack;

import lox.Expr.Assign;
import lox.Expr.Binary;
import lox.Expr.Call;
import lox.Expr.Get;
import lox.Expr.Grouping;
import lox.Expr.Literal;
import lox.Expr.Logical;
import lox.Expr.Set;
import lox.Expr.Unary;
import lox.Expr.Variable;
import lox.Stmt.Block;
import lox.Stmt.Class;
import lox.Stmt.Expression;
import lox.Stmt.Function;
import lox.Stmt.If;
import lox.Stmt.Print;
import lox.Stmt.Return;
import lox.Stmt.Var;
import lox.Stmt.While;

enum FunctionType{
    NONE,
    FUNCTION,
    METHOD
}

public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private FunctionType currentFunction = FunctionType.NONE;

    Resolver (Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    void resolve(List<Stmt> statements){
        for(Stmt statement : statements){
            resolve(statement);
        }
    }

    private void resolve(Stmt stmt){
        stmt.accept(this);
    }

    private void resolve(Expr expr){
        expr.accept(this);
    }

    private void beginScope(){
        scopes.push(new HashMap<String, Boolean>());
    }

    private void endScope(){
        scopes.pop();
    }

    private void declare(Token name){
        if(scopes.isEmpty()) return;

        Map<String,Boolean> scope = scopes.peek();
        if(scope.containsKey(name.lexeme)){
            Lox.error(name, "Already a variable with this name in this scope");
        }
        scope.put(name.lexeme, false);
    }

    private void define(Token name){
        if(scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme, true);
    }

    private void resolveLocal(Expr expr, Token name){
        for(int i = scopes.size() - 1; i >= 0; i--){
            if(scopes.get(i).containsKey(name.lexeme)){
                interpreter.resolve(expr, scopes.size() - 1 - i);
                return;
            }
        }
    }

    private void resolveFunction(Stmt.Function function, FunctionType type){
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;
        
        beginScope();
        for(Token param : function.params){
            declare(param);
            define(param);
        }
        resolve(function.body);
        endScope();

        currentFunction = enclosingFunction;
    }

    @Override
    public Void visitBlockStmt(Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitVariableExpr(Variable expr) {
        if(!scopes.isEmpty() &&
         scopes.peek().get(expr.name.lexeme) == Boolean.FALSE){
            Lox.error(expr.name,
             "Can't read local variable in its own initializer");
         }
         resolveLocal(expr, expr.name);
        return null;
    }

    @Override
    public Void visitVarStmt(Var stmt) {
        declare(stmt.name);
        if(stmt.initializer != null){
            resolve(stmt.initializer);
        }
        define(stmt.name);
        return null;
    }

    @Override
    public Void visitAssignExpr(Assign expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name);
        return null;
    }
    
    @Override
    public Void visitExpressionStmt(Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Function stmt) {
        declare(stmt.name);
        define(stmt.name);

        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitIfStmt(If stmt) {
        resolve(stmt.condition);
        resolve(stmt.thenBranch);
        if(stmt.elseBranch != null) resolve(stmt.elseBranch);
        return null;
    }

    @Override
    public Void visitPrintStmt(Print stmt) {
        resolve(stmt.expression);
        return null;
    }

    @Override
    public Void visitReturnStmt(Return stmt) {
        if(currentFunction == FunctionType.NONE){
            Lox.error(stmt.keyword, "Can't return from top-level code");
        }

        if(stmt.value != null){
            resolve(stmt.value);
        }
        return null;
    }


    @Override
    public Void visitWhileStmt(While stmt) {
        resolve(stmt.condition);
        resolve(stmt.body);
        return null;
    }

    @Override
    public Void visitBinaryExpr(Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitCallExpr(Call expr) {
        resolve(expr.callee);

        for(Expr argument : expr.arguments){
            resolve(argument);
        }

        return null;
    }

    @Override
    public Void visitGroupingExpr(Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteralExpr(Literal expr) {
        return null;
    }

    @Override
    public Void visitLogicalExpr(Logical expr) {
        resolve(expr.left);
        resolve(expr.right);

        return null;
    }

    @Override
    public Void visitUnaryExpr(Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitClassStmt(Class stmt) {
        declare(stmt.name);
        define(stmt.name);

        for(Stmt.Function method : stmt.methods){
            FunctionType declaration = FunctionType.METHOD;
            resolveFunction(method, declaration);
        }

        return null;
    }

    @Override
    public Void visitGetExpr(Get expr) {
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitSetExpr(Set expr) {
        resolve(expr.value);
        resolve(expr.object);
        return null;
    }

}
