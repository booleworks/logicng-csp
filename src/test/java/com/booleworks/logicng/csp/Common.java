package com.booleworks.logicng.csp;

import com.booleworks.logicng.csp.literals.ArithmeticLiteral;
import com.booleworks.logicng.formulas.Literal;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Common {
    public static <G extends Comparable<? extends G>> TreeSet<G> treeSetFrom(final G... elms) {
        return new TreeSet<>(List.of(elms));
    }

    public static <G> Set<G> setFrom(final G... elms) {
        return new LinkedHashSet<>(List.of(elms));
    }

    public static IntegerClause integerClauseFrom(final Literal v, final ArithmeticLiteral a) {
        return new IntegerClause(setFrom(v), setFrom(a));
    }
}
