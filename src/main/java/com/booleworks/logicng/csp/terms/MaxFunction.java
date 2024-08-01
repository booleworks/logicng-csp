package com.booleworks.logicng.csp.terms;

import com.booleworks.logicng.csp.CspFactory;
import com.booleworks.logicng.csp.IntegerClause;
import com.booleworks.logicng.csp.IntegerDomain;
import com.booleworks.logicng.csp.LinearExpression;
import com.booleworks.logicng.formulas.Formula;

import java.util.Set;
import java.util.TreeSet;

public class MaxFunction extends BinaryFunction {
    public final static String MAX_AUX_VARIABLE = "MAX";

    public MaxFunction(final Term left, final Term right) {
        super(Term.Type.MAX, left, right);
    }

    @Override
    protected Decomposition calculateDecomposition(final CspFactory cf) {
        final Decomposition resultLeft = this.left.decompose(cf);
        final Decomposition resultRight = this.right.decompose(cf);
        final IntegerDomain domainLeft = resultLeft.getLinearExpression().getDomain();
        final IntegerDomain domainRight = resultRight.getLinearExpression().getDomain();

        if (domainLeft.ub() <= domainRight.lb()) {
            return resultRight;
        } else if (domainRight.ub() <= domainLeft.lb()) {
            return resultLeft;
        }

        final IntegerDomain newDomain = domainLeft.max(domainRight);
        final IntegerVariable x = cf.auxVariable(MAX_AUX_VARIABLE, newDomain);
        final Set<IntegerClause> constraints = new TreeSet<>(resultLeft.getAdditionalConstraints());
        constraints.addAll(resultRight.getAdditionalConstraints());
        constraints.addAll(cf.ge(x, this.left).decompose(cf));
        constraints.addAll(cf.ge(x, this.right).decompose(cf));
        final Formula leLeft = cf.le(x, this.left);
        final Formula leRight = cf.le(x, this.right);
        constraints.addAll(cf.decompose(cf.formulaFactory().or(leLeft, leRight)));
        return new Decomposition(new LinearExpression(x), constraints);
    }
}
