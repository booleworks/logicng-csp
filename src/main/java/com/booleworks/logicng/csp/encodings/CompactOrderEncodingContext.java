package com.booleworks.logicng.csp.encodings;

import com.booleworks.logicng.csp.CspFactory;
import com.booleworks.logicng.csp.IntegerDomain;
import com.booleworks.logicng.csp.datastructures.IntegerVariableSubstitution;
import com.booleworks.logicng.csp.terms.IntegerConstant;
import com.booleworks.logicng.csp.terms.IntegerVariable;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.formulas.Variable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CompactOrderEncodingContext implements CspEncodingContext {
    public static final int DEFAULT_BASE = 10;
    private final OrderEncodingContext orderContext;
    private final int base;
    private final Map<IntegerVariable, List<IntegerVariable>> digits;
    private final Map<IntegerConstant, List<Integer>> constDigits;
    private final List<IntegerVariable> auxiliaryDigitVariables;
    private final Map<IntegerVariable, Integer> offsets;
    private final IntegerVariableSubstitution adjustedVariablesSubstitution;
    private final List<IntegerVariable> adjustedVariables;
    private final List<Variable> adjustedBoolVariables;
    private final List<IntegerVariable> ternarySimplificationVariables;
    private final List<IntegerVariable> rcspVariables;
    private final List<IntegerVariable> ccspVariables;
    private final List<Variable> ccspBoolVariables;

    public CompactOrderEncodingContext(final int base) {
        this.base = base;
        this.orderContext = CspEncodingContext.order();
        this.digits = new HashMap<>();
        this.constDigits = new HashMap<>();
        this.auxiliaryDigitVariables = new ArrayList<>();
        this.offsets = new HashMap<>();
        this.adjustedVariablesSubstitution = new IntegerVariableSubstitution();
        this.adjustedVariables = new ArrayList<>();
        this.adjustedBoolVariables = new ArrayList<>();
        this.ternarySimplificationVariables = new ArrayList<>();
        this.rcspVariables = new ArrayList<>();
        this.ccspVariables = new ArrayList<>();
        this.ccspBoolVariables = new ArrayList<>();
    }

    public CompactOrderEncodingContext() {
        this(DEFAULT_BASE);
    }

    public CompactOrderEncodingContext(final CompactOrderEncodingContext context) {
        this.base = context.base;
        this.orderContext = new OrderEncodingContext(context.orderContext);
        this.digits = new HashMap<>(context.digits);
        this.constDigits = new HashMap<>(context.constDigits);
        this.auxiliaryDigitVariables = new ArrayList<>(context.auxiliaryDigitVariables);
        this.offsets = new HashMap<>(context.offsets);
        this.adjustedVariablesSubstitution = new IntegerVariableSubstitution(context.adjustedVariablesSubstitution);
        this.adjustedVariables = new ArrayList<>(context.adjustedVariables);
        this.adjustedBoolVariables = new ArrayList<>(context.adjustedBoolVariables);
        this.ternarySimplificationVariables = new ArrayList<>(context.ternarySimplificationVariables);
        this.rcspVariables = new ArrayList<>(context.rcspVariables);
        this.ccspVariables = new ArrayList<>(context.ccspVariables);
        this.ccspBoolVariables = new ArrayList<>(context.ccspBoolVariables);
    }

    public int getBase() {
        return base;
    }

    public void addDigits(final IntegerVariable v, final List<IntegerVariable> digits) {
        this.digits.put(v, digits);
    }

    IntegerVariable newAuxiliaryDigitVariable(final IntegerDomain d, final CspFactory cf) {
        final IntegerVariable v = cf.auxVariable(CompactCSPReduction.AUX_DIGIT, d);
        auxiliaryDigitVariables.add(v);
        return v;
    }

    public List<IntegerVariable> getAuxiliaryDigitVariables() {
        return auxiliaryDigitVariables;
    }

    public boolean hasDigits(final IntegerVariable v) {
        return this.digits.containsKey(v);
    }

    public List<IntegerVariable> getDigits(final IntegerVariable v) {
        return digits.get(v);
    }

    void addConstDigits(final IntegerConstant c, final List<Integer> digits) {
        this.constDigits.put(c, digits);
    }

    public boolean hasConstDigits(final IntegerConstant c) {
        return constDigits.containsKey(c);
    }

    public List<Integer> getConstDigits(final IntegerConstant c) {
        return constDigits.get(c);
    }

    public boolean hasOffset(final IntegerVariable v) {
        return offsets.containsKey(v);
    }

    public int getOffset(final IntegerVariable v) {
        return offsets.get(v);
    }

    public void addOffset(final IntegerVariable v, final int offset) {
        offsets.put(v, offset);
    }

    public OrderEncodingContext getOrderContext() {
        return orderContext;
    }

    IntegerVariable newAdjustedVariable(final String prefix, final IntegerDomain d, final CspFactory cf) {
        final IntegerVariable v = cf.auxVariable(prefix, d);
        adjustedVariables.add(v);
        return v;
    }

    public List<IntegerVariable> getAdjustedVariables() {
        return adjustedVariables;
    }

    void addAdjustedVariable(final IntegerVariable original, final IntegerVariable substitute) {
        adjustedVariablesSubstitution.add(original, substitute);
    }

    public IntegerVariable getAdjustedVariable(final IntegerVariable original) {
        return adjustedVariablesSubstitution.get(original);
    }

    public IntegerVariable getAdjustedVariableOrSelf(final IntegerVariable original) {
        return adjustedVariablesSubstitution.getOrSelf(original);
    }

    public List<IntegerVariable> mapToAdjustedVariableOrSelf(final Collection<IntegerVariable> originals) {
        return originals.stream().map(adjustedVariablesSubstitution::getOrSelf).collect(Collectors.toList());
    }

    public IntegerVariableSubstitution getAdjustedVariablesSubstitution() {
        return adjustedVariablesSubstitution;
    }

    Variable newAdjustedBoolVariable(final FormulaFactory f) {
        final Variable var = f.newAuxVariable(CSP_AUX_LNG_VARIABLE);
        adjustedBoolVariables.add(var);
        return var;
    }

    public List<Variable> getAdjustedBoolVariables() {
        return adjustedBoolVariables;
    }

    IntegerVariable newTernarySimplificationVariable(final IntegerDomain d, final CspFactory cf) {
        final IntegerVariable v = cf.auxVariable(CompactOrderReduction.AUX_TERNARY, d);
        ternarySimplificationVariables.add(v);
        return v;
    }

    public List<IntegerVariable> getTernarySimplificationVariables() {
        return ternarySimplificationVariables;
    }

    IntegerVariable newRCSPVariable(final IntegerDomain d, final CspFactory cf) {
        final IntegerVariable v = cf.auxVariable(CompactOrderReduction.AUX_RCSP, d);
        rcspVariables.add(v);
        return v;
    }

    public List<IntegerVariable> getRCSPVariables() {
        return rcspVariables;
    }

    IntegerVariable newCCSPVariable(final IntegerDomain d, final CspFactory cf) {
        final IntegerVariable v = cf.auxVariable(CompactCSPReduction.AUX_CCSP, d);
        ccspVariables.add(v);
        return v;
    }

    public List<IntegerVariable> getCCSPVariables() {
        return ccspVariables;
    }

    Variable newCCSPBoolVariable(final FormulaFactory f) {
        final Variable v = f.newAuxVariable(CSP_AUX_LNG_VARIABLE);
        ccspBoolVariables.add(v);
        return v;
    }

    public List<Variable> getCCSPBoolVariables() {
        return ccspBoolVariables;
    }

    @Override
    public Set<Variable> getSatVariables(final Collection<IntegerVariable> variables) {
        final Collection<IntegerVariable> subs = adjustedVariablesSubstitution
                .getAllOrSelf(variables).stream()
                .flatMap(v -> {
                    if (hasDigits(v)) {return getDigits(v).stream();} else {return Stream.empty();}
                })
                .collect(Collectors.toList());
        return orderContext.getSatVariables(subs);
    }

    @Override
    public boolean isEncoded(final IntegerVariable v) {
        final List<IntegerVariable> digits = getDigits(adjustedVariablesSubstitution.getOrSelf(v));
        if (digits == null) {
            return false;
        } else {
            return digits.stream().anyMatch(orderContext::isEncoded);
        }
    }

    @Override
    public CspEncodingAlgorithm getAlgorithm() {
        return CspEncodingAlgorithm.CompactOrder;
    }
}
