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
import com.booleworks.logicng.csp.terms.IntegerConstant;
import com.booleworks.logicng.csp.terms.IntegerVariable;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.formulas.Literal;
import com.booleworks.logicng.formulas.Variable;
import com.booleworks.logicng.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CompactOrderReduction {

    public static final String AUX_PREFIX1 = "CRS";
    public static final String AUX_PREFIX2 = "CRR";

    public static Set<IntegerClause> reduce(final Csp csp, final CspEncodingContext context, final FormulaFactory f) {
        adjust();
        toTernary();
        toRCSP();

        simplify();
        CompactCSPReduction.toCCSP();

    }

    private static Set<IntegerClause> adjust(final Csp csp, final CspEncodingContext context, final CspFactory cf) {
        final Set<IntegerClause> adjustedClauses = new LinkedHashSet<>(csp.getClauses());
        for (final IntegerVariable v : csp.getInternalIntegerVariables()) {
            adjustVariable(v, true, adjustedClauses, csp, context, cf);
        }

        final Set<IntegerClause> newClauses = new LinkedHashSet<>();
        for (final IntegerClause c : adjustedClauses) {
            if (c.getArithmeticLiterals().isEmpty()) {
                newClauses.add(c);
            } else {
                final Set<Literal> booleanLiteral = new LinkedHashSet<>(c.getBoolLiterals());
                final Set<ArithmeticLiteral> arithLiterals = new LinkedHashSet<>();

                for (final ArithmeticLiteral lit : c.getArithmeticLiterals()) {
                    if (lit instanceof LinearLiteral) {
                        final LinearLiteral ll = (LinearLiteral) lit;
                        final LinearExpression ls = ll.getLinearExpression();
                        int b = ls.getB();
                        for (final var es : ls.getCoef().entrySet()) {
                            b += offset.get(es.getKey()) * es.getValue();
                        }
                        final LinearExpression newLs = new LinearExpression(ls.getCoef(), b);
                        final LinearLiteral newLl = new LinearLiteral(newLs, ll.getOperator());
                        newClauses.add(new IntegerClause(newLl));
                    } else {
                        throw new IllegalArgumentException("Reduction not supported for: " + lit.getClass());
                    }
                }
                newClauses.add(new IntegerClause(booleanLiteral, arithLiterals));
            }
        }
        return newClauses;
    }

    private static void adjustVariable(final IntegerVariable v, final boolean useOffset, final Set<IntegerClause> newClauses, final Csp.Builder csp, final CspEncodingContext context, final CspFactory cf) {
        final IntegerDomain d = v.getDomain();
        if (!d.isContiguous()) {
            int lst = d.lb() - 1;
            final Iterator<Integer> iter = d.iterator();
            while (iter.hasNext()) {
                final int i = iter.next();
                if (lst + 2 == i) {
                    final IntegerClause c = new IntegerClause(new LinearLiteral(new LinearExpression(1, v, -(lst + 1)), LinearLiteral.Operator.NE));
                    newClauses.add(c);
                } else if (lst + 1 != i) {
                    final Variable b = context.newAuxBoolVariable(cf.formulaFactory());
                    csp.addInternalBooleanVariable(b);
                    final Set<ArithmeticLiteral> arith1 = new LinkedHashSet<>();
                    final Set<Literal> bool1 = new LinkedHashSet<>();
                    arith1.add(new LinearLiteral(new LinearExpression(1, v, -lst), LinearLiteral.Operator.LE));
                    bool1.add(b.negate(cf.formulaFactory()));
                    newClauses.add(new IntegerClause(bool1, arith1));

                    final Set<ArithmeticLiteral> arith2 = new LinkedHashSet<>();
                    final Set<Literal> bool2 = new LinkedHashSet<>();
                    arith1.add(new LinearLiteral(new LinearExpression(-1, v, i), LinearLiteral.Operator.LE));
                    bool2.add(b);
                    newClauses.add(new IntegerClause(bool2, arith2));
                }
                lst = i;
            }
        }
        final int offset = d.lb();
        if (useOffset) {
            final IntegerDomain newD = IntegerDomain.of(0, d.ub() - offset);
            final IntegerVariable newVar = cf.auxVariable(AUX_PREFIX1, newD);
            context.getOffsets().put(v, new Pair(newVar, offset));
        } else {
            final IntegerClause c = new IntegerClause(new LinearLiteral(new LinearExpression(-1, v, offset), LinearLiteral.Operator.LE));
            newClauses.add(c);
            final IntegerDomain newD = IntegerDomain.of(0, d.ub());
            final IntegerVariable newVar = cf.auxVariable(AUX_PREFIX1, newD);
            context.getOffsets().put(v, new Pair(newVar, offset));
        }
    }

    private static Set<IntegerClause> toTernary(final Set<IntegerClause> clauses, final Csp.Builder csp, final CspEncodingContext context, final CspFactory cf) {
        final Set<IntegerClause> newClauses = new LinkedHashSet<>();
        for (final IntegerClause c : clauses) {
            final Set<Literal> booleanLiterals = new LinkedHashSet<>(c.getBoolLiterals());
            final Set<ArithmeticLiteral> arithLiterals = new LinkedHashSet<>();
            for (final ArithmeticLiteral lit : c.getArithmeticLiterals()) {
                if (lit instanceof LinearLiteral) {
                    final LinearLiteral ll = (LinearLiteral) lit;
                    final LinearExpression.Builder ls = simplifyToTernary(new LinearExpression.Builder(ll.getLinearExpression()), newClauses, csp, context, cf);
                    arithLiterals.add(new LinearLiteral(ls.build(), ll.getOperator()));
                } else {
                    arithLiterals.add(lit);
                }
            }
            newClauses.add(new IntegerClause(booleanLiterals, arithLiterals));
        }
        return newClauses;
    }

    private static LinearExpression.Builder simplifyToTernary(final LinearExpression.Builder exp, final Set<IntegerClause> clauses, final Csp.Builder csp, final CspEncodingContext context, final CspFactory cf) {
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
            final LinearExpression.Builder simplified = simplifyToTernaryExpression(ei, clauses, csp, context, cf);
            e.add(simplified.build());
        }

        for (final LinearExpression.Builder ei : OrderReduction.split(rhs.build(), rhs_len)) {
            final LinearExpression.Builder simplified = simplifyToTernaryExpression(ei, clauses, csp, context, cf);
            simplified.multiply(-1);
            e.add(simplified.build());
        }

        return e;
    }

    private static LinearExpression.Builder simplifyToTernaryExpression(final LinearExpression.Builder exp, final Set<IntegerClause> clauses, final Csp.Builder csp, final CspEncodingContext context, final CspFactory cf) {
        final int factor = exp.factor();
        final LinearExpression.Builder normalized = exp.normalize();
        LinearExpression.Builder simplified = simplifyToTernary(normalized, clauses, csp, context, cf);
        if (simplified.size() > 1) {
            final IntegerVariable v = cf.auxVariable(AUX_PREFIX1, simplified.getDomain());
            csp.addInternalIntegerVariable(v);
            adjustVariable(v, false, clauses, csp, context, cf);
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

    private static Set<IntegerClause> toRCSP(final Set<IntegerClause> clauses, final Csp.Builder csp, final CspFactory cf) {
        final Set<IntegerClause> newClauses = new LinkedHashSet<>();
        for (final IntegerClause c : clauses) {
            if (c.getArithmeticLiterals().isEmpty()) {
                newClauses.add(c);
            } else {
                final Set<Literal> booleanLiterals = new LinkedHashSet<>(c.getBoolLiterals());
                final Set<ArithmeticLiteral> arithmeticLiterals = new LinkedHashSet<>();
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
                                    final IntegerVariable av = cf.auxVariable(AUX_PREFIX2, dom);
                                    csp.addInternalIntegerVariable(av);
                                    final ArithmeticLiteral lit = new EqMul(av, cf.constant(lc), lhs);
                                    newClauses.add(new IntegerClause(lit));
                                    lhs = av;
                                }
                                if (rc == 1) {
                                    arithmeticLiterals.add(new OpXY(OpXY.Operator.EQ, lhs, rhs));
                                } else {
                                    arithmeticLiterals.add(new EqMul(lhs, cf.constant(rc), rhs));
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
                                    arithmeticLiterals.add(new OpXY(OpXY.Operator.EQ, x, cf.constant(b)));
                                } else {
                                    arithmeticLiterals.add(new EqMul(cf.constant(b), cf.constant(a), x));
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
                            final IntegerVariable av = cf.auxVariable(AUX_PREFIX2, dom);
                            csp.addInternalIntegerVariable(av);
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
                            rhs = simplifyForRCSP(rhs, newClauses, 2, csp, cf);
                        } else if (rsize == 2 && lsize == 2) {
                            if (rhs.getB() == 0) {
                                rhs = simplifyForRCSP(rhs, newClauses, 1, csp, cf);
                            } else {
                                final IntegerDomain dom = IntegerDomain.of(0, rhs.getDomain().ub());
                                final List<IntegerHolder> rh = getHolders(rhs, cf);
                                final IntegerVariable ax = cf.auxVariable(AUX_PREFIX2, dom);
                                csp.addInternalIntegerVariable(ax);
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
                            arithmeticLiterals.add(lit);
                        }
                    } else {
                        throw new IllegalArgumentException("Cannot reduce " + al.getClass());
                    }
                }
                newClauses.add(new IntegerClause(booleanLiterals, arithmeticLiterals));
            }
        }
        return newClauses;
    }

    private static LinearExpression.Builder simplifyForRCSP(final LinearExpression.Builder e, final Set<IntegerClause> clauses, final int maxlen, final Csp.Builder csp, final CspFactory cf) {
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
        final IntegerVariable w0 = cf.auxVariable(AUX_PREFIX2, v0.getDomain().add(v1.getDomain()));
        csp.addInternalIntegerVariable(w0);
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
            final IntegerVariable w1 = cf.auxVariable(AUX_PREFIX2, v2.getDomain().add(v3.getDomain()));
            csp.addInternalIntegerVariable(w1);
            final ArithmeticLiteral lit1 = new OpAdd(OpAdd.Operator.EQ, w1, v2, v3);
            final IntegerClause clause1 = new IntegerClause(lit1);
            clauses.add(clause1);
            final LinearExpression.Builder ret = new LinearExpression.Builder(w0);
            ret.setA(1, w1);
            return ret;
        }
    }

    private static List<IntegerHolder> getHolders(final LinearExpression.Builder e, final CspFactory cf) {
        final List<IntegerHolder> ret = new ArrayList<>();
        for (final IntegerVariable v : e.getVariables()) {
            ret.add(v);
        }
        if (e.size() == 0 || e.getB() > 0) {
            ret.add(cf.constant(e.getB()));
        }
        return ret;
    }

    private static Set<IntegerClause> simplify(final Set<IntegerClause> clauses, final CspEncodingContext context, final Csp.Builder csp, final FormulaFactory f) {
        return OrderReduction.simplify(clauses, context, csp, f);
    }

}
