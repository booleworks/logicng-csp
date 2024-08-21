package com.booleworks.logicng.csp;

import com.booleworks.logicng.csp.terms.IntegerVariable;
import com.booleworks.logicng.formulas.Literal;
import com.booleworks.logicng.formulas.Variable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class Csp {
    private Set<IntegerVariable> visibleIntegerVariables;
    private Set<IntegerVariable> internalIntegerVariables;
    private Set<Variable> visibleBooleanVariables;
    private Set<Variable> internalBooleanVariables;
    private Map<IntegerVariable, IntegerVariable> reverseSubstitutions;
    private Set<IntegerClause> clauses;

    private Csp() {
        this.internalIntegerVariables = new TreeSet<>();
        this.visibleIntegerVariables = new TreeSet<>();
        this.internalBooleanVariables = new TreeSet<>();
        this.visibleBooleanVariables = new TreeSet<>();
        this.clauses = new TreeSet<>();
        this.reverseSubstitutions = new HashMap<>();
    }

    private Csp(final Csp other) {
        this.internalIntegerVariables = new TreeSet<>(other.internalIntegerVariables);
        this.visibleIntegerVariables = new TreeSet<>(other.visibleIntegerVariables);
        this.internalBooleanVariables = new TreeSet<>(other.internalBooleanVariables);
        this.visibleBooleanVariables = new TreeSet<>(other.visibleBooleanVariables);
        this.clauses = new TreeSet<>(other.clauses);
        this.reverseSubstitutions = new HashMap<>(other.reverseSubstitutions);
    }

    public Set<IntegerVariable> getVisibleIntegerVariables() {
        return visibleIntegerVariables;
    }

    public Set<Variable> getVisibleBooleanVariables() {
        return visibleBooleanVariables;
    }

    public Set<IntegerVariable> getInternalIntegerVariables() {
        return internalIntegerVariables;
    }

    public Set<Variable> getInternalBooleanVariables() {
        return internalBooleanVariables;
    }

    public Set<IntegerClause> getClauses() {
        return clauses;
    }

    public Map<IntegerVariable, IntegerVariable> getReverseSubstitutions() {
        return reverseSubstitutions;
    }

    @Override
    public String toString() {
        return "Csp{" +
                "visibleIntegerVariables=" + visibleIntegerVariables +
                ", internIntegerVariables=" + internalIntegerVariables +
                ", visibleBooleanVariables=" + visibleBooleanVariables +
                ", internBooleanVariables=" + internalBooleanVariables +
                ", reverseSubstitutions=" + reverseSubstitutions +
                ", clauses=" + clauses +
                '}';
    }

    public static Csp fromClauses(final Set<IntegerClause> clauses) {
        return fromClauses(clauses, Collections.emptySet(), Collections.emptySet(), Collections.emptyMap());
    }

    public static Csp fromClauses(final Set<IntegerClause> clauses, final Set<IntegerVariable> integerVariables) {
        return fromClauses(clauses, integerVariables, Collections.emptySet(), Collections.emptyMap());
    }

    public static Csp fromClauses(final Set<IntegerClause> clauses, final Set<IntegerVariable> integerVariables, final Set<Variable> booleanVariables) {
        return fromClauses(clauses, integerVariables, booleanVariables, Collections.emptyMap());
    }

    public static Csp fromClauses(final Set<IntegerClause> clauses, final Set<IntegerVariable> integerVariables, final Map<IntegerVariable, IntegerVariable> reverseSubstitutions) {
        return fromClauses(clauses, integerVariables, Collections.emptySet(), reverseSubstitutions);
    }

    public static Csp fromClauses(final Set<IntegerClause> clauses, final Set<IntegerVariable> integerVariables, final Set<Variable> booleanVariables,
                                  final Map<IntegerVariable, IntegerVariable> reverseSubstitutions) {
        final Set<IntegerVariable> intVars = new TreeSet<>();
        final Set<Variable> boolVars = new TreeSet<>();
        for (final IntegerClause clause : clauses) {
            intVars.addAll(clause.getArithmeticLiterals().stream().flatMap(v -> v.getVariables().stream()).collect(Collectors.toSet()));
            boolVars.addAll(clause.getBoolLiterals().stream().map(Literal::variable).collect(Collectors.toSet()));
        }
        return new Csp.Builder()
                .updateInternalIntegerVariables(intVars)
                .updateInternalBooleanVariables(boolVars)
                .updateReverseSubstitution(reverseSubstitutions)
                .updateClauses(clauses)
                .updateVisibleIntegerVariables(integerVariables)
                .updateVisibleBooleanVariables(booleanVariables)
                .build();
    }

    public static class Builder {
        private Csp csp;

        public Builder() {
            csp = new Csp();
        }

        public Builder(final Csp csp) {
            this.csp = new Csp(csp);
        }

        public Builder addClause(final IntegerClause clause) {
            this.csp.clauses.add(clause);
            return this;
        }

        public Builder updateClauses(final Set<IntegerClause> clauses) {
            csp.clauses = clauses;
            return this;
        }

        public Builder updateInternalIntegerVariables(final Set<IntegerVariable> variables) {
            csp.internalIntegerVariables = variables;
            return this;
        }

        public Builder updateVisibleIntegerVariables(final Set<IntegerVariable> variables) {
            csp.visibleIntegerVariables = variables;
            return this;
        }

        public Builder updateInternalBooleanVariables(final Set<Variable> variables) {
            csp.internalBooleanVariables = variables;
            return this;
        }

        public Builder updateVisibleBooleanVariables(final Set<Variable> variables) {
            csp.visibleBooleanVariables = variables;
            return this;
        }

        public Builder updateReverseSubstitution(final Map<IntegerVariable, IntegerVariable> reverseSubstitution) {
            csp.reverseSubstitutions = reverseSubstitution;
            return this;
        }

        public boolean addInternalIntegerVariable(final IntegerVariable v) {
            return csp.internalIntegerVariables.add(v);
        }

        public boolean addVisibleIntegerVariable(final IntegerVariable v) {
            return csp.visibleIntegerVariables.add(v);
        }

        public boolean addInternalBooleanVariable(final Variable v) {
            return csp.internalBooleanVariables.add(v);
        }

        public boolean addVisibleBooleanVariable(final Variable v) {
            return csp.visibleBooleanVariables.add(v);
        }

        public Csp build() {
            final Csp csp = this.csp;
            this.csp = null;
            return csp;
        }

        public Set<IntegerVariable> getInternalIntegerVariables() {
            return csp.internalIntegerVariables;
        }

        public Set<IntegerVariable> getVisibleIntegerVariables() {
            return csp.visibleIntegerVariables;
        }

        public Set<Variable> getInternalBooleanVariables() {
            return csp.internalBooleanVariables;
        }

        public Set<Variable> getVisibleBooleanVariables() {
            return csp.visibleBooleanVariables;
        }

        public Set<IntegerClause> getClauses() {
            return csp.clauses;
        }

        @Override
        public String toString() {
            return csp.toString();
        }
    }
}
