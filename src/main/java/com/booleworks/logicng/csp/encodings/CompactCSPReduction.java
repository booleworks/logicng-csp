package com.booleworks.logicng.csp.encodings;

import com.booleworks.logicng.csp.Csp;
import com.booleworks.logicng.csp.CspFactory;
import com.booleworks.logicng.csp.IntegerClause;
import com.booleworks.logicng.csp.IntegerDomain;
import com.booleworks.logicng.csp.IntegerHolder;
import com.booleworks.logicng.csp.LinearExpression;
import com.booleworks.logicng.csp.literals.ArithmeticLiteral;
import com.booleworks.logicng.csp.literals.EqMul;
import com.booleworks.logicng.csp.literals.LinearLiteral;
import com.booleworks.logicng.csp.literals.OpAdd;
import com.booleworks.logicng.csp.literals.OpXY;
import com.booleworks.logicng.csp.literals.RCSPLiteral;
import com.booleworks.logicng.csp.terms.IntegerConstant;
import com.booleworks.logicng.csp.terms.IntegerVariable;
import com.booleworks.logicng.formulas.Literal;
import com.booleworks.logicng.formulas.Variable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CompactCSPReduction {
    public static final String AUX_PREFIX = "CRC";

    private static Set<IntegerClause> toCCSP(final Set<IntegerClause> clauses, final Set<IntegerVariable> variables, final Csp.Builder csp, final CspEncodingContext context,
                                             final CspFactory cf) {
        final Set<IntegerClause> newClauses = new LinkedHashSet<>();
        for (final IntegerVariable v : variables) {
            splitToDigits(v, csp, context, cf);
            final int lb = v.getDomain().lb();
            final int ub = v.getDomain().ub();
            final int m = context.getDigits().get(v).size();
            if (m > 1 || ub <= Math.pow(context.getBase(), m) - 1) {
                newClauses.addAll(convertToCCSP(new OpXY(OpXY.Operator.LE, v, cf.constant(ub)), csp, context, cf));
            }
            if (m > 1 && lb != 0) {
                newClauses.addAll(convertToCCSP(new OpXY(OpXY.Operator.LE, cf.constant(lb), v), csp, context, cf));
            }
        }
        for (final IntegerClause clause : clauses) {
            if (clause.getArithmeticLiterals().isEmpty()) {
                newClauses.add(clause);
            } else {
                assert clause.size() - OrderEncoding.simpleClauseSize(clause) <= 1;

                final Set<ArithmeticLiteral> simpleLiterals = new LinkedHashSet<>();
                final Set<IntegerClause> ccspClauses = new LinkedHashSet<>();
                for (final ArithmeticLiteral al : clause.getArithmeticLiterals()) {
                    final RCSPLiteral ll = (RCSPLiteral) al;
                    final Set<IntegerClause> ccsp = convertToCCSP(ll, csp, context, cf);
                    if (isSimpleLiteral(ll, context)) {
                        assert ccsp.size() == 1;
                        final IntegerClause c = ccsp.iterator().next();
                        assert c.getBoolLiterals().isEmpty();
                        simpleLiterals.addAll(c.getArithmeticLiterals());
                    } else {
                        assert ccspClauses.isEmpty();
                        ccspClauses.addAll(ccsp);
                    }
                }
                final Set<IntegerClause> newCcspClauses = new LinkedHashSet<>();
                if (ccspClauses.isEmpty()) {
                    newCcspClauses.add(new IntegerClause(clause.getBoolLiterals(), simpleLiterals));
                } else {
                    for (final IntegerClause c : ccspClauses) {
                        final Set<ArithmeticLiteral> nl = new LinkedHashSet<>(c.getArithmeticLiterals());
                        nl.addAll(simpleLiterals);
                        newCcspClauses.add(new IntegerClause(c.getBoolLiterals(), nl));
                    }
                }
                newClauses.addAll(newCcspClauses);
            }
        }
        return newClauses;
    }

    private static Set<IntegerClause> convertToCCSP(final RCSPLiteral literal, final Csp.Builder csp, final CspEncodingContext context, final CspFactory cf) {
        if (literal instanceof EqMul) {
            return convertToCCSP((EqMul) literal, csp, context, cf);
        } else if (literal instanceof OpAdd) {
            return convertToCCSP((OpAdd) literal, csp, context, cf);
        } else if (literal instanceof OpXY) {
            return convertToCCSP((OpXY) literal, csp, context, cf);
        } else {
            throw new RuntimeException("Unknown RCSP Literal: " + literal.getClass());
        }
    }

    private static Set<IntegerClause> convertToCCSP(final OpXY lit, final Csp.Builder csp, final CspEncodingContext context, final CspFactory cf) {
        final Set<IntegerClause> ret = new LinkedHashSet<>();
        final int b = context.getBase();
        final IntegerHolder x = lit.getX();
        final IntegerHolder y = lit.getY();
        final Map<IntegerConstant, List<Integer>> constDigits = calculateConstDigits(List.of(x, y), b);
        final int m = Math.max(nDigits(x, context, constDigits), nDigits(y, context, constDigits));

        switch (lit.getOp()) {
            case LE:
                if (x instanceof IntegerConstant || y instanceof IntegerConstant) {
                    for (int i = 0; i < m; ++i) {
                        final Set<ArithmeticLiteral> arithLiterals = new LinkedHashSet<>();
                        final Set<Literal> literals = new LinkedHashSet<>();
                        arithLiterals.add(LLExpressions.le(nth(x, i, context, constDigits), nth(y, i, context, constDigits)));
                        for (int j = i + 1; j < m; ++j) {
                            arithLiterals.add(LLExpressions.le(nth(x, j, context, constDigits), LLExpressions.sub(nth(x, j, context, constDigits), 1)));
                        }
                        ret.add(new IntegerClause(literals, arithLiterals));
                    }
                } else {
                    final Variable[] s = new Variable[m];
                    for (int i = 1; i < m; ++i) {
                        s[i] = context.newAuxBoolVariable(cf.formulaFactory());
                        csp.addInternalBooleanVariable(s[i]);
                    }
                    // -s(i+1) or x(i) <= y(i) (when 0 <= i < m - 1)
                    for (int i = 0; i < m - 1; ++i) {
                        final Set<ArithmeticLiteral> arithLiterals = new LinkedHashSet<>();
                        final Set<Literal> literals = new LinkedHashSet<>();
                        literals.add(s[i + 1].negate(cf.formulaFactory()));
                        arithLiterals.add(LLExpressions.le(nth(x, i, context, constDigits), nth(y, i, context, constDigits)));
                        ret.add(new IntegerClause(literals, arithLiterals));
                    }
                    // x(i) <= y(i) (when i == m - 1)
                    ret.add(new IntegerClause(LLExpressions.le(nth(x, m - 1, context, constDigits), nth(y, m - 1, context, constDigits))));

                    // -s(i+1) or (x(i) <= y(i) - 1) or s(i) (when 1 <= i < m - 1)
                    for (int i = 1; i < m - 1; ++i) {
                        final Set<ArithmeticLiteral> arithmeticLiterals = new LinkedHashSet<>();
                        final Set<Literal> literals = new LinkedHashSet<>();
                        literals.add(s[i + 1].negate(cf.formulaFactory()));
                        arithmeticLiterals.add(LLExpressions.le(nth(x, i, context, constDigits), LLExpressions.sub(nth(y, i, context, constDigits), 1)));
                        literals.add(s[i].negate(cf.formulaFactory()));
                        ret.add(new IntegerClause(literals, arithmeticLiterals));
                    }
                    if (m > 1) {
                        // (x(i) <= y(i) - 1) or s(i) (when i == m - 1)
                        final Set<ArithmeticLiteral> arithmeticLiterals = new LinkedHashSet<>();
                        final Set<Literal> literals = new LinkedHashSet<>();
                        arithmeticLiterals.add(LLExpressions.le(nth(x, m - 1, context, constDigits), LLExpressions.sub(nth(y, m - 1, context, constDigits), 1)));
                        literals.add(s[m - 1].negate(cf.formulaFactory()));
                        ret.add(new IntegerClause(literals, arithmeticLiterals));
                    }
                }
                break;
            case EQ:
                for (int i = 0; i < m; ++i) {
                    ret.add(new IntegerClause(LLExpressions.le(nth(x, i, context, constDigits), nth(y, i, context, constDigits))));
                    ret.add(new IntegerClause(LLExpressions.ge(nth(x, i, context, constDigits), nth(y, i, context, constDigits))));
                }
                break;
            case NE:
                final Set<ArithmeticLiteral> arithmeticLiterals = new LinkedHashSet<>();
                for (int i = 0; i < m; ++i) {
                    arithmeticLiterals.add(LLExpressions.le(nth(x, i, context, constDigits), LLExpressions.sub(nth(y, i, context, constDigits), 1)));
                    arithmeticLiterals.add(LLExpressions.ge(LLExpressions.sub(nth(x, i, context, constDigits), 1), nth(y, i, context, constDigits)));
                }
                ret.addAll(OrderReduction.simplifyClause(new IntegerClause(Collections.emptySet(), arithmeticLiterals), Collections.emptySet(), context, csp, cf.formulaFactory()));
                break;
        }
        return ret;
    }

    private static Set<IntegerClause> convertToCCSP(final OpAdd lit, final Csp.Builder csp, final CspEncodingContext context, final CspFactory cf) {
        final Set<IntegerClause> ret = new LinkedHashSet<>();
        final int b = context.getBase();
        final IntegerHolder x = lit.getX();
        final IntegerHolder y = lit.getY();
        final IntegerHolder z = lit.getZ();
        final Map<IntegerConstant, List<Integer>> constDigits = calculateConstDigits(List.of(x, y, z), b);

        final int m = Math.max(Math.max(nDigits(x, context, constDigits), nDigits(y, context, constDigits)), nDigits(z, context, constDigits));
        final IntegerVariable[] c = new IntegerVariable[m];
        for (int i = 1; i < m; ++i) {
            c[i] = cf.auxVariable(AUX_PREFIX, IntegerDomain.of(0, 1));
            csp.addInternalIntegerVariable(c[i]);
        }

        final LinearExpression[] lhs = new LinearExpression[m];
        for (int i = 0; i < m - 1; ++i) {
            lhs[i] = LLExpressions.add(
                    nth(z, i, context, constDigits),
                    LLExpressions.mul(lle(c[i + 1]), b));
        }
        lhs[m - 1] = nth(z, m - 1, context, constDigits);

        final LinearExpression[] rhs = new LinearExpression[m];
        rhs[0] = LLExpressions.add(nth(x, 0, context, constDigits), nth(y, 0, context, constDigits));
        for (int i = 1; i < m; ++i) {
            rhs[i] = LLExpressions.add(
                    nth(x, i, context, constDigits),
                    nth(y, i, context, constDigits),
                    lle(c[i])
            );
        }

        switch (lit.getOp()) {
            case LE: {
                final Variable[] s = new Variable[m];
                for (int i = 1; i < m; ++i) {
                    s[i] = context.newAuxBoolVariable(cf.formulaFactory());
                    csp.addInternalBooleanVariable(s[i]);
                }

                // -s(i+1) or (z(i) + B*c(i+1) <= x(i) + y(i) + c(i)) (when 0 <= i < m - 1)
                for (int i = 0; i < m - 1; ++i) {
                    final Set<Literal> boolLiterals = new LinkedHashSet<>();
                    final Set<ArithmeticLiteral> arithLiterals = new LinkedHashSet<>();
                    boolLiterals.add(s[i + 1].negate(cf.formulaFactory()));
                    arithLiterals.add(LLExpressions.le(lhs[i], rhs[i]));
                    ret.add(new IntegerClause(boolLiterals, arithLiterals));
                }
                //z(i) <= x(i) + y(i) + c(i) (when i == m - 1)
                ret.add(new IntegerClause(LLExpressions.le(lhs[m - 1], rhs[m - 1])));

                // -s(i+1) or (z(i) + B * c(i + 1) <= x(i) + y(i) + c(i) - 1) or s(i)
                // (when 1 <= i < m - 1)
                for (int i = 1; i < m - 1; ++i) {
                    final Set<Literal> boolLiterals = new LinkedHashSet<>();
                    final Set<ArithmeticLiteral> arithLiterals = new LinkedHashSet<>();
                    boolLiterals.add(s[i].negate(cf.formulaFactory()));
                    arithLiterals.add(LLExpressions.le(lhs[i], LLExpressions.sub(rhs[i], 1)));
                    ret.add(new IntegerClause(boolLiterals, arithLiterals));
                }
                // (z(i) <= x(i) + y(i) + c(i) - 1) or s(i) (when i == m - 1)
                if (m > 1) {
                    final Set<Literal> boolLiterals = new LinkedHashSet<>();
                    final Set<ArithmeticLiteral> arithLiterals = new LinkedHashSet<>();
                    boolLiterals.add(s[m - 1].negate(cf.formulaFactory()));
                    arithLiterals.add(LLExpressions.le(lhs[m - 1], LLExpressions.sub(rhs[m - 1], 1)));
                    ret.add(new IntegerClause(boolLiterals, arithLiterals));
                }

                for (int i = 0; i < m - 1; ++i) {
                    //c(i+1) <= 0 or x(i) + y(i) + c(i) >= B
                    final Set<ArithmeticLiteral> ltor = new LinkedHashSet<>();
                    ltor.add(LLExpressions.le(lle(c[i + 1]), 0));
                    ltor.add(LLExpressions.ge(rhs[i], b));

                    final Set<ArithmeticLiteral> rtol = new LinkedHashSet<>();
                    rtol.add(LLExpressions.ge(lle(c[i + 1]), 1));
                    rtol.add(LLExpressions.le(rhs[i], b - 1));

                    ret.add(new IntegerClause(Collections.emptySet(), ltor));
                    ret.add(new IntegerClause(Collections.emptySet(), rtol));
                }
                break;
            }
            case GE: {
                final Variable[] s = new Variable[m];
                for (int i = 1; i < m; i++) {
                    s[i] = context.newAuxBoolVariable(cf.formulaFactory());
                    csp.addInternalBooleanVariable(s[i]);
                }

                // -s(i+1) or (z(i) + B*c(i+1) <= x(i) + y(i) + c(i)) (when 0 <= i < m - 1)
                for (int i = 0; i < m - 1; i++) {
                    final Set<ArithmeticLiteral> arithLiterals = new LinkedHashSet<>();
                    final Set<Literal> boolLiterals = new LinkedHashSet<>();
                    arithLiterals.add(LLExpressions.le(lhs[i], rhs[i]));
                    boolLiterals.add(s[i + 1].negate(cf.formulaFactory()));
                    ret.add(new IntegerClause(boolLiterals, arithLiterals));
                }
                // z(i) <= x(i) + y(i) + c(i) (when i == m - 1)
                ret.add(new IntegerClause(LLExpressions.le(lhs[m - 1], rhs[m - 1])));

                // -s(i+1) or (z(i) + B * c(i+1) <= x(i) + y(i) + c(i) - 1) or s(i)
                // (when 1 <= i < m - 1)
                for (int i = 1; i < m - 1; ++i) {
                    final Set<ArithmeticLiteral> arithLiterals = new LinkedHashSet<>();
                    final Set<Literal> booLiterals = new LinkedHashSet<>();
                    booLiterals.add(s[i + 1].negate(cf.formulaFactory()));
                    booLiterals.add(s[i]);
                    arithLiterals.add(LLExpressions.le(lhs[i], LLExpressions.sub(rhs[i], 1)));
                    ret.add(new IntegerClause(booLiterals, arithLiterals));
                }
                // (z(i) <= x(i) + y(i) + c(i) - 1) or s(i) (when i == m - 1)
                if (m > 1) {
                    final Set<ArithmeticLiteral> arithLiterals = new LinkedHashSet<>();
                    final Set<Literal> boolLiterals = new LinkedHashSet<>();
                    boolLiterals.add(s[m - 1]);
                    arithLiterals.add(LLExpressions.le(lhs[m - 1], LLExpressions.sub(rhs[m - 1], 1)));
                    ret.add(new IntegerClause(boolLiterals, arithLiterals));
                }

                for (int i = 0; i < m - 1; i++) {
                    final LinearExpression cex = lle(c[i + 1]);
                    //c(i + 1) <= 0 x(i) + y(i) + c(i) >= B
                    final Set<ArithmeticLiteral> ltor = new LinkedHashSet<>();
                    ltor.add(LLExpressions.le(cex, 0));
                    ltor.add(LLExpressions.ge(rhs[i], b));

                    //c(i+1) >= 1 or x(i) + y(i) + c(i) <= B - 1
                    final Set<ArithmeticLiteral> rtol = new LinkedHashSet<>();
                    rtol.add(LLExpressions.ge(cex, 1));
                    rtol.add(LLExpressions.le(rhs[i], b - 1));

                    ret.add(new IntegerClause(Collections.emptySet(), ltor));
                    ret.add(new IntegerClause(Collections.emptySet(), rtol));
                }
                break;
            }
            case EQ: {
                for (int i = 0; i < m; ++i) {
                    ret.add(new IntegerClause(LLExpressions.le(lhs[i], rhs[i])));
                    ret.add(new IntegerClause(LLExpressions.ge(lhs[i], rhs[i])));
                }
                break;
            }
            case NE: {
                final Set<ArithmeticLiteral> arithLiterals = new LinkedHashSet<>();
                for (int i = 0; i < m; ++i) {
                    arithLiterals.add(LLExpressions.le(lhs[i], LLExpressions.sub(rhs[i], 1)));
                    arithLiterals.add(LLExpressions.ge(LLExpressions.sub(lhs[i], 1), rhs[i]));
                }
                final IntegerClause newClause = new IntegerClause(Collections.emptySet(), arithLiterals);
                ret.addAll(OrderReduction.simplifyClause(newClause, Collections.emptySet(), context, csp, cf.formulaFactory()));

                for (int i = 0; i < m - 1; i++) {
                    // c(i+1) <= 0 or x(i)+y(i)+c(i) >= B
                    final Set<ArithmeticLiteral> ltor = new LinkedHashSet<>();
                    ltor.add(LLExpressions.le(lle(c[i + 1]), 0));
                    ltor.add(LLExpressions.ge(rhs[i], b));

                    // c(i+1) >= 1 or x(i) + y(i) + c(i) <= B - 1
                    final Set<ArithmeticLiteral> rtol = new LinkedHashSet<>();
                    rtol.add(LLExpressions.ge(lle(c[i + 1]), 1));
                    rtol.add(LLExpressions.le(rhs[i], b - 1));

                    ret.add(new IntegerClause(Collections.emptySet(), ltor));
                    ret.add(new IntegerClause(Collections.emptySet(), rtol));
                }
                break;
            }
        }
        return ret;
    }

    private static Set<IntegerClause> convertToCCSP(final EqMul lit, final Csp.Builder csp, final CspEncodingContext context, final CspFactory cf) {
        final int b = context.getBase();
        final IntegerHolder x = lit.getX();
        final IntegerVariable y = lit.getY();
        final IntegerHolder z = lit.getZ();
        final Map<IntegerConstant, List<Integer>> constDigits = calculateConstDigits(List.of(lit.getX(), lit.getY(), lit.getZ()), b);
        final int m = Math.max(Math.max(nDigits(x, context, constDigits), nDigits(y, context, constDigits)), nDigits(z, context, constDigits));
        final Set<IntegerClause> ret = new LinkedHashSet<>();

        if (x instanceof IntegerConstant && ((IntegerConstant) x).getValue() < b) {
            if (((IntegerConstant) x).getValue() == 0) {
                assert z instanceof IntegerVariable;
                return convertToCCSP(new OpXY(OpXY.Operator.LE, z, cf.constant(0)), csp, context, cf);
            } else if (((IntegerConstant) x).getValue() == 1) {
                return convertToCCSP(new OpXY(OpXY.Operator.EQ, z, y), csp, context, cf);
            }
            final IntegerHolder[] v = new IntegerHolder[m];
            final int a = ((IntegerConstant) x).getValue();
            for (int i = 0; i < m; ++i) {
                final IntegerDomain d = IntegerDomain.of(0, a * nth(y, i, context, constDigits).getDomain().ub());
                final IntegerVariable vi = cf.auxVariable(AUX_PREFIX, d);
                csp.addInternalIntegerVariable(vi);
                splitToDigits(vi, csp, context, cf);
                v[i] = vi;
            }

            for (int i = 0; i < m; ++i) {
                ret.add(new IntegerClause(LLExpressions.le(
                        LLExpressions.add(LLExpressions.mul(nth(v[i], 1, context, constDigits), b), nth(v[i], 0, context, constDigits)),
                        LLExpressions.mul(nth(y, i, context, constDigits), a)
                )));
                ret.add(new IntegerClause(LLExpressions.ge(
                        LLExpressions.add(LLExpressions.mul(nth(v[i], 1, context, constDigits), b), nth(v[0], 0, context, constDigits)),
                        LLExpressions.mul(nth(y, i, context, constDigits), a)
                )));
            }

            final IntegerVariable[] c = new IntegerVariable[m];
            final IntegerDomain d = IntegerDomain.of(0, 1);
            for (int i = 2; i < m; ++i) {
                c[i] = cf.auxVariable(AUX_PREFIX, d);
                csp.addInternalIntegerVariable(c[i]);
            }

            for (int i = 0; i < m; ++i) {
                final LinearExpression lhs;
                if (i == 0 || i == m - 1) {
                    lhs = nth(z, i, context, constDigits);
                } else {
                    lhs = LLExpressions.add(nth(z, i, context, constDigits), nth(v[i - 1], 1, context, constDigits));
                }

                final LinearExpression rhs;
                if (i == 0) {
                    rhs = nth(v[i], 0, context, constDigits);
                } else if (i == 1) {
                    rhs = LLExpressions.add(nth(v[i], 0, context, constDigits), nth(v[i - 1], 1, context, constDigits));
                } else {
                    rhs = LLExpressions.add(
                            LLExpressions.add(nth(v[i], 0, context, constDigits), nth(v[i - 1], 1, context, constDigits)),
                            lle(c[i])
                    );
                }

                ret.add(new IntegerClause(LLExpressions.le(lhs, rhs)));
                ret.add(new IntegerClause(LLExpressions.ge(lhs, rhs)));
            }
        } else {
            // z = xy
            final IntegerVariable[] w = new IntegerVariable[m];
            final int uby = y.getDomain().ub();
            int ubz = z.getDomain().ub();
            for (int i = 0; i < m; ++i) {
                final IntegerDomain d;
                if (x instanceof IntegerConstant) {
                    d = IntegerDomain.of(0, Math.min(nthValue((IntegerConstant) x, i, constDigits) * uby, ubz));
                } else {
                    d = IntegerDomain.of(0, Math.min((b - 1) * uby, ubz));
                }
                w[i] = cf.auxVariable(AUX_PREFIX, d);
                csp.addInternalIntegerVariable(w[i]);
                splitToDigits(w[i], csp, context, cf);
                ubz /= b;
            }
            if (x instanceof IntegerConstant) {
                for (int i = 0; i < m; ++i) {
                    final EqMul newLit = new EqMul(w[i], cf.constant(nthValue((IntegerConstant) x, i, constDigits)), y);
                    ret.addAll(convertToCCSP(newLit, csp, context, cf));
                }
            } else {
                final IntegerVariable[] ya = new IntegerVariable[b];
                for (int a = 0; a < b; ++a) {
                    ya[a] = cf.auxVariable(AUX_PREFIX, IntegerDomain.of(0, a * uby));
                    splitToDigits(ya[a], csp, context, cf);
                    csp.addInternalIntegerVariable(ya[a]);
                }

                for (int i = 0; i < m; ++i) {
                    for (int a = 0; a < b; ++a) {
                        final Set<ArithmeticLiteral> arithLiterals = new LinkedHashSet<>();
                        arithLiterals.add(LLExpressions.le(nth(x, i, context, constDigits), a - 1));
                        arithLiterals.add(LLExpressions.ge(nth(x, i, context, constDigits), a + 1));

                        final OpXY newLit = new OpXY(OpXY.Operator.EQ, w[i], ya[a]);
                        for (final IntegerClause c : convertToCCSP(newLit, csp, context, cf)) {
                            arithLiterals.addAll(c.getArithmeticLiterals());
                            ret.add(new IntegerClause(c.getBoolLiterals(), arithLiterals));
                        }
                    }
                }

                for (int a = 0; a < b; ++a) {
                    final EqMul newLit = new EqMul(ya[a], cf.constant(a), y);
                    ret.addAll(convertToCCSP(newLit, csp, context, cf));
                }
            }

            // [z = Sum_(i = 0)^(m - 1) B^i w_i]
            final IntegerHolder[] zi = new IntegerHolder[m];
            zi[m - 1] = w[m - 1];
            for (int i = m - 2; i > 0; --i) {
                final IntegerDomain d = IntegerDomain.of(0, b * zi[i + 1].getDomain().ub() + w[i].getDomain().ub());
                final IntegerVariable zii = cf.auxVariable(AUX_PREFIX, d);
                splitToDigits(zii, csp, context, cf);
                csp.addInternalIntegerVariable(zii);
                zi[i] = zii;
            }
            zi[0] = z;

            if (m == 1) {
                final LinearExpression exp1 = nth(z, 0, context, constDigits);
                final LinearExpression exp2 = nth(w[0], 0, context, constDigits);
                final LinearLiteral lit1 = LLExpressions.le(exp1, exp2);
                final LinearLiteral lit2 = LLExpressions.ge(exp1, exp2);
                ret.add(new IntegerClause(lit1));
                ret.add(new IntegerClause(lit2));
            } else {
                for (int i = 0; i < m - 1; ++i) {
                    ret.addAll(shiftAddToCCSP(zi[i], zi[i + 1], w[i], csp, context, cf));
                }
            }
        }
        return ret;
    }

    /**
     * u = b*s+t
     */
    private static Set<IntegerClause> shiftAddToCCSP(final IntegerHolder u, final IntegerHolder s, final IntegerHolder t, final Csp.Builder csp, final CspEncodingContext context, final CspFactory cf) {
        final int b = context.getBase();
        final Map<IntegerConstant, List<Integer>> constDigits = calculateConstDigits(List.of(u, s, t), b);
        final int m = Math.max(nDigits(s, context, constDigits), nDigits(t, context, constDigits));
        final Set<IntegerClause> ret = new LinkedHashSet<>();

        final IntegerVariable[] c = new IntegerVariable[m];
        final IntegerDomain d = IntegerDomain.of(0, 1);
        for (int i = 2; i < m; i++) {
            c[i] = cf.auxVariable(AUX_PREFIX, d);
            csp.addInternalIntegerVariable(c[i]);
        }

        for (int i = 0; i < m; ++i) {
            final LinearExpression lhs;
            if (i == 0 || i == m - 1) {
                lhs = nth(u, i, context, constDigits);
            } else {
                lhs = LLExpressions.add(
                        nth(u, i, context, constDigits),
                        LLExpressions.mul(lle(c[i + 1]), b)
                );
            }

            final LinearExpression rhs;
            if (i == 0) {
                rhs = nth(t, i, context, constDigits);
            } else if (i == 1) {
                rhs = LLExpressions.add(nth(t, i, context, constDigits), nth(s, i - 1, context, constDigits));
            } else if (i == m - 1) {
                rhs = LLExpressions.add(nth(s, i - 1, context, constDigits), lle(c[i]));
            } else {
                rhs = LLExpressions.add(
                        LLExpressions.add(nth(t, i, context, constDigits), nth(s, i - 1, context, constDigits)),
                        lle(c[i])
                );
            }

            ret.add(new IntegerClause(LLExpressions.le(lhs, rhs)));
            ret.add(new IntegerClause(LLExpressions.ge(lhs, rhs)));
        }
        return ret;
    }

    private static void splitToDigits(final IntegerVariable v, final Csp.Builder csp, final CspEncodingContext context, final CspFactory cf) {
        int ub = v.getDomain().ub();
        final int b = context.getBase();
        final int m = (int) Math.ceil(Math.log(ub + 1) / Math.log(b));

        final Set<IntegerVariable> digits = new HashSet<>();
        final List<IntegerVariable> vs = new ArrayList<>(m);
        if (m == 1) {
            vs.add(v);
        } else {
            for (int i = 0; i < m; ++i) {
                assert ub > 0;
                final int ubi = (i == m - 1) ? ub : b - 1;
                final IntegerDomain dom = IntegerDomain.of(0, ubi);
                final IntegerVariable dv = cf.auxVariable(AUX_PREFIX, dom);
                vs.add(dv);
                csp.addInternalIntegerVariable(dv);
                digits.add(dv);
                ub /= b;
            }
        }
        context.getDigits().put(v, vs);
    }

    private static Map<IntegerConstant, List<Integer>> calculateConstDigits(final Collection<IntegerHolder> vars, final int b) {
        final Map<IntegerConstant, List<Integer>> digitsMap = new HashMap<>();
        for (final IntegerHolder v : vars) {
            if (v instanceof IntegerConstant) {
                final List<Integer> digits = intToDigits((IntegerConstant) v, b);
                digitsMap.put((IntegerConstant) v, digits);
            }
        }
        return digitsMap;
    }

    private static int nDigits(final IntegerHolder v, final CspEncodingContext context, final Map<IntegerConstant, List<Integer>> constDigits) {
        if (v instanceof IntegerConstant) {
            return constDigits.get((IntegerConstant) v).size();
        } else {
            return context.getDigits().size();
        }
    }

    private static List<Integer> intToDigits(final IntegerConstant c, final int b) {
        final int m = (int) Math.ceil(Math.log(c.getValue() + 1) / Math.log(b));
        int ub = c.getValue();
        final List<Integer> digits = new ArrayList<>(m);
        for (int i = 0; i < m; i++) {
            digits.add(ub % b);
            ub /= b;
        }
        return digits;
    }

    private static LinearExpression nth(final IntegerHolder v, final int n, final CspEncodingContext context, final Map<IntegerConstant, List<Integer>> constDigits) {
        if (v instanceof IntegerConstant) {
            assert constDigits.get((IntegerConstant) v) != null;
            return new LinearExpression(nthValue((IntegerConstant) v, n, constDigits));
        } else {
            final List<IntegerVariable> digits = context.getDigits().get((IntegerVariable) v);
            if (digits.size() > n) {
                return new LinearExpression(digits.get(n));
            } else {
                return new LinearExpression(0);
            }
        }
    }

    private static int nthValue(final IntegerConstant v, final int n, final Map<IntegerConstant, List<Integer>> constDigits) {
        return constDigits.get(v).size() > n ? constDigits.get(v).get(n) : 0;
    }

    private static LinearExpression lle(final IntegerVariable v) {
        return new LinearExpression(v);
    }

    private static boolean isSimpleLiteral(final ArithmeticLiteral lit, final CspEncodingContext context) {
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

    private static boolean isSimpleClause(final IntegerClause clause, final CspEncodingContext context) {
        return clause.size() - simpleClauseSize(clause, context) <= 1;
    }

    private static int simpleClauseSize(final IntegerClause clause, final CspEncodingContext context) {
        int simpleLiterals = clause.getBoolLiterals().size();
        for (final ArithmeticLiteral lit : clause.getArithmeticLiterals()) {
            if (isSimpleLiteral(lit, context)) {
                ++simpleLiterals;
            }
        }
        return simpleLiterals;
    }
}
