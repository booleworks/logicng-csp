package com.booleworks.logicng.csp.datastructures;

import com.booleworks.logicng.csp.terms.IntegerVariable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class IntegerVariableSubstitution {
    private final Map<IntegerVariable, IntegerVariable> mainDirection;
    // private final Map<IntegerVariable, IntegerVariable> reverseDirection;

    public IntegerVariableSubstitution() {
        mainDirection = new HashMap<>();
        // reverseDirection = new HashMap<>();
    }

    public IntegerVariableSubstitution(final IntegerVariableSubstitution other) {
        mainDirection = new HashMap<>(other.mainDirection);
        // reverseDirection = new HashMap<>(other.reverseDirection);
    }

    public int size() {
        return mainDirection.size();
    }

    public boolean isEmpty() {
        return mainDirection.isEmpty();
    }

    public boolean containsKey(final IntegerVariable v) {
        return mainDirection.containsKey(v);
    }

    public void add(final IntegerVariable original, final IntegerVariable substitute) {
        mainDirection.put(original, substitute);
        // reverseDirection.put(substitute, original);
    }

    public IntegerVariable get(final IntegerVariable original) {
        return mainDirection.get(original);
    }

    public IntegerVariable getOrSelf(final IntegerVariable original) {
        return mainDirection.getOrDefault(original, original);
    }

    //public void getOriginal(final IntegerVariable substitute) {
    //    reverseDirection.get(substitute);
    //}

    //public void getOriginalOrSelf(final IntegerVariable substitute) {
    //    reverseDirection.getOrDefault(substitute, substitute);
    //}

    public List<IntegerVariable> getAllOrSelf(final Collection<IntegerVariable> originals) {
        return originals.stream().map(v -> mainDirection.getOrDefault(v, v)).collect(Collectors.toList());
    }

    //public List<IntegerVariable> getAllOriginalOrSelf(final Collection<IntegerVariable> substitutes) {
    //    return substitutes.stream().map(v -> reverseDirection.getOrDefault(v, v)).collect(Collectors.toList());
    //}

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IntegerVariableSubstitution)) {
            return false;
        }

        final IntegerVariableSubstitution that = (IntegerVariableSubstitution) o;
        return mainDirection.equals(that.mainDirection); //&& reverseDirection.equals(that.reverseDirection);
    }

    @Override
    public int hashCode() {
        final int result = mainDirection.hashCode();
        // result = 31 * result + reverseDirection.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "IntegerVariableSubstitution{" + mainDirection + '}';
    }
}
