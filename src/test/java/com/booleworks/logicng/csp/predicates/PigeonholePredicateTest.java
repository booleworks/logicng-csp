package com.booleworks.logicng.csp.predicates;

import static org.assertj.core.api.Assertions.assertThat;

import com.booleworks.logicng.csp.CspFactory;
import com.booleworks.logicng.csp.ParameterizedCspTest;
import com.booleworks.logicng.csp.terms.IntegerVariable;
import com.booleworks.logicng.csp.terms.Term;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

public class PigeonholePredicateTest extends ParameterizedCspTest {
    @ParameterizedTest
    @MethodSource("cspFactories")
    public void testType(final CspFactory cf) {
        assertThat(cf.pigeonhole(List.of()).getType()).isEqualTo(CspPredicate.Type.PIGEONHOLE);
        assertThat(cf.pigeonhole(List.of(cf.variable("a", 0, 20), cf.one(), cf.constant(2))).getType()).isEqualTo(CspPredicate.Type.PIGEONHOLE);
    }

    @ParameterizedTest
    @MethodSource("cspFactories")
    public void testSimpleExamples(final CspFactory cf) {
        final IntegerVariable a = cf.variable("a", 0, 5);
        final IntegerVariable b = cf.variable("b", 10, 15);
        final Term term1 = cf.add(a, b);
        final Term term2 = cf.mul(2, a);
        final PigeonholePredicate pred1 = cf.pigeonhole(List.of(cf.zero(), cf.one()));
        final PigeonholePredicate pred2 = cf.pigeonhole(List.of(term1, term2));
        final PigeonholePredicate pred3 = cf.pigeonhole(List.of(term1, term2, cf.zero(), cf.one(), cf.constant(20)));

        assertThat(pred1.terms).containsExactlyInAnyOrder(cf.zero(), cf.one());
        assertThat(pred1.type).isEqualTo(CspPredicate.Type.PIGEONHOLE);

        assertThat(pred2.terms).containsExactlyInAnyOrder(term1, term2);
        assertThat(pred2.type).isEqualTo(CspPredicate.Type.PIGEONHOLE);

        assertThat(pred3.terms).containsExactlyInAnyOrder(term1, term2, cf.zero(), cf.one(), cf.constant(20));
        assertThat(pred3.type).isEqualTo(CspPredicate.Type.PIGEONHOLE);
    }

    @ParameterizedTest
    @MethodSource("cspFactories")
    public void testNegation(final CspFactory cf) {
        final IntegerVariable a = cf.variable("a", 0, 5);
        final IntegerVariable b = cf.variable("b", 10, 15);
        final Term term1 = cf.add(a, b);
        final Term term2 = cf.mul(2, a);
        final AllDifferentPredicate pred1 = cf.pigeonhole(List.of(cf.zero(), cf.one())).negate(cf);
        final AllDifferentPredicate pred2 = cf.pigeonhole(List.of(term1, term2)).negate(cf);
        final AllDifferentPredicate pred3 = cf.pigeonhole(List.of(term1, term2, cf.zero(), cf.one(), cf.constant(20))).negate(cf);

        assertThat(pred1.terms).containsExactlyInAnyOrder(cf.zero(), cf.one());
        assertThat(pred1.type).isEqualTo(CspPredicate.Type.ALLDIFFERENT);

        assertThat(pred2.terms).containsExactlyInAnyOrder(term1, term2);
        assertThat(pred2.type).isEqualTo(CspPredicate.Type.ALLDIFFERENT);

        assertThat(pred3.terms).containsExactlyInAnyOrder(term1, term2, cf.zero(), cf.one(), cf.constant(20));
        assertThat(pred3.type).isEqualTo(CspPredicate.Type.ALLDIFFERENT);
    }

    @ParameterizedTest
    @MethodSource("cspFactories")
    public void testCaching(final CspFactory cf) {
        final IntegerVariable a = cf.variable("a", 0, 5);
        final IntegerVariable b = cf.variable("b", 10, 15);
        final Term term1 = cf.add(a, b);
        final Term term2 = cf.mul(2, a);
        final PigeonholePredicate pred1 = cf.pigeonhole(List.of(cf.zero(), cf.one()));
        final PigeonholePredicate pred2 = cf.pigeonhole(List.of(term1, term2));
        final PigeonholePredicate pred3 = cf.pigeonhole(List.of(term1, term2, cf.zero(), cf.one(), cf.constant(20)));

        assertThat(pred1).isSameAs(cf.pigeonhole(List.of(cf.zero(), cf.one())));
        assertThat(pred2).isSameAs(cf.pigeonhole(List.of(term1, term2)));
        assertThat(pred3).isSameAs(cf.pigeonhole(List.of(term1, term2, cf.zero(), cf.one(), cf.constant(20))));
    }

