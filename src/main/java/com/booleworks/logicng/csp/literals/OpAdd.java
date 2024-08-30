package com.booleworks.logicng.csp.literals;

import com.booleworks.logicng.csp.IntegerDomain;
import com.booleworks.logicng.csp.IntegerHolder;
import com.booleworks.logicng.csp.terms.IntegerVariable;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * z op x+y (op in {=, <=, >=, !=})
 */
public class OpAdd implements RCSPLiteral {
    private final IntegerHolder z, x, y;
    private final Operator op;

    public OpAdd(final Operator op, final IntegerHolder z, final IntegerHolder x, final IntegerHolder y) {
        this.z = z;
        this.x = x;
        this.y = y;
        this.op = op;
    }

    public enum Operator {
        LE, GE, EQ, NE;

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

        public static Operator from(final LinearLiteral.Operator op, final boolean inverted) {
            switch (op) {
                case LE:
                    return inverted ? GE : LE;
                case EQ:
                    return EQ;
                case NE:
                    return NE;
            }
            throw new RuntimeException("Unreachable Code");
        }
    }

    public IntegerHolder getZ() {
        return z;
    }

    public IntegerHolder getX() {
        return x;
    }

    public IntegerHolder getY() {
        return y;
    }

    public Operator getOp() {
        return op;
    }

    @Override
    public int getUpperBound() {
        return Math.max(Math.max(z.getDomain().ub(), x.getDomain().ub()), y.getDomain().ub());
    }

    @Override
    public boolean isValid() {
        final IntegerDomain zd = z.getDomain();
        final IntegerDomain xd = x.getDomain();
        final IntegerDomain yd = y.getDomain();
        switch (op) {
            case LE:
                return zd.ub() < xd.lb() + yd.lb();
            case GE:
                return zd.lb() >= xd.ub() + yd.ub();
            case EQ:
                return zd.size() == 1 && xd.size() == 1 && yd.size() == 1 && zd.ub() == xd.ub() + yd.ub();
            case NE:
                return zd.cap(xd.add(yd)).isEmpty();
        }
        throw new RuntimeException("Unreachable code");
    }

    @Override
    public boolean isUnsat() {
        return false;
    }

    @Override
    public Set<IntegerVariable> getVariables() {
        final Set<IntegerVariable> set = new TreeSet<>();
        if (x instanceof IntegerVariable) {
            set.add((IntegerVariable) x);
        }
        if (y instanceof IntegerVariable) {
            set.add((IntegerVariable) y);
        }
        if (z instanceof IntegerVariable) {
            set.add((IntegerVariable) z);
        }
        return set;
    }

    @Override
    public ArithmeticLiteral substitute(final Map<IntegerVariable, IntegerVariable> assignment) {
        IntegerHolder newX = this.x;
        IntegerHolder newY = this.y;
        IntegerHolder newZ = this.z;
        if (this.x instanceof IntegerVariable) {
            final IntegerVariable x = (IntegerVariable) this.x;
            newX = assignment.getOrDefault(x, x);
        }
        if (this.y instanceof IntegerVariable) {
            final IntegerVariable y = (IntegerVariable) this.y;
            newY = assignment.getOrDefault(y, y);
        }
        if (this.z instanceof IntegerVariable) {
            final IntegerVariable z = (IntegerVariable) this.z;
            newZ = assignment.getOrDefault(z, z);
        }
        if (this.x == newX && this.y == newY && this.z == newZ) {
            return this;
        } else {
            return new OpAdd(op, newZ, newX, newY);
        }
    }
}
