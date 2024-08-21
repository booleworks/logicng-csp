package com.booleworks.logicng.csp.encodings;

import com.booleworks.logicng.csp.IntegerDomain;
import com.booleworks.logicng.csp.terms.IntegerVariable;
import com.booleworks.logicng.datastructures.EncodingResult;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.formulas.Literal;
import com.booleworks.logicng.formulas.Variable;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class CspEncodingContext {
    public final static String CSP_AUX_LNG_VARIABLE = "CSP";
    private final Map<IntegerVariable, Map<Integer, Variable>> variableMap;
    private final Set<Variable> booleanAuxVariables;
    private final Set<IntegerVariable> integerAuxVariables;
    private final CspEncodingAlgorithm algorithm;
    private int booleanVariables = 0;
    private int integerVariables = 0;

    public CspEncodingContext() {
        this(CspEncodingAlgorithm.Order);
    }

    public CspEncodingContext(final CspEncodingAlgorithm algorithm) {
        this.variableMap = new TreeMap<>();
        this.booleanAuxVariables = new TreeSet<>();
        this.integerAuxVariables = new TreeSet<>();
        this.algorithm = algorithm;
    }

    public CspEncodingContext(final CspEncodingContext context) {
        this.variableMap = new TreeMap<>(context.variableMap);
        this.booleanAuxVariables = new TreeSet<>(context.booleanAuxVariables);
        this.integerAuxVariables = new TreeSet<>(context.integerAuxVariables);
        this.booleanVariables = context.booleanVariables;
        this.integerVariables = context.integerVariables;
        this.algorithm = context.algorithm;
    }

    public Variable intVariableInstance(final IntegerVariable group, final int index, final EncodingResult result) {
        final Map<Integer, Variable> intMap = this.variableMap.computeIfAbsent(group, k -> new TreeMap<>());
        return intMap.computeIfAbsent(index, i -> result.newVariable(CSP_AUX_LNG_VARIABLE));
    }

    public Map<IntegerVariable, Map<Integer, Variable>> getVariableMap() {
        return this.variableMap;
    }

    public Set<Variable> getSatVariables(final Collection<IntegerVariable> variables) {
        return variables.stream().map(variableMap::get).filter(Objects::nonNull).flatMap(m -> m.values().stream()).collect(Collectors.toSet());
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

    public CspEncodingAlgorithm getAlgorithm() {
        return this.algorithm;
    }

    IntegerVariable newAuxIntVariable(final String prefix, final IntegerDomain domain) {
        final IntegerVariable var = IntegerVariable.auxVar(prefix + (++this.integerVariables), domain);
        this.integerAuxVariables.add(var);
        return var;
    }

    Variable newAuxBoolVariable(final FormulaFactory f) {
        final Variable var = f.newAuxVariable(CSP_AUX_LNG_VARIABLE);
        this.booleanAuxVariables.add(var);
        return var;
    }

    Literal negate(final Variable v) {
        return v.negate(v.factory());
    }
}
