package com.booleworks.logicng.csp.terms;

import com.booleworks.logicng.csp.CspFactory;
import com.booleworks.logicng.csp.IntegerClause;
import com.booleworks.logicng.csp.LinearExpression;
import com.booleworks.logicng.csp.predicates.CspPredicate;
import com.booleworks.logicng.formulas.Variable;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public abstract class Term {

    protected final Type type;
    protected Decomposition decompositionResult;

    protected Term(final Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public abstract boolean isAtom();

    public SortedSet<IntegerVariable> variables(final CspFactory cf) {
        final SortedSet<IntegerVariable> set = new TreeSet<>();
        variablesInplace(set);
        return set;
    }

    public abstract void variablesInplace(SortedSet<IntegerVariable> variables);

    protected abstract Decomposition calculateDecomposition(final CspFactory cf);

    public final Decomposition decompose(final CspFactory cf) {
        if (decompositionResult == null) {
            decompositionResult = calculateDecomposition(cf);
        }
        return decompositionResult;
    }

    public enum Type {
        ZERO, ONE, CONST, VAR, NEG, ADD, SUB, MUL, MOD, DIV, MAX, MIN, ABS
    }

    public static final class Decomposition {
        private final LinearExpression linearExpression;
        private final Set<IntegerClause> additionalConstraints;

        private final Set<IntegerVariable> auxiliaryIntegerVariables;
        private final Set<Variable> auxiliaryBooleanVariables;

        public Decomposition(final LinearExpression linearExpression, final Set<IntegerClause> additionalConstraints, final Set<IntegerVariable> auxiliaryIntegerVariables,
                             final Set<Variable> auxiliaryBooleanVariables) {
            this.linearExpression = linearExpression;
            this.additionalConstraints = additionalConstraints;
            this.auxiliaryIntegerVariables = auxiliaryIntegerVariables;
            this.auxiliaryBooleanVariables = auxiliaryBooleanVariables;
        }

        public LinearExpression getLinearExpression() {
            return linearExpression;
        }

        public Set<IntegerClause> getAdditionalConstraints() {
            return Collections.unmodifiableSet(additionalConstraints);
        }

        public Set<IntegerVariable> getAuxiliaryIntegerVariables() {
            return Collections.unmodifiableSet(auxiliaryIntegerVariables);
        }

        public Set<Variable> getAuxiliaryBooleanVariables() {
            return Collections.unmodifiableSet(auxiliaryBooleanVariables);
        }

        public static Decomposition merge(final Decomposition term, final Collection<CspPredicate.Decomposition> predicateDecompositions) {
            final Set<IntegerClause> clauses = new LinkedHashSet<>();
            final Set<IntegerVariable> intVars = new TreeSet<>(term.getAuxiliaryIntegerVariables());
            final Set<Variable> boolVars = new TreeSet<>(term.getAuxiliaryBooleanVariables());
            for (final CspPredicate.Decomposition predDecomp : predicateDecompositions) {
                clauses.addAll(predDecomp.getClauses());
                intVars.addAll(predDecomp.getAuxiliaryIntegerVariables());
                boolVars.addAll(predDecomp.getAuxiliaryBooleanVariables());
            }
            return new Decomposition(term.linearExpression, clauses, intVars, boolVars);
        }

        @Override
        public String toString() {
            return "IntegerTerm.Decomposition{" +
                    "linearExpression=" + linearExpression.toString() +
                    ", additionalConstraints=" + additionalConstraints.toString() +
                    ", auxiliaryIntegerVariables=" + auxiliaryIntegerVariables.toString() +
                    ", auxiliaryBooleanVariables=" + auxiliaryBooleanVariables.toString() +
                    '}';
        }
    }
}
