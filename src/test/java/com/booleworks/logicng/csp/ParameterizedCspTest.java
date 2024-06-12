package com.booleworks.logicng.csp;

import com.booleworks.logicng.formulas.FormulaFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class ParameterizedCspTest {
    public static Collection<CspFactory> cspFactories() {
        final List<CspFactory> factories = new ArrayList<>();
        factories.add(new CspFactory(FormulaFactory.caching()));
        factories.add(new CspFactory(FormulaFactory.nonCaching()));
        return factories;
    }
}
