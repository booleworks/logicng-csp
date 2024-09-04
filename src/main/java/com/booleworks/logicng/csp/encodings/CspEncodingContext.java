package com.booleworks.logicng.csp.encodings;

import com.booleworks.logicng.csp.terms.IntegerVariable;
import com.booleworks.logicng.formulas.Variable;

import java.util.Collection;
import java.util.Set;

public interface CspEncodingContext {
    String CSP_AUX_LNG_VARIABLE = "CSP";

    CspEncodingAlgorithm getAlgorithm();

    Set<Variable> getSatVariables(final Collection<IntegerVariable> variables);

    boolean isEncoded(final IntegerVariable v);

    static OrderEncodingContext order() {
        return new OrderEncodingContext();
    }

    static CompactOrderEncodingContext compactOrder(final int base) {
        return new CompactOrderEncodingContext(base);
    }
}