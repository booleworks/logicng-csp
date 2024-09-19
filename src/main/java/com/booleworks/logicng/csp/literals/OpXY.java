package com.booleworks.logicng.csp.literals;

import com.booleworks.logicng.csp.datastructures.IntegerVariableSubstitution;
import com.booleworks.logicng.csp.datastructures.domains.IntegerDomain;
import com.booleworks.logicng.csp.terms.IntegerHolder;
import com.booleworks.logicng.csp.terms.IntegerVariable;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * x op y (op in {=, <=, !=})
 */
public class OpXY implements RCSPLiteral {
    private final IntegerHolder x, y;
    private final Operator op;

    public OpXY(final Operator op, final IntegerHolder x, final IntegerHolder y) {
        this(op, x, y, false);
    }

    public OpXY(final Operator op, final IntegerHolder x, final IntegerHolder y, final boolean inverted) {
        if (inverted && op == Operator.LE) {
            this.x = y;
            this.y = x;
        } else {
            this.x = x;
            this.y = y;
        }
        this.op = op;
    }

    public enum Operator {
        LE,
        EQ,
        NE;

        public static Operator from(final LinearLiteral.Operator op) {
            switch (op) {
                case LE:
                    return LE;
                case EQ:
                    return EQ;
                case NE:
                    return NE;
            }
            throw new RuntimeException("Unreachable Code");
        }
    }

    public Operator getOp() {
        return op;
    }

    public IntegerHolder getX() {
        return x;
    }

    public IntegerHolder getY() {
        return y;
    }

    @Override
    public int getUpperBound() {
        return Math.max(x.getDomain().ub(), y.getDomain().ub());
    }

    @Override
    public boolean isValid() {
        final IntegerDomain xd = x.getDomain();
        final IntegerDomain yd = y.getDomain();
        switch (op) {
            case LE:
                return xd.ub() <= yd.lb();
            case EQ:
                return xd.size() == 1 && yd.size() == 1 && xd.ub() == yd.ub();
            case NE:
                return xd.cap(yd).isEmpty();
        }
        throw new RuntimeException("Unreachable code");
    }

    @Override
    public boolean isUnsat() {
        return false;
    }

    @Override
    public Set<IntegerVariable> getVariables() {
        final Set<IntegerVariable> set = new LinkedHashSet<>();
        if (x instanceof IntegerVariable) {
            set.add((IntegerVariable) x);
        }
        if (y instanceof IntegerVariable) {
            set.add((IntegerVariable) y);
        }
        return set;
    }

    @Override
    public ArithmeticLiteral substitute(final IntegerVariableSubstitution assignment) {
        IntegerHolder newX = this.x;
        IntegerHolder newY = this.y;
        if (this.x instanceof IntegerVariable) {
            final IntegerVariable x = (IntegerVariable) this.x;
            newX = assignment.getOrSelf(x);
        }
        if (this.y instanceof IntegerVariable) {
            final IntegerVariable y = (IntegerVariable) this.y;
            newY = assignment.getOrSelf(y);
        }
        if (newX == x && newY == y) {
            return this;
        } else {
            return new OpXY(op, newX, newY);
        }
    }

    @Override
    public String toString() {
        return "(" + op + " " + x + " " + y + ")";
    }
}
