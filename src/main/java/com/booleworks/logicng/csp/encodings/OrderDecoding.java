package com.booleworks.logicng.csp.encodings;

import com.booleworks.logicng.csp.Csp;
import com.booleworks.logicng.csp.CspAssignment;
import com.booleworks.logicng.csp.CspFactory;
import com.booleworks.logicng.csp.IntegerDomain;
import com.booleworks.logicng.csp.datastructures.IntegerVariableSubstitution;
import com.booleworks.logicng.csp.terms.IntegerVariable;
import com.booleworks.logicng.datastructures.Assignment;
import com.booleworks.logicng.formulas.Literal;
import com.booleworks.logicng.formulas.Variable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class OrderDecoding {
    public static CspAssignment decode(final Assignment model, final Collection<IntegerVariable> integerVariables,
                                       final Collection<Variable> booleanVariables,
                                       final IntegerVariableSubstitution propagateSubstitution,
                                       final OrderEncodingContext context,
                                       final CspFactory cf) {
        final CspAssignment result = new CspAssignment();
        for (final IntegerVariable v : integerVariables) {
            final int value = decodeIntVar(propagateSubstitution.getOrSelf(v), model, context);
            result.addIntAssignment(v, value);
        }
        for (final Variable v : booleanVariables) {
            if (model.positiveVariables().contains(v)) {
                result.addPos(v);
            }
            final Literal negV = v.negate(cf.formulaFactory());
            if (model.negativeLiterals().contains(negV)) {
                result.addNeg(negV);
            }
        }
        return result;
    }

    public static CspAssignment decode(final Assignment model, final Collection<IntegerVariable> integerVariables, final Collection<Variable> booleanVariables,
                                       final OrderEncodingContext context, final CspFactory cf) {
        return decode(model, integerVariables, booleanVariables, new IntegerVariableSubstitution(), context, cf);
    }

    public static CspAssignment decode(final Assignment model, final Collection<IntegerVariable> integerVariables, final OrderEncodingContext context, final CspFactory cf) {
        return decode(model, integerVariables, Collections.emptyList(), new IntegerVariableSubstitution(), context, cf);
    }

    public static CspAssignment decode(final Assignment model, final Csp csp, final OrderEncodingContext context, final CspFactory cf) {
        return decode(model, csp.getVisibleIntegerVariables(), csp.getVisibleBooleanVariables(), csp.getPropagateSubstitutions(), context, cf);
    }

    static int decodeIntVar(final IntegerVariable var, final Assignment model, final OrderEncodingContext context) {
        final IntegerDomain domain = var.getDomain();
        final int lb = domain.lb();
        final int ub = domain.ub();
        int value = ub;
        final Map<Integer, Variable> varMap = context.getVariableMap().get(var);
        if (varMap == null) {
            return value;
        }
        int index = 0;
        for (int c = lb; c < ub; c++) {
            if (domain.contains(c)) {
                final Variable satVar = varMap.get(index);
                if (satVar != null && model.positiveVariables().contains(satVar)) {
                    value = c;
                    break;
                }
                ++index;
            }
        }
        return value;
    }
}
