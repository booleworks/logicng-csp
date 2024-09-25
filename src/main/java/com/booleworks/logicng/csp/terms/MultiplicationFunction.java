package com.booleworks.logicng.csp.terms;

import com.booleworks.logicng.csp.CspFactory;
import com.booleworks.logicng.csp.datastructures.LinearExpression;

/**
 * A function term representing the multiplication operation.
 */
public final class MultiplicationFunction extends BinaryFunction {
    /**
     * Constructs a new multiplication function term.
     * <p>
     * <B>This constructor should not be used!</B> Use {@link CspFactory} to create new terms.
     * @param left  the constant factor
     * @param right the variable
     */
    public MultiplicationFunction(final IntegerConstant left, final Term right) {
        super(Term.Type.MUL, left, right);
    }

    @Override
    public Decomposition calculateDecomposition(final CspFactory cf) {
        final Decomposition resultRight = right.decompose(cf);
        final LinearExpression exp = LinearExpression.multiply(resultRight.getLinearExpression(), getLeft().getValue());
        return new Decomposition(exp, resultRight.getAdditionalConstraints(),
                resultRight.getAuxiliaryIntegerVariables(), resultRight.getAuxiliaryBooleanVariables());
    }

    @Override
    public IntegerConstant getLeft() {
        return (IntegerConstant) super.getLeft();
    }

    @Override
    public boolean equals(final Object o) {
        return equals(o, false);
    }

    @Override
    public int hashCode() {
        return hashCode(false);
    }
}
