package com.booleworks.logicng.csp.functions;

import com.booleworks.logicng.csp.Csp;
import com.booleworks.logicng.csp.CspAssignment;
import com.booleworks.logicng.csp.CspFactory;
import com.booleworks.logicng.csp.encodings.CspEncodingContext;
import com.booleworks.logicng.csp.terms.IntegerVariable;
import com.booleworks.logicng.formulas.Variable;
import com.booleworks.logicng.solvers.SATSolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CspModelEnumeration {
    public static List<CspAssignment> enumerate(final SATSolver solver, final Csp csp, final CspEncodingContext context, final CspFactory cf) {
        return enumerate(solver, csp.getPropagateSubstitutions().getAllOrSelf(csp.getVisibleIntegerVariables()), csp.getVisibleBooleanVariables(), context, cf);
    }

    public static List<CspAssignment> enumerate(final SATSolver solver, final Collection<IntegerVariable> integerVariables, final Collection<Variable> booleanVariables, final CspEncodingContext context, final CspFactory cf) {
        final Set<Variable> allVars = context.getSatVariables(integerVariables);
        allVars.addAll(booleanVariables);
        final List<IntegerVariable> additionalVariables = integerVariables.stream().filter(v -> !context.isEncoded(v)).collect(Collectors.toList());
        final List<CspAssignment> decodedModels = solver.enumerateAllModels(allVars).stream()
                .map(m -> cf.decode(m.assignment(), integerVariables, booleanVariables, context))
                .collect(Collectors.toList());
        if (additionalVariables.isEmpty() || decodedModels.isEmpty()) {
            return decodedModels;
        } else {
            return enumerateAdditionalVariables(decodedModels, additionalVariables);
        }
    }

    private static List<CspAssignment> enumerateAdditionalVariables(final List<CspAssignment> decodedModels, final List<IntegerVariable> integerVariables) {
        final List<Iterator<Integer>> iterators = integerVariables.stream().map(v -> v.getDomain().iterator()).collect(Collectors.toList());
        final List<Integer> values = iterators.stream().map(Iterator::next).collect(Collectors.toList()); //Domains cannot be empty
        final List<CspAssignment> models = new ArrayList<>();
        int leaderIndex = 0;
        for (int index = 0; index < integerVariables.size(); ) {
            for (final CspAssignment model : decodedModels) {
                final CspAssignment newModel = new CspAssignment(model);
                for (int i = 0; i < integerVariables.size(); ++i) {
                    newModel.addIntAssignment(integerVariables.get(i), values.get(i));
                }
                models.add(newModel);
            }
            if (iterators.get(index).hasNext()) {
                values.set(index, iterators.get(index).next());
            } else {
                while (index < integerVariables.size() && !iterators.get(index).hasNext()) {
                    ++index;
                }
                if (index == integerVariables.size()) {
                    break;
                }
                values.set(index, iterators.get(index).next());
                for (int i = 0; i < index; ++i) {
                    iterators.set(i, integerVariables.get(i).getDomain().iterator());
                    values.set(i, iterators.get(i).next());
                }
                if (index > leaderIndex) {
                    leaderIndex = index;
                }
                index = 0;
            }
        }
        return models;
    }
}
