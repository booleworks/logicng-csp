package com.booleworks.logicng.csp.encodings;

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
import com.booleworks.logicng.csp.terms.IntegerConstant;
import com.booleworks.logicng.csp.terms.IntegerVariable;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.formulas.Literal;
import com.booleworks.logicng.formulas.Variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CompactOrderReduction {

    public static final String AUX_ADJUST = "COE_ADJUST";
    public static final String AUX_TERNARY = "COE_TERNARY";
    public static final String AUX_RCSP = "COE_RCSP";
    public static final String AUX_SIMPLE = "COE_SIMPLE";

    static ReductionResult reduce(final Set<IntegerClause> clauses, final Set<IntegerVariable> integerVariables, final CompactOrderEncodingContext context,
                                  final CspFactory cf) {
        Set<IntegerClause> newClauses;
        newClauses = adjust(clauses, integerVariables, context, cf);
        newClauses = toTernary(newClauses, context, cf);
        newClauses = toRCSP(newClauses, context, cf);
        newClauses = simplify(newClauses, context, cf.formulaFactory());

        final int size = context.getAdjustedVariables().size() + context.getTernarySimplificationVariables().size() + context.getRCSPVariables().size();
        final List<IntegerVariable> currentVariables = new ArrayList<>(size);
        currentVariables.addAll(context.getAdjustedVariables());
        currentVariables.addAll(context.getTernarySimplificationVariables());
        currentVariables.addAll(context.getRCSPVariables());

        return CompactCSPReduction.toCCSP(newClauses, currentVariables, context, cf);
    }

    /**
     * <h1>Behaviour</h1>
     * <p>
     * Adjusts variables to be continuous and start at 0 (see {@link CompactOrderReduction#createAdjustedVariable})
     * <p>
     * Adjust linear literals / linear sums: Adds the offset of each auxiliary variable to {@code b}.
     * {@code b_new = b + coef(v_1) * offset(v_1) + ... + coef(v_n) * offset(v_n)}
     * <p>
     * Replace all literals in clauses with the adjusted literals.
     * <p>
     * <h1>Example</h1>
     * TODO
     */
    private static Set<IntegerClause> adjust(final Set<IntegerClause> clauses, final Set<IntegerVariable> variables, final CompactOrderEncodingContext context,
                                             final CspFactory cf) {
        final Set<IntegerClause> adjustedClauses = new LinkedHashSet<>(clauses);
        for (final IntegerVariable v : variables) {
            final IntegerVariable adjustedVar = createAdjustedVariable(v.getDomain(), AUX_ADJUST, true, adjustedClauses, context, cf);
            context.addAdjustedVariable(v, adjustedVar);
        }

        final Set<IntegerClause> newClauses = new LinkedHashSet<>();
        for (final IntegerClause c : adjustedClauses) {
            if (c.getArithmeticLiterals().isEmpty()) {
                newClauses.add(c);
            } else {
                final IntegerClause.Builder newClause = IntegerClause.Builder.cloneOnlyBool(c);
                for (final ArithmeticLiteral lit : c.getArithmeticLiterals()) {
                    if (lit instanceof LinearLiteral) {
                        final LinearLiteral ll = ((LinearLiteral) lit).substitute(context.getAdjustedVariablesSubstitution());
                        final LinearExpression ls = ll.getLinearExpression();
                        int b = ls.getB();
                        for (final Map.Entry<IntegerVariable, Integer> es : ls.getCoef().entrySet()) {
                            b += context.getOffset(es.getKey()) * es.getValue();
                        }
                        final LinearExpression newLs = new LinearExpression(ls.getCoef(), b);
                        final LinearLiteral newLl = new LinearLiteral(newLs, ll.getOperator());
                        newClause.addArithmeticLiteral(newLl);
                    } else {
                        throw new IllegalArgumentException("Reduction not supported for: " + lit.getClass());
                    }
                }
                newClauses.add(newClause.build());
            }
        }
        return newClauses;
    }

    /**
     * Adjust Variable: Makes non-contiguous variable contiguous and offset so that their lower bound is at 0:
     * - Substitute original variable with auxiliary variable, that has the wanted properties.
     * - Document the offset of the auxiliary variable inside the encoding context.
     * - We add clauses that exclude values that are not contained in the domain if the domain is not contiguous.
     * <p>
     * This function does not substitute the variables in the existing clauses, but uses the substitute in the created clauses. This caller must make sure the remaining uses of
     * the original variables are substituted.
     * <p>
     * Example:
     * TODO
     * <p>
     * TODO: There are some things I would like to investigate:
     * - What is the reason for the {@code useOffset} case distinction?
     * - Are variables with negative lower bounds represented properly?
     */
    private static IntegerVariable createAdjustedVariable(final IntegerDomain d, final String prefix, final boolean useOffset, final Set<IntegerClause> additionalClauses,
                                                          final CompactOrderEncodingContext context, final CspFactory cf) {
        final int offset = d.lb();
        final IntegerVariable newVar;
        if (useOffset) {
            final IntegerDomain newD = IntegerDomain.of(0, d.ub() - offset);
            newVar = context.newAdjustedVariable(prefix, newD, cf);
        } else {
            final IntegerDomain newD = IntegerDomain.of(0, d.ub());
            newVar = context.newAdjustedVariable(prefix, newD, cf);
            final IntegerClause c = new IntegerClause(new LinearLiteral(new LinearExpression(-1, newVar, offset), LinearLiteral.Operator.LE));
            additionalClauses.add(c);
        }
        context.addOffset(newVar, offset);

        if (!d.isContiguous()) {
            int lst = d.lb() - 1;
            final Iterator<Integer> iter = d.iterator();
            while (iter.hasNext()) {
                final int i = iter.next();
                if (lst + 2 == i) {
                    final IntegerClause c = new IntegerClause(new LinearLiteral(new LinearExpression(1, newVar, -(lst + 1)), LinearLiteral.Operator.NE));
                    additionalClauses.add(c);
                } else if (lst + 1 != i) {
                    final Variable b = context.newAdjustedBoolVariable(cf.formulaFactory());
                    final IntegerClause clause1 = new IntegerClause(
                            b.negate(cf.formulaFactory()),
                            new LinearLiteral(new LinearExpression(1, newVar, -lst), LinearLiteral.Operator.LE)
                    );
                    additionalClauses.add(clause1);

                    final IntegerClause clause2 = new IntegerClause(
                            b,
                            new LinearLiteral(new LinearExpression(-1, newVar, i), LinearLiteral.Operator.LE)
                    );
                    additionalClauses.add(clause2);
                }
                lst = i;
            }
        }
        return newVar;
    }

    /**
     * Convert arithmetic literals to arithmetic literals with less or equal 3 variables. Substitutes new literals in the returned clauses
     */
    private static Set<IntegerClause> toTernary(final Set<IntegerClause> clauses, final CompactOrderEncodingContext context, final CspFactory cf) {
        final Set<IntegerClause> newClauses = new LinkedHashSet<>();
        for (final IntegerClause c : clauses) {
            final IntegerClause.Builder newClause = IntegerClause.Builder.cloneOnlyBool(c);
            for (final ArithmeticLiteral lit : c.getArithmeticLiterals()) {
                if (lit instanceof LinearLiteral) {
                    final LinearLiteral ll = (LinearLiteral) lit;
                    if (ll.getLinearExpression().size() > 3) {
                        final LinearExpression.Builder ls = simplifyToTernary(new LinearExpression.Builder(ll.getLinearExpression()), newClauses, context, cf);
                        newClause.addArithmeticLiteral(new LinearLiteral(ls.build(), ll.getOperator()));
                    } else {
                        newClause.addArithmeticLiteral(ll);
                    }
                } else {
                    newClause.addArithmeticLiteral(lit);
                }
            }
            newClauses.add(newClause.build());
        }
        return newClauses;
    }

    private static LinearExpression.Builder simplifyToTernary(final LinearExpression.Builder exp, final Set<IntegerClause> clauses, final CompactOrderEncodingContext context,
                                                              final CspFactory cf) {
        if (exp.size() <= 3) {
            return exp;
        }
        final LinearExpression.Builder lhs = new LinearExpression.Builder(0);
        final LinearExpression.Builder rhs = new LinearExpression.Builder(0);
        for (final IntegerVariable v : exp.getVariables()) {
            final int a = exp.getA(v);
            if (a > 0) {
                lhs.setA(a, v);
            } else {
                rhs.setA(-a, v);
            }
        }
        final int b = exp.getB();
        final int rest = b == 0 ? 3 : 2;
        int lhs_len = 0, rhs_len = 0;
        if (lhs.size() == 0) {
            rhs_len = rest;
        } else if (rhs.size() == 0) {
            lhs_len = rest;
        } else if (lhs.getDomain().size() < rhs.getDomain().size()) {
            lhs_len = 1;
            rhs_len = rest - 1;
        } else {
            rhs_len = 1;
            lhs_len = rest - 1;
        }

        final LinearExpression.Builder e = new LinearExpression.Builder(b);
        for (final LinearExpression.Builder ei : OrderReduction.split(lhs.build(), lhs_len)) {
            final LinearExpression.Builder simplified = simplifyToTernaryExpression(ei, clauses, context, cf);
            e.add(simplified.build());
        }

        for (final LinearExpression.Builder ei : OrderReduction.split(rhs.build(), rhs_len)) {
            final LinearExpression.Builder simplified = simplifyToTernaryExpression(ei, clauses, context, cf);
            simplified.multiply(-1);
            e.add(simplified.build());
        }

        return e;
    }

    private static LinearExpression.Builder simplifyToTernaryExpression(final LinearExpression.Builder exp, final Set<IntegerClause> clauses,
                                                                        final CompactOrderEncodingContext context, final CspFactory cf) {
        final int factor = exp.factor();
        final LinearExpression.Builder normalized = exp.normalize();
        LinearExpression.Builder simplified = simplifyToTernary(normalized, clauses, context, cf);
        if (simplified.size() > 1) {
            final IntegerVariable v = createAdjustedVariable(simplified.getDomain(), AUX_TERNARY, false, clauses, context, cf);
            final LinearExpression.Builder ls = new LinearExpression.Builder(v);
            ls.subtract(simplified.build());
            simplified = new LinearExpression.Builder(v);
            final LinearLiteral ll = new LinearLiteral(ls.build(), LinearLiteral.Operator.EQ);
            final IntegerClause clause = new IntegerClause(ll);
            clauses.add(clause);
        }
        if (factor > 1) {
            simplified.multiply(factor);
        }
        return simplified;
    }

    private static Set<IntegerClause> toRCSP(final Set<IntegerClause> clauses, final CompactOrderEncodingContext context, final CspFactory cf) {
        final Set<IntegerClause> newClauses = new LinkedHashSet<>();
        for (final IntegerClause c : clauses) {
            if (c.getArithmeticLiterals().isEmpty()) {
                newClauses.add(c);
            } else {
                final IntegerClause.Builder newClause = IntegerClause.Builder.cloneOnlyBool(c);
                for (final ArithmeticLiteral al : c.getArithmeticLiterals()) {
                    if (al instanceof LinearLiteral) {
                        final LinearLiteral ll = (LinearLiteral) al;
                        final LinearExpression ls = ll.getLinearExpression();
                        if (ll.getOperator() == LinearLiteral.Operator.EQ && ls.size() == 2 && ls.getB() == 0) {
                            final IntegerVariable v1 = ls.getCoef().firstKey();
                            final IntegerVariable v2 = ls.getCoef().lastKey();
                            final int c1 = ls.getA(v1);
                            final int c2 = ls.getA(v2);
                            if (c1 * c2 < 0) {
                                IntegerVariable lhs = Math.abs(c1) < Math.abs(c2) ? v1 : v2;
                                final IntegerVariable rhs = Math.abs(c1) <= Math.abs(c2) ? v2 : v1;
                                final int lc = Math.abs(ls.getA(lhs));
                                final int rc = Math.abs(ls.getA(rhs));
                                if (lc > 1) {
                                    final IntegerDomain dom = lhs.getDomain().mul(lc);
                                    final IntegerVariable av = context.newRCSPVariable(dom, cf);
                                    final ArithmeticLiteral lit = new EqMul(av, cf.constant(lc), lhs);
                                    newClauses.add(new IntegerClause(lit));
                                    lhs = av;
                                }
                                if (rc == 1) {
                                    newClause.addArithmeticLiteral(new OpXY(OpXY.Operator.EQ, lhs, rhs));
                                } else {
                                    newClause.addArithmeticLiterals(new EqMul(lhs, cf.constant(rc), rhs));
                                }
                                continue;
                            }
                        } else if (ll.getOperator() == LinearLiteral.Operator.EQ && ls.size() == 1) {
                            final IntegerVariable x = ls.getCoef().firstKey();
                            int a = ls.getA(x);
                            int b = ls.getB();
                            if (a * b <= 0) {
                                a = Math.abs(a);
                                b = Math.abs(b);
                                if (a == 1) {
                                    newClause.addArithmeticLiterals(new OpXY(OpXY.Operator.EQ, x, cf.constant(b)));
                                } else {
                                    newClause.addArithmeticLiterals(new EqMul(cf.constant(b), cf.constant(a), x));
                                }
                                continue;
                            }
                        }
                        LinearExpression.Builder lhs, rhs;
                        if (ls.getB() > 0) {
                            lhs = new LinearExpression.Builder(ls.getB());
                            rhs = new LinearExpression.Builder(0);
                        } else {
                            lhs = new LinearExpression.Builder(0);
                            rhs = new LinearExpression.Builder(-ls.getB());
                        }
                        for (final Map.Entry<IntegerVariable, Integer> es : ls.getCoef().entrySet()) {
                            int a = es.getValue();
                            final IntegerVariable v = es.getKey();
                            if (a == 1) {
                                lhs.setA(1, v);
                                continue;
                            } else if (a == -1) {
                                rhs.setA(1, v);
                                continue;
                            }
                            a = Math.abs(a);
                            assert v.getDomain().lb() == 0;
                            final IntegerDomain dom = v.getDomain().mul(a);
                            final IntegerVariable av = context.newRCSPVariable(dom, cf);
                            final ArithmeticLiteral lit = new EqMul(av, cf.constant(a), v);
                            newClauses.add(new IntegerClause(lit));
                            if (es.getValue() > 0) {
                                lhs.add(new LinearExpression(av));
                            } else {
                                rhs.add(new LinearExpression(av));
                            }
                        }

                        int lsize = lhs.size() + (lhs.getB() == 0 ? 0 : 1);
                        int rsize = rhs.size() + (rhs.getB() == 0 ? 0 : 1);
                        final LinearLiteral.Operator op = ll.getOperator();
                        boolean invert = false;
                        if (lsize > rsize) {
                            final LinearExpression.Builder tmp = lhs;
                            lhs = rhs;
                            rhs = tmp;
                            invert = true;
                            final int tmpsize = lsize;
                            lsize = rsize;
                            rsize = tmpsize;
                        }
                        assert lsize <= rsize;
                        assert lsize <= 2;
                        assert rsize <= 4;

                        if (rsize >= 3) {
                            rhs = simplifyForRCSP(rhs, newClauses, 2, context, cf);
                        } else if (rsize == 2 && lsize == 2) {
                            if (rhs.getB() == 0) {
                                rhs = simplifyForRCSP(rhs, newClauses, 1, context, cf);
                            } else {
                                final IntegerDomain dom = IntegerDomain.of(0, rhs.getDomain().ub());
                                final List<IntegerHolder> rh = getHolders(rhs, cf);
                                final IntegerVariable ax = context.newRCSPVariable(dom, cf);
                                final ArithmeticLiteral geB = new OpXY(OpXY.Operator.LE, cf.constant(rhs.getB()), ax);
                                final ArithmeticLiteral eqAdd = new OpAdd(OpAdd.Operator.EQ, ax, rh.get(0), rh.get(1));
                                newClauses.add(new IntegerClause(geB));
                                newClauses.add(new IntegerClause(eqAdd));
                                rhs = new LinearExpression.Builder(ax);
                            }
                        }

                        final List<IntegerHolder> lh = getHolders(lhs, cf);
                        final List<IntegerHolder> rh = getHolders(rhs, cf);
                        assert lh.size() + rh.size() <= 3;

                        ArithmeticLiteral lit = null;
                        if (lh.size() == 1 && rh.size() == 1) {
                            lit = new OpXY(OpXY.Operator.from(op), lh.get(0), rh.get(0), invert);
                        } else if (lh.size() == 1 && rh.size() == 2) {
                            lit = new OpAdd(OpAdd.Operator.from(op, invert), lh.get(0), rh.get(0), rh.get(1));
                        } else if (lh.size() == 2 && rh.size() == 1) {
                            if (op == LinearLiteral.Operator.LE && !invert) {
                                lit = new OpAdd(OpAdd.Operator.GE, rh.get(0), lh.get(0), lh.get(1));
                            } else if (op == LinearLiteral.Operator.LE && invert) {
                                lit = new OpAdd(OpAdd.Operator.LE, rh.get(0), lh.get(0), lh.get(1));
                            } else {
                                lit = new OpAdd(OpAdd.Operator.from(op, invert), rh.get(0), lh.get(0), lh.get(1));
                            }
                        }
                        if (lit != null) {
                            newClause.addArithmeticLiterals(lit);
                        }
                    } else {
                        throw new IllegalArgumentException("Cannot reduce " + al.getClass());
                    }
                }
                newClauses.add(newClause.build());
            }
        }
        return newClauses;
    }

    private static LinearExpression.Builder simplifyForRCSP(final LinearExpression.Builder e, final Set<IntegerClause> clauses, final int maxlen,
                                                            final CompactOrderEncodingContext context, final CspFactory cf) {
        final int esize = e.size() + (e.getB() == 0 ? 0 : 1);
        if (esize <= maxlen) {
            return e;
        }
        assert (esize == 4 && maxlen == 2) || (esize == 3 && maxlen == 2) || (esize == 2 && maxlen == 1);
        final List<IntegerHolder> holders = getHolders(e, cf);
        assert holders.size() <= 4;
        Collections.sort(holders);

        final IntegerHolder v0 = holders.get(0);
        final IntegerHolder v1 = holders.get(1);
        final IntegerDomain d = v0.getDomain().add(v1.getDomain());
        final IntegerVariable w0 = context.newRCSPVariable(d, cf);
        final ArithmeticLiteral lit0 = new OpAdd(OpAdd.Operator.EQ, w0, v0, v1);
        final IntegerClause clause0 = new IntegerClause(lit0);
        clauses.add(clause0);

        if (holders.size() == 2) {
            return new LinearExpression.Builder(w0);
        } else if (holders.size() == 3) {
            final LinearExpression.Builder ret = new LinearExpression.Builder(w0);
            final IntegerHolder v2 = holders.get(2);
            if (v2 instanceof IntegerConstant) {
                ret.setB(((IntegerConstant) v2).getValue());
            } else {
                ret.setA(1, (IntegerVariable) v2);
            }
            return ret;
        } else {
            assert holders.size() == 4;
            final IntegerHolder v2 = holders.get(2);
            final IntegerHolder v3 = holders.get(3);
            final IntegerDomain d2 = v2.getDomain().add(v3.getDomain());
            final IntegerVariable w1 = context.newRCSPVariable(d2, cf);
            final ArithmeticLiteral lit1 = new OpAdd(OpAdd.Operator.EQ, w1, v2, v3);
            final IntegerClause clause1 = new IntegerClause(lit1);
            clauses.add(clause1);
            final LinearExpression.Builder ret = new LinearExpression.Builder(w0);
            ret.setA(1, w1);
            return ret;
        }
    }

    private static List<IntegerHolder> getHolders(final LinearExpression.Builder e, final CspFactory cf) {
        final List<IntegerHolder> ret = new ArrayList<>(e.getVariables());
        if (e.size() == 0 || e.getB() > 0) {
            ret.add(cf.constant(e.getB()));
        }
        return ret;
    }

    static Set<IntegerClause> simplify(final Set<IntegerClause> clauses, final CompactOrderEncodingContext context, final FormulaFactory f) {
        return clauses.stream().flatMap(clause -> {
            if (clause.isValid()) {
                return null;
            } else if (CompactOrderEncoding.isSimpleClause(clause, context)) {
                return Stream.of(clause);
            } else {
                return simplifyClause(clause, context, f).stream();
            }
        }).collect(Collectors.toSet());
    }

    static Set<IntegerClause> simplifyClause(final IntegerClause clause, final CompactOrderEncodingContext context, final FormulaFactory f) {
        final Set<IntegerClause> newClauses = new LinkedHashSet<>();
        final IntegerClause.Builder c = IntegerClause.Builder.cloneOnlyBool(clause);
        for (final ArithmeticLiteral literal : clause.getArithmeticLiterals()) {
            if (CompactOrderEncoding.isSimpleLiteral(literal, context)) {
                c.addArithmeticLiteral(literal);
            } else {
                final Variable p = context.getOrderContext().addSimplifyBooleanVariable(f);
                final Literal notP = p.negate(f);
                final IntegerClause newClause = new IntegerClause(notP, literal);
                newClauses.add(newClause);
                c.addBooleanLiteral(p);
            }
        }
        newClauses.add(c.build());
        return newClauses;
    }
}
