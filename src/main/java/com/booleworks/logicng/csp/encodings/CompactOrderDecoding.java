package com.booleworks.logicng.csp.encodings;

import com.booleworks.logicng.csp.CspAssignment;
import com.booleworks.logicng.csp.CspFactory;
import com.booleworks.logicng.csp.terms.IntegerVariable;
import com.booleworks.logicng.datastructures.Assignment;
import com.booleworks.logicng.formulas.Literal;
import com.booleworks.logicng.formulas.Variable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CompactOrderDecoding {
    public static CspAssignment decode(final Assignment model, final Collection<IntegerVariable> integerVariables,
                                       final Collection<Variable> booleanVariables,
                                       final Map<IntegerVariable, IntegerVariable> reverseSubstitution,
                                       final CspEncodingContext context,
                                       final CspFactory cf) {
        final CspAssignment result = new CspAssignment();
        for (final IntegerVariable v : integerVariables) {
            final int value = decodeIntVar(v, model, context);
            //FIXME: I don't understand the substitution. In my mind, the user passes the original variables and not the substitute.
            result.addIntAssignment(reverseSubstitution.getOrDefault(v, v), value);
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

    static int decodeIntVar(final IntegerVariable var, final Assignment model, final CspEncodingContext context) {
        final List<IntegerVariable> digits = context.getDigits().get(var);
        if (digits == null || digits.size() <= 1) {
            return OrderDecoding.decodeIntVar(var, model, context); //FIXME: Consider substitution
        } else {
            return decodeBigIntVar(var, model, context);
        }
    }

    static int decodeBigIntVar(final IntegerVariable var, final Assignment model, final CspEncodingContext context) {
        final List<IntegerVariable> digits = context.getDigits().get(var);
        assert digits != null && digits.size() > 1;
        final int b = context.getBase();
        int dbase = 1;
        int value = context.getOffsets().get(var); //FIXME: Consider substitution
        for (final IntegerVariable digit : digits) {
            final int d = OrderDecoding.decodeIntVar(digit, model, context); //FIXME: Consider substitution
            value += dbase * d;
            dbase *= b;
        }
        return value;
    }
}
