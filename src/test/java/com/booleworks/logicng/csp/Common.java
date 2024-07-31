package com.booleworks.logicng.csp;

import com.booleworks.logicng.csp.literals.ArithmeticLiteral;
import com.booleworks.logicng.formulas.Literal;

import java.util.List;
import java.util.TreeSet;

public class Common {
    public static <G extends Comparable<? extends G>> TreeSet<G> treeSetFrom(final G... elms) {
        return new TreeSet<>(List.of(elms));
    }

    public static IntegerClause integerClauseFrom(final Literal v, final ArithmeticLiteral a) {
        return new IntegerClause(treeSetFrom(v), treeSetFrom(a));
    }
}
