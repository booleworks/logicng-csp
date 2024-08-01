package com.booleworks.logicng.csp.terms;

import com.booleworks.logicng.csp.CspFactory;
import com.booleworks.logicng.csp.IntegerClause;
import com.booleworks.logicng.csp.IntegerDomain;
import com.booleworks.logicng.csp.LinearExpression;
import com.booleworks.logicng.formulas.Formula;

import java.util.Set;
import java.util.TreeSet;

public class AbsoluteFunction extends UnaryFunction {
    public final static String ABS_AUX_VARIABLE = "ABS";

    public AbsoluteFunction(final Term operand) {
        super(Term.Type.ABS, operand);
    }

    @Override
    protected Decomposition calculateDecomposition(final CspFactory cf) {
        final Decomposition decomposition = this.getOperand().decompose(cf);
        final LinearExpression expression = decomposition.getLinearExpression();
        final Set<IntegerClause> constraints = new TreeSet<>(decomposition.getAdditionalConstraints());
        final IntegerDomain domain = expression.getDomain();
        if (domain.lb() >= 0) {
            return new Decomposition(expression, constraints);
        } else if (domain.ub() <= 0) {
            return new Decomposition(LinearExpression.multiply(expression, -1), constraints);
        }
        final IntegerDomain newDomain = domain.abs();
        final IntegerVariable newVariable = cf.auxVariable(ABS_AUX_VARIABLE, newDomain);
        final Formula positiveFormula = cf.eq(newVariable, this.getOperand());
        final Formula negativeFormula = cf.eq(cf.minus(newVariable), this.getOperand());
        constraints.addAll(cf.decompose(cf.formulaFactory().or(positiveFormula, negativeFormula)));
        return new Decomposition(new LinearExpression(newVariable), constraints);
    }
}
