package com.booleworks.logicng.csp.encodings;

import com.booleworks.logicng.csp.CspFactory;
import com.booleworks.logicng.csp.IntegerDomain;
import com.booleworks.logicng.csp.terms.IntegerVariable;
import com.booleworks.logicng.datastructures.EncodingResult;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.formulas.Variable;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class OrderEncodingContext implements CspEncodingContext {
    private final Map<IntegerVariable, Map<Integer, Variable>> variableMap;
    private final Set<Variable> booleanAuxVariables;
    private final Set<IntegerVariable> integerAuxVariables;

    OrderEncodingContext() {
        this.variableMap = new TreeMap<>();
        this.booleanAuxVariables = new TreeSet<>();
        this.integerAuxVariables = new TreeSet<>();
    }

    OrderEncodingContext(final OrderEncodingContext context) {
        this.variableMap = new TreeMap<>(context.variableMap);
        this.booleanAuxVariables = new TreeSet<>(context.booleanAuxVariables);
        this.integerAuxVariables = new TreeSet<>(context.integerAuxVariables);
    }

    @Override
    public CspEncodingAlgorithm getAlgorithm() {
        return CspEncodingAlgorithm.Order;
    }

    IntegerVariable newAuxIntVariable(final String prefix, final IntegerDomain domain, final CspFactory cf) {
        final IntegerVariable var = cf.auxVariable(prefix, domain);
        this.integerAuxVariables.add(var);
        return var;
    }

    Variable newAuxBoolVariable(final FormulaFactory f) {
        final Variable var = f.newAuxVariable(CSP_AUX_LNG_VARIABLE);
        this.booleanAuxVariables.add(var);
        return var;
    }

    public Variable intVariableInstance(final IntegerVariable group, final int index, final EncodingResult result) {
        final Map<Integer, Variable> intMap = this.variableMap.computeIfAbsent(group, k -> new TreeMap<>());
        return intMap.computeIfAbsent(index, i -> result.newVariable(CSP_AUX_LNG_VARIABLE));
    }

    public Map<IntegerVariable, Map<Integer, Variable>> getVariableMap() {
        return this.variableMap;
    }

    @Override
    public Set<Variable> getSatVariables(final Collection<IntegerVariable> variables) {
        return variables.stream().map(variableMap::get).filter(Objects::nonNull).flatMap(m -> m.values().stream()).collect(Collectors.toSet());
    }

    public boolean isEncoded(final IntegerVariable v) {
        return variableMap.containsKey(v);
    }

    public Set<IntegerVariable> getIntegerVariables() {
        return this.variableMap.keySet();
    }

    public Set<Variable> getBooleanAuxVariables() {
        return this.booleanAuxVariables;
    }

    public Set<IntegerVariable> getIntegerAuxVariables() {
        return this.integerAuxVariables;
    }
}
