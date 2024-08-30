package com.booleworks.logicng.csp.encodings;

import com.booleworks.logicng.csp.Csp;
import com.booleworks.logicng.csp.CspFactory;
import com.booleworks.logicng.csp.IntegerClause;
import com.booleworks.logicng.csp.terms.IntegerVariable;
import com.booleworks.logicng.datastructures.EncodingResult;

import java.util.Set;

public class CompactOrderEncoding {
    public static void encode(final Csp csp, final CspEncodingContext context, final EncodingResult result, final CspFactory cf) {
        for (final IntegerVariable v : csp.getInternalIntegerVariables()) {
            encodeVariable(v, context, result, cf);
        }
        encodeClauses(csp.getClauses(), context, result, cf);
    }

    public static void encodeVariable(final IntegerVariable v, final CspEncodingContext context, final EncodingResult result, final CspFactory cf) {
        if (v.getDigits().size() <= 1) {
            OrderEncoding.encodeVariable(v, context, result, cf);
        }
    }

    public static void encodeClauses(final Set<IntegerClause> clauses, final CspEncodingContext context, final EncodingResult result, final CspFactory cf) {
        for (final IntegerClause c : clauses) {
            encodeClause(c, context, result, cf);
        }
    }

    public static void encodeClause(final IntegerClause clause, final CspEncodingContext context, final EncodingResult result, final CspFactory cf) {
        OrderEncoding.encodeClause(clause, context, result, cf);
    }
}
