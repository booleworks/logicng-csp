package com.booleworks.logicng.csp.predicates;

import com.booleworks.logicng.csp.CspFactory;
import com.booleworks.logicng.csp.IntegerClause;
import com.booleworks.logicng.csp.terms.IntegerVariable;
import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.formulas.Predicate;
import com.booleworks.logicng.formulas.Variable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public abstract class CspPredicate implements Predicate {

    protected final Type type;
    protected Decomposition decomposition;
    private final FormulaFactory f;

    protected CspPredicate(final Type type, final FormulaFactory f) {
        this.type = type;
        this.f = f;
    }

    public Type getType() {
        return type;
    }

    public abstract Formula negate(final CspFactory cf);

    public SortedSet<IntegerVariable> variables() {
        final SortedSet<IntegerVariable> variables = new TreeSet<>();
        variablesInplace(variables);
        return variables;
    }

    public abstract void variablesInplace(SortedSet<IntegerVariable> variables);

    protected abstract Decomposition calculateDecomposition(final CspFactory cf);

    public Decomposition decompose(final CspFactory cf) {
        if (decomposition == null) {
            decomposition = calculateDecomposition(cf);
        }
        return decomposition;
    }

    @Override
    public FormulaFactory factory() {
        return f;
    }

    public enum Type {
        EQ, NE, LE, LT, GE, GT, ALLDIFFERENT
    }

    public final static class Decomposition {
        final Set<IntegerClause> clauses;
        final Set<IntegerVariable> auxiliaryIntegerVariables;
        final Set<Variable> auxiliaryBooleanVariables;

        public Decomposition(final Set<IntegerClause> clauses, final Set<IntegerVariable> auxiliaryIntegerVariables, final Set<Variable> auxiliaryBooleanVariables) {
            this.clauses = clauses;
            this.auxiliaryIntegerVariables = auxiliaryIntegerVariables;
            this.auxiliaryBooleanVariables = auxiliaryBooleanVariables;
        }

        public Set<IntegerClause> getClauses() {
            return Collections.unmodifiableSet(clauses);
        }

        public Set<IntegerVariable> getAuxiliaryIntegerVariables() {
            return Collections.unmodifiableSet(auxiliaryIntegerVariables);
        }

        public Set<Variable> getAuxiliaryBooleanVariables() {
            return Collections.unmodifiableSet(auxiliaryBooleanVariables);
        }

        public static Decomposition merge(final Collection<Decomposition> decomps) {
            final Set<IntegerClause> clauses = new TreeSet<>();
            final Set<IntegerVariable> auxiliaryIntegerVariables = new TreeSet<>();
            final Set<Variable> auxiliaryBooleanVariables = new TreeSet<>();
            for (final Decomposition decomp : decomps) {
                clauses.addAll(decomp.getClauses());
                auxiliaryIntegerVariables.addAll(decomp.getAuxiliaryIntegerVariables());
                auxiliaryBooleanVariables.addAll(decomp.getAuxiliaryBooleanVariables());
            }
            return new Decomposition(clauses, auxiliaryIntegerVariables, auxiliaryBooleanVariables);
        }

        public static Decomposition merge(final Decomposition... decomps) {
            return merge(List.of(decomps));
        }

        public static Decomposition empty() {
            return new Decomposition(Collections.emptySet(), Collections.emptySet(), Collections.emptySet());
        }

        public static Decomposition emptyClause() {
            return new Decomposition(Collections.singleton(new IntegerClause()), Collections.emptySet(), Collections.emptySet());
        }
    }
}
