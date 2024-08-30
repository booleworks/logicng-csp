package com.booleworks.logicng.csp.literals;

import com.booleworks.logicng.csp.IntegerDomain;
import com.booleworks.logicng.csp.IntegerHolder;
import com.booleworks.logicng.csp.terms.IntegerVariable;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * z = x * y
 */
public class EqMul implements RCSPLiteral {
    private final IntegerHolder z, x;
    private final IntegerVariable y;

    public EqMul(final IntegerHolder z, final IntegerHolder x, final IntegerVariable y) {
        this.z = z;
        this.x = x;
        this.y = y;
    }

    @Override
    public ArithmeticLiteral substitute(final Map<IntegerVariable, IntegerVariable> assignment) {
        final IntegerVariable newY = assignment.getOrDefault(y, y);
        final IntegerHolder newX;
        final IntegerHolder newZ;
        if (this.x instanceof IntegerVariable) {
            final IntegerVariable x = (IntegerVariable) this.x;
            newX = assignment.getOrDefault(x, x);
        } else {
            newX = this.x;
        }
        if (this.z instanceof IntegerVariable) {
            final IntegerVariable z = (IntegerVariable) this.z;
            newZ = assignment.getOrDefault(z, z);
        } else {
            newZ = this.z;
        }
        if (newY == y && newX == x && newZ == z) {
            return this;
        } else {
            return new EqMul(newZ, newX, newY);
        }
    }

    @Override
    public Set<IntegerVariable> getVariables() {
        final Set<IntegerVariable> set = new TreeSet<>();
        set.add(y);
        if (x instanceof IntegerVariable) {
            set.add((IntegerVariable) x);
        }
        if (z instanceof IntegerVariable) {
            set.add((IntegerVariable) z);
        }
        return set;
    }

    @Override
    public boolean isUnsat() {
        return false;
    }

    @Override
    public boolean isValid() {
        final IntegerDomain zd = z.getDomain();
        final IntegerDomain xd = x.getDomain();
        final IntegerDomain yd = y.getDomain();
        if (zd.size() != 1) {
            return false;
        }
        if (xd.size() != 1 && yd.size() != 1) {
            return false;
        }
        return zd.ub() == xd.ub() * yd.ub();
    }

    @Override
    public int getUpperBound() {
        return Math.max(Math.max(x.getDomain().ub(), y.getDomain().ub()), y.getDomain().ub());
    }

    public IntegerHolder getZ() {
        return z;
    }

    public IntegerHolder getX() {
        return x;
    }

    public IntegerVariable getY() {
        return y;
    }
}
