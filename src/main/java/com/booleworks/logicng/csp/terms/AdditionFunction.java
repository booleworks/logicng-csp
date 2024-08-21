package com.booleworks.logicng.csp.terms;

import com.booleworks.logicng.csp.CspFactory;
import com.booleworks.logicng.csp.IntegerClause;
import com.booleworks.logicng.csp.LinearExpression;
import com.booleworks.logicng.formulas.Variable;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

public final class AdditionFunction extends NAryFunction {
    public AdditionFunction(final LinkedHashSet<Term> terms) {
        super(Term.Type.ADD, terms);
    }

    @Override
    public Decomposition calculateDecomposition(final CspFactory cf) {
        LinearExpression.Builder expression = new LinearExpression.Builder(0);
        final Set<IntegerClause> constraints = new TreeSet<>();
        final Set<IntegerVariable> auxIntVars = new TreeSet<>();
        final Set<Variable> auxBoolVars = new TreeSet<>();
        for (final Term operand : operands) {
            final Decomposition ei = operand.decompose(cf);
            expression = expression.add(ei.getLinearExpression());
            constraints.addAll(ei.getAdditionalConstraints());
            auxIntVars.addAll(ei.getAuxiliaryIntegerVariables());
            auxBoolVars.addAll(ei.getAuxiliaryBooleanVariables());
        }
        return new Decomposition(expression.build(), constraints, auxIntVars, auxBoolVars);
    }

}
