package com.booleworks.logicng.csp.terms;

import com.booleworks.logicng.csp.CspFactory;
import com.booleworks.logicng.csp.datastructures.LinearExpression;

public final class NegationFunction extends UnaryFunction {

    public NegationFunction(final Term operand) {
        super(Term.Type.NEG, operand);
    }

    @Override
    public Decomposition calculateDecomposition(final CspFactory cf) {
        final Decomposition result = operand.decompose(cf);
        return new Decomposition(LinearExpression.multiply(result.getLinearExpression(), -1),
                result.getAdditionalConstraints(), result.getAuxiliaryIntegerVariables(),
                result.getAuxiliaryBooleanVariables());
    }
}
