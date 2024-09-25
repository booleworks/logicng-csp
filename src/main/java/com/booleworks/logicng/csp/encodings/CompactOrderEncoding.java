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

/**
 * A class grouping functions for compact order encoding.
 */
public class CompactOrderEncoding {
    private CompactOrderEncoding() {
    }

    /**
     * Encodes a CSP problem using the compact order encoding.
     * @param csp     the problem
     * @param context the encoding context
     * @param result  destination for the result
     * @param cf      the factory
     */
    public static void encode(final Csp csp, final CompactOrderEncodingContext context, final EncodingResult result,
                              final CspFactory cf) {
        final ReductionResult reduced =
                CompactOrderReduction.reduce(csp.getClauses(), csp.getInternalIntegerVariables(), context, cf);
        for (final IntegerVariable v : reduced.getFrontierAuxiliaryVariables()) {
            encodeVariable(v, context, result, cf);
        }
        encodeClauses(reduced.getClauses(), context, result, cf);
    }

    private static void encodeVariable(final IntegerVariable v, final CompactOrderEncodingContext context,
                                       final EncodingResult result, final CspFactory cf) {
        assert context.getDigits(v) == null || context.getDigits(v).size() == 1;
        OrderEncoding.encodeVariable(v, context.getOrderContext(), result, cf);
    }

    private static void encodeClauses(final Set<IntegerClause> clauses, final CompactOrderEncodingContext context,
                                      final EncodingResult result, final CspFactory cf) {
        for (final IntegerClause c : clauses) {
            encodeClause(c, context, result, cf);
        }
    }

    private static void encodeClause(final IntegerClause clause, final CompactOrderEncodingContext context,
                                     final EncodingResult result, final CspFactory cf) {
        OrderEncoding.encodeClause(clause, context.getOrderContext(), result, cf);
    }

    /**
     * Returns whether an arithmetic literal is simple.
     * <p>
     * A literal is <I>simple</I> if it will encode as a single boolean variable.
     * @param lit     the arithmetic literal
     * @param context the encoding context
     * @return {@code true} if the literal is simple
     */
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

    /**
     * Returns whether an arithmetic clauses is simple.
     * <p>
     * A clause is <I>simple</I> if it contains at most one non-simple literal.
     * @param clause  the clause
     * @param context the encoding context
     * @return {@code true} if the clause is simple
     */
    static boolean isSimpleClause(final IntegerClause clause, final CompactOrderEncodingContext context) {
        return clause.size() - simpleClauseSize(clause, context) <= 1;
    }

    /**
     * Returns the number of simple literals (simple arithmetic literals and all boolean literals).
     * @param clause  the clause
     * @param context the encoding context
     * @return number of simple literals
     */
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
