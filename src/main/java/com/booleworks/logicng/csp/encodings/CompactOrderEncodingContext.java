package com.booleworks.logicng.csp.encodings;

import com.booleworks.logicng.csp.CspFactory;
import com.booleworks.logicng.csp.IntegerDomain;
import com.booleworks.logicng.csp.terms.IntegerVariable;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.formulas.Variable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class CompactOrderEncodingContext implements CspEncodingContext {
    public static final int DEFAULT_BASE = 10;
    private final OrderEncodingContext orderContext;
    private final int base;
    private final Map<IntegerVariable, List<IntegerVariable>> digits;
    private final Map<IntegerVariable, Integer> offsets;
    private final Map<IntegerVariable, IntegerVariable> reverseSubstitutions;
    private final Map<IntegerVariable, IntegerVariable> substitutions;

    public CompactOrderEncodingContext(final int base) {
        this.base = base;
        this.orderContext = CspEncodingContext.order();
        this.digits = new TreeMap<>();
        this.offsets = new TreeMap<>();
        this.reverseSubstitutions = new TreeMap<>();
        this.substitutions = new TreeMap<>();
    }

    public CompactOrderEncodingContext() {
        this(DEFAULT_BASE);
    }

    public CompactOrderEncodingContext(final CompactOrderEncodingContext context) {
        this.base = context.base;
        this.orderContext = new OrderEncodingContext(context.orderContext);
        this.digits = new TreeMap<>(context.digits);
        this.offsets = new TreeMap<>(context.offsets);
        this.reverseSubstitutions = new TreeMap<>(context.reverseSubstitutions);
        this.substitutions = new TreeMap<>(context.substitutions);
    }

    public int getBase() {
        return base;
    }

    public Map<IntegerVariable, List<IntegerVariable>> getDigits() {
        return digits;
    }

    public Map<IntegerVariable, Integer> getOffsets() {
        return offsets;
    }

    public OrderEncodingContext getOrderContext() {
        return orderContext;
    }

    public void addSubstitution(final IntegerVariable original, final IntegerVariable substitute) {
        substitutions.put(original, substitute);
        reverseSubstitutions.put(substitute, original);
    }

    public IntegerVariable getSubstitution(final IntegerVariable original) {
        return substitutions.get(original);
    }

    public IntegerVariable getSubstitutionOrSelf(final IntegerVariable original) {
        return substitutions.getOrDefault(original, original);
    }

    public IntegerVariable getSubstitutionAllOrSelf(final IntegerVariable original) {
        IntegerVariable current = original;
        while (substitutions.containsKey(current)) {
            current = substitutions.get(current);
        }
        return current;
    }

    public Map<IntegerVariable, IntegerVariable> getSubstitutions() {
        return Collections.unmodifiableMap(substitutions);
    }

    public IntegerVariable reverseSubstitution(final IntegerVariable substitute) {
        return reverseSubstitutions.get(substitute);
    }

    public IntegerVariable reverseSubstitutionOrSelf(final IntegerVariable substitute) {
        return reverseSubstitutions.getOrDefault(substitute, substitute);
    }

    public IntegerVariable reverseSubstitutionAllOrSelf(final IntegerVariable substitute) {
        IntegerVariable current = substitute;
        while (reverseSubstitutions.containsKey(current)) {
            current = reverseSubstitutions.get(current);
        }
        return current;
    }

    public Map<IntegerVariable, IntegerVariable> getReverseSubstitutions() {
        return Collections.unmodifiableMap(reverseSubstitutions);
    }

    public IntegerVariable newAuxIntVariable(final String prefix, final IntegerDomain domain, final CspFactory cf) {
        return orderContext.newAuxIntVariable(prefix, domain, cf);
    }

    public Variable newAuxBoolVariable(final FormulaFactory f) {
        return orderContext.newAuxBoolVariable(f);
    }

    public Set<Variable> getBooleanAuxVariables() {
        return orderContext.getBooleanAuxVariables();
    }

    public Set<IntegerVariable> getIntegerAuxVariables() {
        return orderContext.getIntegerAuxVariables();
    }

    @Override
    public Set<Variable> getSatVariables(final Collection<IntegerVariable> variables) {
        return orderContext.getSatVariables(variables); //FIXME: Substitution?
    }

    @Override
    public boolean isEncoded(final IntegerVariable v) {
        return orderContext.isEncoded(v);
    }

    @Override
    public CspEncodingAlgorithm getAlgorithm() {
        return CspEncodingAlgorithm.CompactOrder;
    }
}
