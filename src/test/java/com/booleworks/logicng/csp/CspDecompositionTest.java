package com.booleworks.logicng.csp;

import static org.assertj.core.api.Assertions.assertThat;

import com.booleworks.logicng.csp.encodings.CspEncodingContext;
import com.booleworks.logicng.csp.functions.CspModelEnumeration;
import com.booleworks.logicng.csp.literals.LinearLiteral;
import com.booleworks.logicng.csp.predicates.CspPredicate;
import com.booleworks.logicng.csp.terms.IntegerVariable;
import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.formulas.Variable;
import com.booleworks.logicng.io.parsers.ParserException;
import com.booleworks.logicng.solvers.SATSolver;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class CspDecompositionTest extends ParameterizedCspTest {

    @ParameterizedTest
    @MethodSource("cspFactories")
    public void testFormulas(final CspFactory cf) throws ParserException {
        final FormulaFactory f = cf.formulaFactory();
        final IntegerVariable a = cf.variable("a", 0, 3);
        final IntegerVariable b = cf.variable("b", 3, 5);
        final IntegerVariable c = cf.variable("c", 1, 2);
        final Variable A = f.variable("A");
        final Variable B = f.variable("B");
        final Variable C = f.variable("C");
        final Variable D = f.variable("D");

        final Formula formula1 = f.parse("A & B");
        final Formula formula2 = f.parse("A & ~(B | C)");
        final Formula formula3 = f.or(
                cf.eq(a, cf.constant(2)),
                f.and(C, D)
        );
        final Formula formula4 = f.not(cf.allDifferent(List.of(a, b, c, cf.add(a, b), cf.add(b, c), cf.add(a, c))));

        final CspPredicate.Decomposition decomp1 = cf.decompose(formula1);
        final CspPredicate.Decomposition decomp2 = cf.decompose(formula2);
        final CspPredicate.Decomposition decomp3 = cf.decompose(formula3);
        final CspPredicate.Decomposition decomp4 = cf.decompose(formula4);

        assertThat(decomp1.getClauses()).containsExactlyInAnyOrder(new IntegerClause(A), new IntegerClause(B));
        assertThat(decomp1.getAuxiliaryBooleanVariables()).isEmpty();
        assertThat(decomp1.getAuxiliaryIntegerVariables()).isEmpty();
        assertThat(decomp2.getClauses()).containsExactlyInAnyOrder(new IntegerClause(A), new IntegerClause(B.negate(f)), new IntegerClause(C.negate(f)));
        assertThat(decomp2.getAuxiliaryBooleanVariables()).isEmpty();
        assertThat(decomp2.getAuxiliaryIntegerVariables()).isEmpty();
        final LinearLiteral l = new LinearLiteral(new LinearExpression(1, a, -2), LinearLiteral.Operator.EQ);
        assertThat(decomp3.getClauses()).containsExactlyInAnyOrder(Common.integerClauseFrom(C, l), Common.integerClauseFrom(D, l));
        assertThat(decomp3.getAuxiliaryBooleanVariables()).isEmpty();
        assertThat(decomp3.getAuxiliaryIntegerVariables()).isEmpty();
        assertThat(decomp4.getClauses()).containsExactlyInAnyOrder(
                new IntegerClause(Common.treeSetFrom(), Common.treeSetFrom(lt(-2, a), lt(-1, a, 1, b),
                        lt(1, a, -1, b), lt(1, a, -1, c), lt(-1, a, 1, b, -1, c)))
        );
        assertThat(decomp4.getAuxiliaryBooleanVariables()).isEmpty();
        assertThat(decomp4.getAuxiliaryIntegerVariables()).isEmpty();
    }

    private static LinearLiteral lt(final int c0, final IntegerVariable a0) {
        final SortedMap<IntegerVariable, Integer> coefs = new TreeMap<>();
        coefs.put(a0, c0);
        return new LinearLiteral(new LinearExpression(coefs, 0), LinearLiteral.Operator.EQ);
    }

    private static LinearLiteral lt(final int c0, final IntegerVariable a0, final int c1, final IntegerVariable a1) {
        final SortedMap<IntegerVariable, Integer> coefs = new TreeMap<>();
        coefs.put(a0, c0);
        coefs.put(a1, c1);
        return new LinearLiteral(new LinearExpression(coefs, 0), LinearLiteral.Operator.EQ);
    }

    private static LinearLiteral lt(final int c0, final IntegerVariable a0, final int c1, final IntegerVariable a1, final int c2, final IntegerVariable a2) {
        final SortedMap<IntegerVariable, Integer> coefs = new TreeMap<>();
        coefs.put(a0, c0);
        coefs.put(a1, c1);
        coefs.put(a2, c2);
        return new LinearLiteral(new LinearExpression(coefs, 0), LinearLiteral.Operator.EQ);
    }

    @ParameterizedTest
    @MethodSource("cspFactories")
    public void testX(final CspFactory cf) {
        final FormulaFactory f = cf.formulaFactory();
        final IntegerVariable a = cf.variable("a", -20, 10);
        final IntegerVariable b = cf.variable("b", 10, 20);
        final CspPredicate p = cf.ne(cf.max(b, cf.constant(15)), cf.add(a, b, cf.constant(2)));
        final Csp csp = cf.buildCsp(p);
        final CspEncodingContext context = new CspEncodingContext();
        final var encoded = cf.encodeCsp(csp, context);
        final SATSolver solver = SATSolver.newSolver(f);
        solver.add(encoded);
        final List<CspAssignment> models = CspModelEnumeration.enumerate(solver, csp, context, cf);

        final SATSolver solver2 = SATSolver.newSolver(f);
        final CspEncodingContext context2 = new CspEncodingContext();
        final var v1 = cf.encodeVariable(a, context2);
        final var v2 = cf.encodeVariable(b, context2);
        final var p1 = cf.encodeConstraint(p, context2);
        solver2.add(v1);
        solver2.add(v2);
        solver2.add(p1);
        final List<CspAssignment> models2 = CspModelEnumeration.enumerate(solver2, List.of(a, b), Collections.emptyList(), context2, cf);

        assertThat(models.size()).isEqualTo(models2.size());
        assertThat(models).containsExactlyInAnyOrderElementsOf(models2);
    }

    @ParameterizedTest
    @MethodSource("cspFactories")
    public void testY(final CspFactory cf) {
        final FormulaFactory f = cf.formulaFactory();
        final IntegerVariable b = cf.variable("b", 0, 10);
        final IntegerVariable m = cf.variable("m", 5, 10);
        final Formula formula = f.or(cf.le(m, cf.constant(5)), cf.le(m, b), f.variable("A"));
        final Csp csp = cf.buildCsp(formula);
        final CspEncodingContext context = new CspEncodingContext();
        final var encoded = cf.encodeCsp(csp, context);
        encoded.forEach(System.out::println);
        final SATSolver solver = SATSolver.newSolver(f);
        solver.add(encoded);
        for (final var model : CspModelEnumeration.enumerate(solver, csp, context, cf)) {
            System.out.println(model);
        }
    }
}
