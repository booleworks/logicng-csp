package com.booleworks.logicng.csp.encodings;

import com.booleworks.logicng.csp.Csp;
import com.booleworks.logicng.csp.CspAssignment;
import com.booleworks.logicng.csp.CspFactory;
import com.booleworks.logicng.csp.datastructures.IntegerVariableSubstitution;
import com.booleworks.logicng.csp.terms.IntegerVariable;
import com.booleworks.logicng.datastructures.Assignment;
import com.booleworks.logicng.formulas.Literal;
import com.booleworks.logicng.formulas.Variable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class CompactOrderDecoding {
    public static CspAssignment decode(final Assignment model, final Collection<IntegerVariable> integerVariables,
                                       final Collection<Variable> booleanVariables,
                                       final IntegerVariableSubstitution propagateSubstitution,
                                       final CompactOrderEncodingContext context,
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
                                       final CompactOrderEncodingContext context, final CspFactory cf) {
        return decode(model, integerVariables, booleanVariables, new IntegerVariableSubstitution(), context, cf);
    }

    public static CspAssignment decode(final Assignment model, final Collection<IntegerVariable> integerVariables, final CompactOrderEncodingContext context, final CspFactory cf) {
        return decode(model, integerVariables, Collections.emptyList(), new IntegerVariableSubstitution(), context, cf);
    }

    public static CspAssignment decode(final Assignment model, final Csp csp, final CompactOrderEncodingContext context, final CspFactory cf) {
        return decode(model, csp.getVisibleIntegerVariables(), csp.getVisibleBooleanVariables(), csp.getPropagateSubstitutions(), context, cf);
    }

    static int decodeIntVar(final IntegerVariable var, final Assignment model, final CompactOrderEncodingContext context) {
        final IntegerVariable adjusted = context.getAdjustedVariableOrSelf(var);
        final List<IntegerVariable> digits = context.getDigits(adjusted);
        assert Objects.nonNull(digits);
        return decodeBigIntVar(adjusted, model, context);
    }

    static int decodeBigIntVar(final IntegerVariable var, final Assignment model, final CompactOrderEncodingContext context) {
        final List<IntegerVariable> digits = context.getDigits(var);
        assert digits != null;
        final int b = context.getBase();
        int dbase = 1;
        int value = context.hasOffset(var) ? context.getOffset(var) : 0;
        for (final IntegerVariable digit : digits) {
            final int d = OrderDecoding.decodeIntVar(digit, model, context.getOrderContext());
            value += dbase * d;
            dbase *= b;
        }
        return value;
    }
}
