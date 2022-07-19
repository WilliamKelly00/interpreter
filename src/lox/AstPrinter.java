package lox;

import lox.Expr.Assign;
import lox.Expr.Binary;
import lox.Expr.Grouping;
import lox.Expr.Literal;
import lox.Expr.Unary;
import lox.Expr.Variable;

public class AstPrinter implements Expr.Visitor<String> {
    String print(Expr expr){
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Literal expr) {
        if( expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    private String parenthesize(String name, Expr... exprs){
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for(Expr expr : exprs){
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    @Override
    public String visitAssignExpr(Assign expr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String visitVariableExpr(Variable expr) {
        // TODO Auto-generated method stub
        return null;
    }

}
