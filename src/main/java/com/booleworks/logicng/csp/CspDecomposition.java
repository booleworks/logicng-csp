package com.booleworks.logicng.csp;

import com.booleworks.logicng.csp.predicates.CspPredicate;
import com.booleworks.logicng.formulas.FType;
import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.Literal;
import com.booleworks.logicng.formulas.Not;

import java.util.Set;
import java.util.TreeSet;

public class CspDecomposition {
    public static Set<IntegerClause> decompose(final Formula formula, final CspFactory cf) {
        final Formula nnf = formula.nnf(cf.formulaFactory());
        final Set<IntegerClause> clauses = new TreeSet<>();
        decomposeRecursive(nnf, cf, clauses);
        return clauses;
    }

    private static void decomposeRecursive(final Formula formula, final CspFactory cf, Set<IntegerClause> clauses) {
        switch(formula.type()) {
            case AND:
                for(final Formula op: formula) {
                    decomposeRecursive(op, cf, clauses);
                }
                break;
            case OR:
                Set<IntegerClause> factorized = null;
                for(final Formula op: formula) {
                    Set<IntegerClause> disj = new TreeSet<>();
                    decomposeRecursive(op, cf, disj);
                    if(factorized == null) {
                        factorized = disj;
                    } else {
                        factorized = IntegerClause.factorize(factorized, disj);
                    }
                    if(factorized.isEmpty()) {
                        break;
                    }
                }
                if(factorized != null) {
                    clauses.addAll(factorized);
                }
                break;
            case LITERAL:
                clauses.add(new IntegerClause((Literal) formula));
                break;
            case PREDICATE:
                if(formula instanceof CspPredicate) {
                    clauses.addAll(((CspPredicate) formula).decompose(cf));
                } else {
                    throw new RuntimeException("Cannot decompose predicates of type: " + formula.getClass());
                }
                break;
            case NOT:
                final Not not = (Not) formula;
                assert(not.operand().type() == FType.PREDICATE);
                if(not.operand() instanceof CspPredicate) {
                    decomposeRecursive(((CspPredicate) not.operand()).negate(cf).nnf(cf.formulaFactory()), cf, clauses);
                } else {
                    throw new RuntimeException("Cannot decompose predicates of type: " + not.operand().getClass());
                }
                break;
            default:
                throw new RuntimeException("Cannot decompose formula of type: " + formula.getClass());
        }
    }
}
