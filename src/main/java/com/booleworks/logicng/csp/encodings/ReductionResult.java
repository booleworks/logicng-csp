package com.booleworks.logicng.csp.encodings;

import com.booleworks.logicng.csp.IntegerClause;
import com.booleworks.logicng.csp.terms.IntegerVariable;

import java.util.List;
import java.util.Set;

public class ReductionResult {
    private final Set<IntegerClause> clauses;
    private final List<IntegerVariable> frontierAuxiliaryVariables;

    ReductionResult(final Set<IntegerClause> clauses, final List<IntegerVariable> frontierAuxiliaryVariables) {
        this.clauses = clauses;
        this.frontierAuxiliaryVariables = frontierAuxiliaryVariables;
    }

    public Set<IntegerClause> getClauses() {
        return clauses;
    }

    public List<IntegerVariable> getFrontierAuxiliaryVariables() {
        return frontierAuxiliaryVariables;
    }
}
