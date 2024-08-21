package com.booleworks.logicng.csp.terms;

import com.booleworks.logicng.csp.CspFactory;
import com.booleworks.logicng.csp.IntegerDomain;
import com.booleworks.logicng.csp.LinearExpression;
import com.booleworks.logicng.csp.predicates.CspPredicate;

import java.util.List;
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
        final Set<IntegerVariable> intVars = new TreeSet<>(resultLeft.getAuxiliaryIntegerVariables());
        final IntegerDomain domainLeft = resultLeft.getLinearExpression().getDomain();
        final int rightValue = getRight().getValue();
        final IntegerVariable q = cf.auxVariable(MOD_AUX_VARIABLE, domainLeft.div(rightValue));
        final IntegerVariable r = cf.auxVariable(MOD_AUX_VARIABLE, domainLeft.mod(rightValue));
        intVars.add(q);
        intVars.add(r);
        final Term px = cf.mul(getRight(), q);
        final CspPredicate.Decomposition d1 = cf.eq(this.left, cf.add(px, r)).decompose(cf);
        final CspPredicate.Decomposition d2 = cf.ge(r, cf.zero()).decompose(cf);
        final CspPredicate.Decomposition d3 = cf.gt(cf.constant(Math.abs(rightValue)), r).decompose(cf);
        final Term.Decomposition newTerm = new Decomposition(new LinearExpression(r), resultLeft.getAdditionalConstraints(), intVars, resultLeft.getAuxiliaryBooleanVariables());
        return Term.Decomposition.merge(newTerm, List.of(d1, d2, d3));
    }

    @Override
    public IntegerConstant getRight() {
        return (IntegerConstant) super.getRight();
    }

}
