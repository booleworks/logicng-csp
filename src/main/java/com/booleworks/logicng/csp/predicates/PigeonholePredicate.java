package com.booleworks.logicng.csp.predicates;

import com.booleworks.logicng.csp.CspFactory;
import com.booleworks.logicng.csp.IntegerClause;
import com.booleworks.logicng.csp.terms.Term;
import com.booleworks.logicng.formulas.FormulaFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class PigeonholePredicate extends CspPredicate {
    List<Term> terms;

    public PigeonholePredicate(final Collection<Term> terms, final FormulaFactory f) {
        super(CspPredicate.Type.PIGEONHOLE, f);
        this.terms = new ArrayList<>(terms);
    }

    @Override
    public AllDifferentPredicate negate(final CspFactory cf) {
        return cf.allDifferent(terms);
    }

    @Override
    protected Set<IntegerClause> calculateDecomposition(final CspFactory cf) {
        final FormulaFactory f = factory();
        List<CspPredicate> eqs = new ArrayList<>();
        for (int i = 0; i < terms.size(); i++) {
            for (int j = i + 1; j < terms.size(); j++) {
                eqs.add(cf.eq(terms.get(i), terms.get(j)));
            }
        }
        return cf.decompose(f.or(eqs));
    }

    @Override
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (getClass() == other.getClass()) {
            if (factory() == ((com.booleworks.logicng.csp.predicates.PigeonholePredicate) other).factory()) {
                return false; // the same factory would have produced a == object
            }
            return Objects.equals(terms, ((com.booleworks.logicng.csp.predicates.PigeonholePredicate) other).terms);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(terms);
    }

    @Override
    public String toString() {
        return type + "(" + terms.stream().map(Object::toString).collect(Collectors.joining(", ")) + ")";
    }
}
