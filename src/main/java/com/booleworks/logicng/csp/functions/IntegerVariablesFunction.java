package com.booleworks.logicng.csp.functions;

import com.booleworks.logicng.csp.predicates.CspPredicate;
import com.booleworks.logicng.csp.terms.IntegerVariable;
import com.booleworks.logicng.formulas.Formula;

import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;

public class IntegerVariablesFunction {
    public static void integerVariablesInplace(final Formula formula, final SortedSet<IntegerVariable> variables) {
        final Stack<Formula> stack = new Stack<>();
        stack.push(formula);
        while (!stack.isEmpty()) {
            final Formula current = stack.pop();
            switch (current.type()) {
                case EQUIV:
                case IMPL:
                case OR:
                case AND:
                case NOT:
                    for (final Formula op : current) {
                        stack.add(op);
                    }
                    break;
                case PREDICATE:
                    if (current instanceof CspPredicate) {
                        ((CspPredicate) current).variablesInplace(variables);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public static SortedSet<IntegerVariable> integerVariables(final Formula formula) {
        final SortedSet<IntegerVariable> variables = new TreeSet<>();
        integerVariablesInplace(formula, variables);
        return variables;
    }
}
