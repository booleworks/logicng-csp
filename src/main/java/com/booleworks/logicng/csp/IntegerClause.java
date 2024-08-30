package com.booleworks.logicng.csp;

import com.booleworks.logicng.csp.literals.ArithmeticLiteral;
import com.booleworks.logicng.csp.predicates.CspPredicate;
import com.booleworks.logicng.csp.terms.IntegerVariable;
import com.booleworks.logicng.formulas.Literal;
import com.booleworks.logicng.formulas.Variable;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IntegerClause {
    private final Set<Literal> boolLiterals;
    private final Set<ArithmeticLiteral> arithLiterals;

    public IntegerClause(final Set<Literal> boolLiterals, final Set<ArithmeticLiteral> arithLiterals) {
        this.boolLiterals = boolLiterals;
        this.arithLiterals = arithLiterals;
    }

    public IntegerClause(final Literal booLiteral) {
        this(new LinkedHashSet<>(), Collections.emptySortedSet());
        boolLiterals.add(booLiteral);
    }

    public IntegerClause(final ArithmeticLiteral arithLiteral) {
        this(Collections.emptySortedSet(), new LinkedHashSet<>());
        arithLiterals.add(arithLiteral);
    }

    public IntegerClause() {
        this.boolLiterals = Collections.emptySortedSet();
        this.arithLiterals = Collections.emptySortedSet();
    }

    public Set<Literal> getBoolLiterals() {
        return boolLiterals;
    }

    public Set<ArithmeticLiteral> getArithmeticLiterals() {
        return arithLiterals;
    }

    public Set<IntegerVariable> getCommonVariables() {
        if (!boolLiterals.isEmpty()) {
            return Collections.emptySet();
        }
        Set<IntegerVariable> commonVars = null;
        for (final ArithmeticLiteral lit : arithLiterals) {
            final Set<IntegerVariable> vs = lit.getVariables();
            if (commonVars == null) {
                commonVars = vs;
            } else {
                commonVars = commonVars.stream().filter(vs::contains).collect(Collectors.toSet());
            }
        }
        return Objects.requireNonNullElse(commonVars, Collections.emptySet());
    }

    public int size() {
        return boolLiterals.size() + arithLiterals.size();
    }

    public boolean isValid() {
        for (final ArithmeticLiteral lit : arithLiterals) {
            if (lit.isValid()) {
                return true;
            }
        }
        return false;
    }

    public boolean isUnsat() {
        for (final ArithmeticLiteral lit : arithLiterals) {
            if (!lit.isUnsat()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("CLAUSE<");
        final String lits = Stream.concat(
                boolLiterals.stream().map(Object::toString),
                arithLiterals.stream().map(Object::toString)
        ).collect(Collectors.joining(", "));
        builder.append(lits);
        builder.append(">");
        return builder.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}

        final IntegerClause that = (IntegerClause) o;

        if (!boolLiterals.equals(that.boolLiterals)) {return false;}
        return arithLiterals.equals(that.arithLiterals);
    }

    @Override
    public int hashCode() {
        int result = boolLiterals.hashCode();
        result = 31 * result + arithLiterals.hashCode();
        return result;
    }

    public static CspPredicate.Decomposition factorize(final CspPredicate.Decomposition left, final CspPredicate.Decomposition right) {
        final Set<IntegerClause> clauses = new LinkedHashSet<>();
        for (final IntegerClause l : left.getClauses()) {
            for (final IntegerClause r : right.getClauses()) {
                final SortedSet<Literal> newBools = new TreeSet<>(l.boolLiterals);
                final SortedSet<ArithmeticLiteral> newAriths = new TreeSet<>(l.arithLiterals);
                newBools.addAll(r.boolLiterals);
                newAriths.addAll(r.arithLiterals);
                clauses.add(new IntegerClause(newBools, newAriths));
            }
        }
        final Set<IntegerVariable> intVars = new TreeSet<>(left.getAuxiliaryIntegerVariables());
        final Set<Variable> boolVars = new TreeSet<>(left.getAuxiliaryBooleanVariables());
        intVars.addAll(right.getAuxiliaryIntegerVariables());
        boolVars.addAll(right.getAuxiliaryBooleanVariables());
        return new CspPredicate.Decomposition(clauses, intVars, boolVars);
    }
}
