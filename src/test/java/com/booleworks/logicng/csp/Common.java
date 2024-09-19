package com.booleworks.logicng.csp;

import com.booleworks.logicng.csp.datastructures.IntegerClause;
import com.booleworks.logicng.csp.literals.ArithmeticLiteral;
import com.booleworks.logicng.formulas.Literal;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Common {
    @SafeVarargs
    public static <G> Set<G> setFrom(final G... elms) {
        return new LinkedHashSet<>(List.of(elms));
    }

    public static IntegerClause integerClauseFrom(final Literal v, final ArithmeticLiteral a) {
        return new IntegerClause(setFrom(v), setFrom(a));
    }
}
