package com.booleworks.logicng.csp.encodings;

import com.booleworks.logicng.csp.CspFactory;
import com.booleworks.logicng.csp.datastructures.Csp;
import com.booleworks.logicng.csp.datastructures.IntegerClause;
import com.booleworks.logicng.csp.literals.ArithmeticLiteral;
import com.booleworks.logicng.csp.literals.EqMul;
import com.booleworks.logicng.csp.literals.OpAdd;
import com.booleworks.logicng.csp.literals.OpXY;
import com.booleworks.logicng.csp.terms.IntegerVariable;
import com.booleworks.logicng.datastructures.EncodingResult;

import java.util.Set;

public class CompactOrderEncoding {
    public static void encode(final Csp csp, final CompactOrderEncodingContext context, final EncodingResult result, final CspFactory cf) {
        final ReductionResult reduced = CompactOrderReduction.reduce(csp.getClauses(), csp.getInternalIntegerVariables(), context, cf);
        for (final IntegerVariable v : reduced.getFrontierAuxiliaryVariables()) {
            encodeVariable(v, context, result, cf);
        }
        encodeClauses(reduced.getClauses(), context, result, cf);
    }

    static void encodeVariable(final IntegerVariable v, final CompactOrderEncodingContext context, final EncodingResult result, final CspFactory cf) {
        assert context.getDigits(v) == null || context.getDigits(v).size() == 1;
        OrderEncoding.encodeVariable(v, context.getOrderContext(), result, cf);
    }

    static void encodeClauses(final Set<IntegerClause> clauses, final CompactOrderEncodingContext context, final EncodingResult result, final CspFactory cf) {
        for (final IntegerClause c : clauses) {
            encodeClause(c, context, result, cf);
        }
    }

    static void encodeClause(final IntegerClause clause, final CompactOrderEncodingContext context, final EncodingResult result, final CspFactory cf) {
        OrderEncoding.encodeClause(clause, context.getOrderContext(), result, cf);
    }

    static boolean isSimpleLiteral(final ArithmeticLiteral lit, final CompactOrderEncodingContext context) {
        if (lit instanceof OpXY) {
            final OpXY l = (OpXY) lit;
            assert !l.getVariables().isEmpty();
            if (l.getOp() == OpXY.Operator.EQ) {
                return false;
            }
            return l.getVariables().size() == 1 && l.getUpperBound() < context.getBase();
        } else if (lit instanceof EqMul) {
            return false;
        } else if (lit instanceof OpAdd) {
            return false;
        }
        return OrderEncoding.isSimpleLiteral(lit);
    }

    static boolean isSimpleClause(final IntegerClause clause, final CompactOrderEncodingContext context) {
        return clause.size() - simpleClauseSize(clause, context) <= 1;
    }

    static int simpleClauseSize(final IntegerClause clause, final CompactOrderEncodingContext context) {
        int simpleLiterals = clause.getBoolLiterals().size();
        for (final ArithmeticLiteral lit : clause.getArithmeticLiterals()) {
            if (isSimpleLiteral(lit, context)) {
                ++simpleLiterals;
            }
        }
        return simpleLiterals;
    }
}