    //@ParameterizedTest
    //@MethodSource("cspFactories")
    //public void testDecomposition(final CspFactory cf) {
    //    final IntegerVariable a = cf.variable("a", 0, 2);
    //    final IntegerVariable b = cf.variable("b", 0, 2);
    //    final IntegerVariable c = cf.variable("c", 0, 2);
    //    final IntegerVariable d = cf.variable("d", 0, 2);
    //    final Set<IntegerClause> pred1 = cf.pigeonhole(List.of(cf.zero(), cf.one(), a)).decompose(cf);
    //    final Set<IntegerClause> pred2 = cf.pigeonhole(List.of(a, b, c)).decompose(cf);
    //    final Set<IntegerClause> pred3 = cf.pigeonhole(List.of(a, b, c, d)).decompose(cf);
    //
    //    final SortedMap<IntegerVariable, Integer> coefsAB = new TreeMap<>();
    //    final SortedMap<IntegerVariable, Integer> coefsAC = new TreeMap<>();
    //    final SortedMap<IntegerVariable, Integer> coefsAD = new TreeMap<>();
    //    final SortedMap<IntegerVariable, Integer> coefsBC = new TreeMap<>();
    //    final SortedMap<IntegerVariable, Integer> coefsBD = new TreeMap<>();
    //    final SortedMap<IntegerVariable, Integer> coefsCD = new TreeMap<>();
    //    coefsAB.put(a, 1);
    //    coefsAB.put(b, -1);
    //    coefsAC.put(a, 1);
    //    coefsAC.put(c, -1);
    //    coefsAD.put(a, 1);
    //    coefsAD.put(d, -1);
    //    coefsBC.put(b, 1);
    //    coefsBC.put(c, -1);
    //    coefsBD.put(b, 1);
    //    coefsBD.put(d, -1);
    //    coefsCD.put(c, 1);
    //    coefsCD.put(d, -1);
    //
    //    System.out.println(pred1);
    //    System.out.println(pred2);
    //    System.out.println(pred3);
    //    assertThat(pred1).hasSize(1);
    //    assertThat(pred1).containsExactlyInAnyOrder(
    //            new IntegerClause(Common.treeSetFrom(), Common.treeSetFrom(
    //                    new LinearLiteral(new LinearExpression(-1, a, 1), LinearLiteral.Operator.EQ),
    //                    new LinearLiteral(new LinearExpression(-1, a, 1), LinearLiteral.Operator.EQ)
    //            ))
    //    );
    //    assertThat(pred2).hasSize(1);
    //    assertThat(pred2).containsExactlyInAnyOrder(
    //            new IntegerClause(Common.treeSetFrom(), Common.treeSetFrom(
    //                    new LinearLiteral(new LinearExpression(coefsAB, 0), LinearLiteral.Operator.EQ),
    //                    new LinearLiteral(new LinearExpression(coefsAC, 0), LinearLiteral.Operator.EQ),
    //                    new LinearLiteral(new LinearExpression(coefsBC, 0), LinearLiteral.Operator.EQ)
    //            ))
    //    );
    //    assertThat(pred3).hasSize(1);
    //    assertThat(pred3).containsExactlyInAnyOrder(
    //            new IntegerClause(Common.treeSetFrom(), Common.treeSetFrom(
    //                    new LinearLiteral(new LinearExpression(coefsAB, 0), LinearLiteral.Operator.EQ),
    //                    new LinearLiteral(new LinearExpression(coefsAC, 0), LinearLiteral.Operator.EQ),
    //                    new LinearLiteral(new LinearExpression(coefsAD, 0), LinearLiteral.Operator.EQ),
    //                    new LinearLiteral(new LinearExpression(coefsBC, 0), LinearLiteral.Operator.EQ),
    //                    new LinearLiteral(new LinearExpression(coefsBD, 0), LinearLiteral.Operator.EQ),
    //                    new LinearLiteral(new LinearExpression(coefsCD, 0), LinearLiteral.Operator.EQ)
    //            ))
    //    );
    //}
}
