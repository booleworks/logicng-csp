package com.booleworks.logicng.csp;

import com.booleworks.logicng.csp.literals.ArithmeticLiteral;
import com.booleworks.logicng.csp.terms.IntegerVariable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class CspPropagation {
    public static String BOUNDED_AUX_VAR = "PROPAGATION";

    public static Csp propagate(final Csp csp, final CspFactory cf) {
        final Map<IntegerVariable, IntegerVariable> restrictions = new TreeMap<>();
        final Map<IntegerVariable, IntegerVariable> reverseSubstitutions = new HashMap<>();
        boolean changed = true;
        while (changed) {
            changed = false;
            for (final IntegerClause clause : csp.getClauses()) {
                if (calculateNewBounds(clause, restrictions, reverseSubstitutions, cf)) {
                    changed = true;
                }
            }
        }
        if (!restrictions.isEmpty()) {
            final Set<IntegerClause> newClauses =
                    csp.getClauses().stream().map(c -> rebuildClause(c, restrictions)).filter(c -> !c.isValid()).collect(Collectors.toSet());
            return Csp.fromClauses(newClauses, reverseSubstitutions);
        } else {
            return csp;
        }
    }

    private static boolean calculateNewBounds(final IntegerClause clause, final Map<IntegerVariable, IntegerVariable> restrictions,
                                              final Map<IntegerVariable, IntegerVariable> reverseSubstitutions, final CspFactory cf) {
        boolean changed = false;
        for (final IntegerVariable v : clause.getCommonVariables()) {
            assert clause.getBoolLiterals().isEmpty();
            final IntegerVariable currentV = restrictions.getOrDefault(v, v);
            if (currentV.isUnsatisfiable()) {
                continue;
            }
            int[] bound = null;
            for (final ArithmeticLiteral lit : clause.getArithmeticLiterals()) {
                final int[] b = lit.getBound(currentV, restrictions);
                if (b == null) {
                    bound = null;
                    break;
                } else {
                    if (bound == null) {
                        bound = b;
                    } else {
                        bound[0] = Math.min(bound[0], b[0]);
                        bound[1] = Math.max(bound[1], b[1]);
                    }
                }
            }
            if (bound != null && bound[0] <= bound[1]) {
                final IntegerVariable oldVar = restrictions.getOrDefault(v, v);
                final IntegerVariable newVar = boundVariable(oldVar, bound[0], bound[1], cf);
                if (newVar != oldVar) {
                    reverseSubstitutions.put(newVar, v);
                    restrictions.put(v, newVar);
                    changed = true;
                }
            }
        }
        return changed;
    }

    private static IntegerClause rebuildClause(final IntegerClause clause, final Map<IntegerVariable, IntegerVariable> assignment) {
        final TreeSet<ArithmeticLiteral> newLits = clause.getArithmeticLiterals().stream()
                .map(l -> l.substitute(assignment))
                .filter(Objects::nonNull)
                .filter(l -> !l.isUnsat())
                .collect(Collector.of(TreeSet::new, TreeSet::add, (left, right) -> {
                    left.addAll(right);
                    return left;
                }));
        if (newLits.equals(clause.getArithmeticLiterals())) {
            return clause;
        } else {
            return new IntegerClause(clause.getBoolLiterals(), newLits);
        }
    }

    private static IntegerVariable boundVariable(final IntegerVariable variable, final int lb, final int ub, final CspFactory cf) {
        final IntegerDomain d = variable.getDomain().bound(lb, ub);
        if (d == variable.getDomain()) {
            return variable;
        }
        return cf.auxVariable(BOUNDED_AUX_VAR, variable.getName(), d);
    }
}
