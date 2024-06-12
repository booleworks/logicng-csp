package com.booleworks.logicng.csp.terms;

import static org.assertj.core.api.Assertions.assertThat;

import com.booleworks.logicng.csp.CspFactory;
import com.booleworks.logicng.csp.ParameterizedCspTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedHashSet;
import java.util.List;

public class AdditionFunctionTest extends ParameterizedCspTest {

    @ParameterizedTest
    @MethodSource("cspFactories")
    public void testType(final CspFactory cf) {
        final IntegerVariable a = cf.variable("a", 0, 10);
        final IntegerVariable b = cf.variable("b", 0, 10);
        final IntegerConstant c = cf.constant(5);

        assertThat(cf.add(c, c).getType()).isEqualTo(Term.Type.CONST);
        assertThat(cf.add(c, c, c, c).getType()).isEqualTo(Term.Type.CONST);
        assertThat(cf.add(a, b).getType()).isEqualTo(Term.Type.ADD);
        assertThat(cf.add(a, c).getType()).isEqualTo(Term.Type.ADD);
    }

    @ParameterizedTest
    @MethodSource("cspFactories")
    public void testCompactification(final CspFactory cf) {
        final IntegerVariable a = cf.variable("a", 0, 10);
        final IntegerVariable b = cf.variable("b", 0, 10);
        final IntegerConstant c = cf.constant(5);
        final IntegerConstant d = cf.constant(10);

        assertThat(cf.add(a, cf.zero())).isSameAs(a);
        assertThat(cf.add(c, cf.zero())).isSameAs(c);
        assertThat(cf.add(a, b, cf.zero())).isSameAs(cf.add(a, b));
        assertThat(cf.add(c, d)).isSameAs(cf.constant(15));
        assertThat(cf.add(a, b, c)).isEqualTo(new AdditionFunction(new LinkedHashSet<>(List.of(a, b, c))));
        assertThat(cf.add(a, a)).isSameAs(cf.mul(2, a));
        assertThat(cf.add(a, a, c)).isSameAs(cf.add(cf.mul(2, a), c));
        assertThat(cf.add(a, cf.add(b, cf.add(a, c)), cf.add(a, c))).isSameAs(cf.add(cf.mul(3, a), b, cf.constant(c.getValue() * 2)));
    }

    @ParameterizedTest
    @MethodSource("cspFactories")
    public void testCaching(final CspFactory cf) {
        final IntegerVariable a = cf.variable("a", 0, 10);
        final IntegerVariable b = cf.variable("b", 0, 10);
        final IntegerConstant c = cf.constant(5);

        assertThat(cf.add(a, b, c)).isSameAs(cf.add(a, b, c));
        assertThat(cf.add(a, b, c)).isSameAs(cf.add(a, c, b));
        assertThat(cf.add(a, b, c)).isSameAs(cf.add(b, a, c));
        assertThat(cf.add(a, cf.add(b, c))).isSameAs(cf.add(c, cf.add(b, a)));
        assertThat(cf.add(a, b)).isNotSameAs(cf.add(a, c));
        assertThat(cf.add(a, b)).isNotSameAs(cf.add(a, a));
    }
}
