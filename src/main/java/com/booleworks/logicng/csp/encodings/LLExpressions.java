package com.booleworks.logicng.csp.encodings;

import com.booleworks.logicng.csp.LinearExpression;
import com.booleworks.logicng.csp.literals.LinearLiteral;

public class LLExpressions {
    private LLExpressions() {}

    public static LinearLiteral le(final LinearExpression lhs, final LinearExpression rhs) {
        final LinearExpression l = LinearExpression.subtract(lhs, rhs);
        return new LinearLiteral(l, LinearLiteral.Operator.LE);
    }

    public static LinearLiteral le(final LinearExpression lhs, final int e) {
        final LinearExpression.Builder l = new LinearExpression.Builder(lhs);
        l.setB(l.getB() - e);
        return new LinearLiteral(l.build(), LinearLiteral.Operator.LE);
    }

    public static LinearLiteral ge(final LinearExpression lhs, final LinearExpression rhs) {
        return le(rhs, lhs);
    }

    public static LinearLiteral ge(final LinearExpression lhs, final int e) {
        final LinearExpression.Builder l = new LinearExpression.Builder(lhs);
        l.setB(l.getB() - e);
        l.multiply(-1);
        return new LinearLiteral(l.build(), LinearLiteral.Operator.LE);
    }

    public static LinearExpression add(final LinearExpression... es) {
        final LinearExpression.Builder l = new LinearExpression.Builder(0);
        for (final LinearExpression e : es) {
            l.add(e);
        }
        return l.build();
    }

    public static LinearExpression add(final LinearExpression lhs, final int e) {
        final LinearExpression.Builder l = new LinearExpression.Builder(lhs);
        l.setB(l.getB() + e);
        return l.build();
    }

    public static LinearExpression sub(final LinearExpression lhs, final int e) {
        return add(lhs, e);
    }

    public static LinearExpression mul(final LinearExpression lhs, final int c) {
        final LinearExpression.Builder l = new LinearExpression.Builder(lhs);
        l.multiply(c);
        return l.build();
    }

}


