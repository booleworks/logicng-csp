package com.booleworks.logicng.csp.terms;

import com.booleworks.logicng.csp.CspFactory;
import com.booleworks.logicng.csp.IntegerClause;
import com.booleworks.logicng.csp.IntegerDomain;
import com.booleworks.logicng.csp.LinearExpression;

import java.util.Set;
import java.util.TreeSet;

public class ModuloFunction extends BinaryFunction {
    public final static String MOD_AUX_VARIABLE = "MOD";

    public ModuloFunction(final Term left, final IntegerConstant right) {
        super(Term.Type.MOD, left, right);
    }

    @Override
    public Decomposition calculateDecomposition(final CspFactory cf) {
        final Decomposition resultLeft = left.decompose(cf);
        final Set<IntegerClause> constraints = new TreeSet<>(resultLeft.getAdditionalConstraints());
        final IntegerDomain domainLeft = resultLeft.getLinearExpression().getDomain();
        final int rightValue = getRight().getValue();
        final IntegerVariable q = cf.auxVariable(MOD_AUX_VARIABLE, domainLeft.div(rightValue));
        final IntegerVariable r = cf.auxVariable(MOD_AUX_VARIABLE, domainLeft.mod(rightValue));
        final Term px = cf.mul(getRight(), q);
        constraints.addAll(cf.eq(this.left, cf.add(px, r)).decompose(cf));
        constraints.addAll(cf.ge(r, cf.zero()).decompose(cf));
        constraints.addAll(cf.gt(cf.constant(Math.abs(rightValue)), r).decompose(cf));
        return new Decomposition(new LinearExpression(r), constraints);
    }

    @Override
    public IntegerConstant getRight() {
        return (IntegerConstant) super.getRight();
    }

}
