package com.booleworks.logicng.csp.literals;

import com.booleworks.logicng.csp.terms.IntegerVariable;

import java.util.Map;
import java.util.Set;

public interface ArithmeticLiteral {
    boolean isValid();

    boolean isUnsat();

    Set<IntegerVariable> getVariables();

    ArithmeticLiteral substitute(Map<IntegerVariable, IntegerVariable> assignment);
}
