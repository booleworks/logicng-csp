package com.booleworks.logicng.csp.terms;

import static org.assertj.core.api.Assertions.assertThat;

import com.booleworks.logicng.csp.CspFactory;
import com.booleworks.logicng.csp.ParameterizedCspTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class MultiplicationFunctionTest extends ParameterizedCspTest {
    @ParameterizedTest
    @MethodSource("cspFactories")
    public void testType(final CspFactory cf) {
        final IntegerVariable a = cf.variable("a", 0, 10);
        final IntegerVariable b = cf.variable("b", 0, 10);
        final IntegerConstant c = cf.constant(5);
        final IntegerConstant d = cf.constant(10);

        assertThat(cf.mul(0, a).getType()).isEqualTo(Term.Type.ZERO);
        assertThat(cf.mul(c, cf.zero()).getType()).isEqualTo(Term.Type.ZERO);
        assertThat(cf.mul(c, d).getType()).isEqualTo(Term.Type.CONST);
        assertThat(cf.mul(c, a).getType()).isEqualTo(Term.Type.MUL);
        assertThat(cf.mul(c, cf.add(a, b, c)).getType()).isEqualTo(Term.Type.MUL);
        assertThat(cf.mul(c, cf.sub(a, b)).getType()).isEqualTo(Term.Type.MUL);
        assertThat(cf.mul(c, cf.mul(c, a)).getType()).isEqualTo(Term.Type.MUL);
        assertThat(cf.mul(c, cf.minus(a)).getType()).isEqualTo(Term.Type.MUL);
    }

    @ParameterizedTest
    @MethodSource("cspFactories")
    public void testEquivalence(final CspFactory cf) {
        final IntegerVariable a = cf.variable("a", 0, 10);
        final IntegerVariable b = cf.variable("b", 0, 10);
        final IntegerConstant c = cf.constant(5);
        final IntegerConstant d = cf.constant(10);

        assertThat(cf.mul(0, a)).isEqualTo(cf.zero());
        assertThat(cf.mul(c, cf.zero())).isEqualTo(cf.zero());
        assertThat(cf.mul(c, d)).isEqualTo(cf.constant(50));
        assertThat(cf.mul(c, a)).isEqualTo(new MultiplicationFunction(c, a));
        assertThat(cf.mul(c, cf.add(a, b, c))).isEqualTo(new MultiplicationFunction(c, cf.add(a, b, c)));
        assertThat(cf.mul(c, cf.sub(a, b))).isEqualTo(new MultiplicationFunction(c, cf.sub(a, b)));
        assertThat(cf.mul(c, cf.mul(c, a))).isEqualTo(new MultiplicationFunction(c, cf.mul(c, a)));
        assertThat(cf.mul(c, cf.minus(a))).isEqualTo(new MultiplicationFunction(c, cf.minus(a)));
    }

    @ParameterizedTest
    @MethodSource("cspFactories")
    public void testCaching(final CspFactory cf) {
        final IntegerVariable a = cf.variable("a", 0, 10);
        final IntegerVariable b = cf.variable("b", 0, 10);
        final IntegerConstant c = cf.constant(5);
        final IntegerConstant d = cf.constant(10);

        assertThat(cf.mul(0, a)).isSameAs(cf.zero());
        assertThat(cf.mul(c, cf.zero())).isSameAs(cf.zero());
        assertThat(cf.mul(c, d)).isSameAs(cf.constant(50));
        assertThat(cf.mul(c, a)).isSameAs(cf.mul(c, a));
        assertThat(cf.mul(c, cf.add(a, b, c))).isSameAs(cf.mul(c, cf.add(a, b, c)));
        assertThat(cf.mul(c, cf.sub(a, b))).isSameAs(cf.mul(c, cf.sub(a, b)));
        assertThat(cf.mul(c, cf.mul(c, a))).isSameAs(cf.mul(c, cf.mul(c, a)));
        assertThat(cf.mul(c, cf.minus(a))).isSameAs(cf.mul(c, cf.minus(a)));
    }
}
