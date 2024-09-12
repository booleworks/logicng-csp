package com.booleworks.logicng.csp.terms;

import com.booleworks.logicng.csp.CspFactory;
import com.booleworks.logicng.csp.IntegerClause;
import com.booleworks.logicng.csp.LinearExpression;
import com.booleworks.logicng.formulas.Variable;

import java.util.LinkedHashSet;
import java.util.Set;

public final class SubtractionFunction extends BinaryFunction {
    public SubtractionFunction(final Term left, final Term right) {
        super(Term.Type.SUB, left, right);
    }

    @Override
    public Decomposition calculateDecomposition(final CspFactory cf) {
        final Decomposition resultLeft = left.decompose(cf);
        LinearExpression expression = resultLeft.getLinearExpression();
        final Set<IntegerClause> constraints = new LinkedHashSet<>(resultLeft.getAdditionalConstraints());
        final Set<IntegerVariable> intVars = new LinkedHashSet<>(resultLeft.getAuxiliaryIntegerVariables());
        final Set<Variable> boolVars = new LinkedHashSet<>(resultLeft.getAuxiliaryBooleanVariables());
        final Decomposition resultRight = right.decompose(cf);
        expression = LinearExpression.add(expression, LinearExpression.multiply(resultRight.getLinearExpression(), -1));
        constraints.addAll(resultRight.getAdditionalConstraints());
        intVars.addAll(resultRight.getAuxiliaryIntegerVariables());
        boolVars.addAll(resultRight.getAuxiliaryBooleanVariables());
        return new Decomposition(expression, constraints, intVars, boolVars);
    }

    @Override
    public boolean equals(final Object o) {
        return equals(o, true);
    }

    @Override
    public int hashCode() {
        return hashCode(true);
    }
}
