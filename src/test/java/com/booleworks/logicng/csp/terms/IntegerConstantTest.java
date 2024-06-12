package com.booleworks.logicng.csp.terms;

import static org.assertj.core.api.Assertions.assertThat;

import com.booleworks.logicng.csp.CspFactory;
import com.booleworks.logicng.csp.ParameterizedCspTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class IntegerConstantTest extends ParameterizedCspTest {

    @ParameterizedTest
    @MethodSource("cspFactories")
    public void testType(final CspFactory cf) {
        assertThat(cf.constant(0).getType()).isEqualTo(Term.Type.ZERO);
        assertThat(cf.constant(1).getType()).isEqualTo(Term.Type.ONE);
        assertThat(cf.constant(2).getType()).isEqualTo(Term.Type.CONST);
        assertThat(cf.constant(-1).getType()).isEqualTo(Term.Type.CONST);
    }

    @ParameterizedTest
    @MethodSource("cspFactories")
    public void testZeroAndOne(final CspFactory cf) {
        final Term zero = cf.zero();
        assertThat(zero.getType()).isEqualTo(Term.Type.ZERO);
        assertThat(zero).isSameAs(cf.constant(0));
        assertThat(zero).isSameAs(cf.zero());

        final Term one = cf.one();
        assertThat(one.getType()).isEqualTo(Term.Type.ONE);
        assertThat(one).isSameAs(cf.constant(1));
        assertThat(one).isSameAs(cf.one());
    }

    @ParameterizedTest
    @MethodSource("cspFactories")
    public void testCaching(final CspFactory cf) {
        assertThat(cf.constant(2)).isSameAs(cf.constant(2));
        assertThat(cf.constant(2)).isNotSameAs(cf.constant(3));
    }

    @ParameterizedTest
    @MethodSource("cspFactories")
    public void testEquality(final CspFactory cf) {
        assertThat(cf.constant(0)).isEqualTo(new IntegerConstant(0));
        assertThat(cf.constant(1)).isEqualTo(new IntegerConstant(1));
        assertThat(cf.constant(2)).isEqualTo(new IntegerConstant(2));
        assertThat(cf.constant(2)).isNotEqualTo(new IntegerConstant(0));
        assertThat(cf.constant(2)).isNotEqualTo(new IntegerConstant(1));
        assertThat(cf.constant(2)).isNotEqualTo(new IntegerConstant(-1));
    }
}
