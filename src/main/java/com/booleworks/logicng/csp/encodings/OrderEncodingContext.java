package com.booleworks.logicng.csp.encodings;

import com.booleworks.logicng.csp.CspFactory;
import com.booleworks.logicng.csp.datastructures.domains.IntegerDomain;
import com.booleworks.logicng.csp.terms.IntegerVariable;
import com.booleworks.logicng.datastructures.EncodingResult;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.formulas.Variable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class OrderEncodingContext implements CspEncodingContext {
    private final Map<IntegerVariable, Map<Integer, Variable>> variableMap;
    private final List<Variable> simplifyBoolVariables;
    private final List<IntegerVariable> simplifyIntVariables;

    OrderEncodingContext() {
        this.variableMap = new TreeMap<>();
        this.simplifyBoolVariables = new ArrayList<>();
        this.simplifyIntVariables = new ArrayList<>();
    }

    public OrderEncodingContext(final OrderEncodingContext context) {
        this.variableMap = new TreeMap<>(context.variableMap);
        this.simplifyBoolVariables = new ArrayList<>(context.simplifyBoolVariables);
        this.simplifyIntVariables = new ArrayList<>(context.simplifyIntVariables);
    }

    @Override
    public CspEncodingAlgorithm getAlgorithm() {
        return CspEncodingAlgorithm.Order;
    }

    IntegerVariable addSimplifyIntVariable(final IntegerDomain domain, final CspFactory cf) {
        final IntegerVariable var = cf.auxVariable(OrderReduction.AUX_SIMPLE, domain);
        this.simplifyIntVariables.add(var);
        return var;
    }

    Variable addSimplifyBooleanVariable(final FormulaFactory f) {
        final Variable var = f.newAuxVariable(CSP_AUX_LNG_VARIABLE);
        this.simplifyBoolVariables.add(var);
        return var;
    }

    Variable intVariableInstance(final IntegerVariable group, final int index, final EncodingResult result) {
        final Map<Integer, Variable> intMap = this.variableMap.computeIfAbsent(group, k -> new TreeMap<>());
        return intMap.computeIfAbsent(index, i -> result.newVariable(CSP_AUX_LNG_VARIABLE));
    }

    public Map<IntegerVariable, Map<Integer, Variable>> getVariableMap() {
        return Collections.unmodifiableMap(this.variableMap);
    }

    @Override
    public Set<Variable> getSatVariables(final Collection<IntegerVariable> variables) {
        return variables.stream().map(variableMap::get).filter(Objects::nonNull).flatMap(m -> m.values().stream()).collect(Collectors.toSet());
    }

    @Override
    public boolean isEncoded(final IntegerVariable v) {
        return variableMap.containsKey(v);
    }

    public Set<IntegerVariable> getIntegerVariables() {
        return this.variableMap.keySet();
    }

    public List<Variable> getSimplifyBoolVariables() {
        return this.simplifyBoolVariables;
    }

    public List<IntegerVariable> getSimplifyIntVariables() {
        return this.simplifyIntVariables;
    }
}
