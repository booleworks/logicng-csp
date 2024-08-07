package com.booleworks.logicng.csp;

import com.booleworks.logicng.csp.encodings.CspEncodingContext;
import com.booleworks.logicng.csp.encodings.OrderDecoding;
import com.booleworks.logicng.csp.encodings.OrderEncoding;
import com.booleworks.logicng.csp.encodings.OrderReduction;
import com.booleworks.logicng.csp.predicates.AllDifferentPredicate;
import com.booleworks.logicng.csp.predicates.ComparisonPredicate;
import com.booleworks.logicng.csp.predicates.CspPredicate;
import com.booleworks.logicng.csp.terms.AbsoluteFunction;
import com.booleworks.logicng.csp.terms.AdditionFunction;
import com.booleworks.logicng.csp.terms.DivisionFunction;
import com.booleworks.logicng.csp.terms.IntegerConstant;
import com.booleworks.logicng.csp.terms.IntegerVariable;
import com.booleworks.logicng.csp.terms.MaxFunction;
import com.booleworks.logicng.csp.terms.MinFunction;
import com.booleworks.logicng.csp.terms.ModuloFunction;
import com.booleworks.logicng.csp.terms.MultiplicationFunction;
import com.booleworks.logicng.csp.terms.NegationFunction;
import com.booleworks.logicng.csp.terms.SubtractionFunction;
import com.booleworks.logicng.csp.terms.Term;
import com.booleworks.logicng.datastructures.Assignment;
import com.booleworks.logicng.datastructures.EncodingResult;
import com.booleworks.logicng.formulas.Formula;
import com.booleworks.logicng.formulas.FormulaFactory;
import com.booleworks.logicng.util.Pair;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class CspFactory {
    public static final String AUX_PREFIX = "@AUX_";
    private final IntegerConstant zero;
    private final IntegerConstant one;
    private final FormulaFactory formulaFactory;
    private final Map<Integer, IntegerConstant> integerConstants;
    private final Map<String, IntegerVariable> integerVariables;
    private final Map<Term, NegationFunction> unaryMinusTerms;
    private final Map<LinkedHashSet<Term>, Term> addTerms;
    private final Map<Pair<Term, Term>, SubtractionFunction> subTerms;
    private final Map<Pair<Integer, Term>, MultiplicationFunction> mulTerms;
    private final Map<Term, AbsoluteFunction> absTerms;
    private final Map<LinkedHashSet<Term>, MaxFunction> maxTerms;
    private final Map<LinkedHashSet<Term>, MinFunction> minTerms;
    private final Map<Pair<Term, Integer>, ModuloFunction> modTerms;
    private final Map<Pair<Term, Integer>, DivisionFunction> divTerms;
    private final Map<LinkedHashSet<Term>, ComparisonPredicate> eqPredicates;
    private final Map<LinkedHashSet<Term>, ComparisonPredicate> nePredicates;
    private final Map<Pair<Term, Term>, ComparisonPredicate> lePredicates;
    private final Map<Pair<Term, Term>, ComparisonPredicate> ltPredicates;
    private final Map<Pair<Term, Term>, ComparisonPredicate> gePredicates;
    private final Map<Pair<Term, Term>, ComparisonPredicate> gtPredicates;
    private final Map<LinkedHashSet<Term>, AllDifferentPredicate> allDifferentPredicates;
    private final Map<String, Integer> auxVarCounters;

    public CspFactory(final FormulaFactory formulaFactory) {
        this.formulaFactory = formulaFactory;
        this.integerConstants = new HashMap<>();
        this.integerVariables = new HashMap<>();
        this.unaryMinusTerms = new HashMap<>();
        this.addTerms = new HashMap<>();
        this.subTerms = new HashMap<>();
        this.mulTerms = new HashMap<>();
        this.absTerms = new HashMap<>();
        this.maxTerms = new HashMap<>();
        this.minTerms = new HashMap<>();
        this.modTerms = new HashMap<>();
        this.divTerms = new HashMap<>();
        this.eqPredicates = new HashMap<>();
        this.nePredicates = new HashMap<>();
        this.lePredicates = new HashMap<>();
        this.ltPredicates = new HashMap<>();
        this.gePredicates = new HashMap<>();
        this.gtPredicates = new HashMap<>();
        this.allDifferentPredicates = new HashMap<>();
        this.auxVarCounters = new HashMap<>();
        this.zero = new IntegerConstant(0);
        this.one = new IntegerConstant(1);
        this.integerConstants.put(0, this.zero);
        this.integerConstants.put(1, this.one);
    }

    public CspFactory(final CspFactory other, final FormulaFactory formulaFactory) {
        this.formulaFactory = formulaFactory;
        this.integerConstants = new HashMap<>(other.integerConstants);
        this.integerVariables = new HashMap<>(other.integerVariables);
        this.unaryMinusTerms = new HashMap<>(other.unaryMinusTerms);
        this.addTerms = new HashMap<>(other.addTerms);
        this.subTerms = new HashMap<>(other.subTerms);
        this.mulTerms = new HashMap<>(other.mulTerms);
        this.absTerms = new HashMap<>(other.absTerms);
        this.maxTerms = new HashMap<>(other.maxTerms);
        this.minTerms = new HashMap<>(other.minTerms);
        this.modTerms = new HashMap<>(other.modTerms);
        this.divTerms = new HashMap<>(other.divTerms);
        this.eqPredicates = new HashMap<>(other.eqPredicates);
        this.nePredicates = new HashMap<>(other.nePredicates);
        this.lePredicates = new HashMap<>(other.lePredicates);
        this.ltPredicates = new HashMap<>(other.ltPredicates);
        this.gePredicates = new HashMap<>(other.gePredicates);
        this.gtPredicates = new HashMap<>(other.gtPredicates);
        this.allDifferentPredicates = new HashMap<>(other.allDifferentPredicates);
        this.auxVarCounters = new HashMap<>(other.auxVarCounters);
        this.zero = new IntegerConstant(0);
        this.one = new IntegerConstant(1);
        this.integerConstants.put(0, this.zero);
        this.integerConstants.put(1, this.one);
    }

    public IntegerConstant zero() {
        return zero;
    }

    public IntegerConstant one() {
        return one;
    }

    public IntegerConstant constant(final int value) {
        if (value == 0) {
            return zero();
        } else if (value == 1) {
            return one();
        } else {
            return integerConstants.computeIfAbsent(value, c -> new IntegerConstant(value));
        }
    }

    public IntegerVariable variable(final String name, final int lowerBound, final int upperBound) {
        return variable(name, IntegerDomain.of(lowerBound, upperBound));
    }

    public IntegerVariable variable(final String name, final Collection<Integer> values) {
        return variable(name, IntegerDomain.of(new TreeSet<>(values)));
    }

    public IntegerVariable variable(final String name, final IntegerDomain domain) {
        return variableIntern(name, domain, false);
    }

    private IntegerVariable variableIntern(final String name, final IntegerDomain domain, final boolean aux) {
        if (domain.isEmpty()) {
            throw new IllegalArgumentException("Cannot create a variable with an empty domain");
        }
        final IntegerVariable existingVar = integerVariables.get(name);
        if (existingVar != null) {
            throw new IllegalArgumentException("Variable \"" + name + "\" already exists in this CSP factory");
        }
        final IntegerVariable newVariable = new IntegerVariable(name, domain, aux);
        integerVariables.put(name, newVariable);
        return newVariable;
    }

    public IntegerVariable auxVariable(final String type, final IntegerDomain domain) {
        final int counter = auxVarCounters.compute(type, (key, value) -> value == null ? 0 : value + 1);
        return variableIntern(AUX_PREFIX + type + "_" + counter, domain, true);
    }

    public IntegerVariable auxVariable(final String type, final String postfix, final IntegerDomain domain) {
        final int counter = auxVarCounters.compute(type, (key, value) -> value == null ? 0 : value + 1);
        return variableIntern(AUX_PREFIX + type + "_" + counter + "_" + postfix, domain, true);
    }

    public Term minus(final Term term) {
        // contract double minus --x to x
        if (term instanceof NegationFunction) {
            return ((NegationFunction) term).getOperand();
        }
        if (term instanceof IntegerConstant) {
            return constant(((IntegerConstant) term).getValue() * -1);
        }
        return unaryMinusTerms.computeIfAbsent(term, NegationFunction::new);
    }

    public Term minus(final IntegerConstant constant) {
        return constant(-constant.getValue());
    }

    public Term add(final Term... terms) {
        return add(Arrays.asList(terms));
    }

    public Term add(final Collection<Term> terms) {
        final Collection<Term> originalOperands = terms;
        final Term foundFunction = addTerms.get(originalOperands);
        if (foundFunction != null) {
            return foundFunction;
        }
        final LinkedHashSet<Term> compactedOperands = compactifyAddOperands(originalOperands);
        final Term foundFunctionCompact = this.addTerms.get(compactedOperands);
        if (foundFunctionCompact != null) {
            return foundFunctionCompact;
        }
        if (compactedOperands.size() == 1) {
            final Term term = compactedOperands.iterator().next();
            addTerms.put(compactedOperands, term);
            return term;
        }
        final AdditionFunction addition = new AdditionFunction(compactedOperands);
        addTerms.put(compactedOperands, addition);
        return addition;
    }

    public IntegerConstant add(final IntegerConstant left, final IntegerConstant right) {
        return constant(left.getValue() + right.getValue());
    }

    private LinkedHashSet<Term> compactifyAddOperands(final Collection<Term> originalOperands) {
        final LinkedHashMap<Term, Integer> mergedTerms = new LinkedHashMap<>();
        final LinkedHashSet<Term> compactifiedTerms = new LinkedHashSet<>();
        int constValue = 0;
        for (final Term op : originalOperands) {
            // gather all constant integers including 0
            if (op instanceof IntegerConstant) {
                constValue += ((IntegerConstant) op).getValue();
            }
            // flatten nested additions
            else if (op instanceof AdditionFunction) {
                for (final Term t : ((AdditionFunction) op).getOperands()) {
                    addOperand(t, mergedTerms);
                }
            } else {
                addOperand(op, mergedTerms);
            }
        }
        for (final Map.Entry<Term, Integer> entry : mergedTerms.entrySet()) {
            if (entry.getValue() == 1) {
                compactifiedTerms.add(entry.getKey());
            } else {
                compactifiedTerms.add(mul(entry.getValue(), entry.getKey()));
            }
        }
        if (constValue != 0) {
            compactifiedTerms.add(constant(constValue));
        }
        return compactifiedTerms;
    }

    private void addOperand(final Term op, final LinkedHashMap<Term, Integer> compactifiedTerms) {
        if (op instanceof MultiplicationFunction) {
            compactifiedTerms.compute(((MultiplicationFunction) op).getRight(), (k, v) -> v == null
                    ? ((MultiplicationFunction) op).getLeft().getValue()
                    : ((MultiplicationFunction) op).getLeft().getValue() + v);
        } else {
            compactifiedTerms.compute(op, (k, v) -> v == null ? 1 : v + 1);
        }
    }

    public Term sub(final Term left, final Term right) {
        // x-x = 0
        if (left.equals(right)) {
            return zero;
        }
        // 0 - x = -x
        if (left.getType() == Term.Type.ZERO) {
            return minus(right);
        }
        // x - 0 = x
        if (right.getType() == Term.Type.ZERO) {
            return left;
        }
        // inline - in constants
        if (left instanceof IntegerConstant && right instanceof IntegerConstant) {
            return constant(((IntegerConstant) left).getValue() - ((IntegerConstant) right).getValue());
        }
        return subTerms.computeIfAbsent(new Pair<>(left, right), p -> new SubtractionFunction(left, right));
    }

    public IntegerConstant sub(final IntegerConstant left, final IntegerConstant right) {
        return constant(left.getValue() - right.getValue());
    }

    public Term mul(final int value, final Term term) {
        return mul(constant(value), term);
    }

    public Term mul(final IntegerConstant left, final Term right) {
        // a*0 or 0*a = 0
        if (left.getType() == Term.Type.ZERO || right.getType() == Term.Type.ZERO) {
            return zero;
        }
        // 1*a = a
        if (left.getType() == Term.Type.ONE) {
            return right;
        }
        // a*1 = a
        if (right.getType() == Term.Type.ONE) {
            return left;
        }
        // inline * in constants
        if (right instanceof IntegerConstant) {
            return constant(left.getValue() * ((IntegerConstant) right).getValue());
        }
        return mulTerms.computeIfAbsent(new Pair<>(left.getValue(), right), o -> new MultiplicationFunction(left, right));
    }

    public Term abs(final Term operand) {
        // constant
        if (operand instanceof IntegerConstant) {
            return constant(Math.abs(((IntegerConstant) operand).getValue()));
        }
        return absTerms.computeIfAbsent(operand, o -> new AbsoluteFunction(operand));
    }

    public Term div(final Term left, final int right) {
        return div(left, constant(right));
    }

    public Term div(final Term left, final IntegerConstant right) {
        // no division by 0
        if (right.getValue() == 0) {
            throw new IllegalArgumentException("Cannot divide by zero");
        }
        // Decomposition only supports division by positive integers
        if (right.getValue() < 0) {
            return div(minus(left), constant(-right.getValue()));
        }
        // a/1 = a
        if (right.getValue() == 1) {
            return left;
        }
        // constant
        if (left instanceof IntegerConstant) {
            return constant(((IntegerConstant) left).getValue() / right.getValue());
        }
        return this.divTerms.computeIfAbsent(new Pair<>(left, right.getValue()), p -> new DivisionFunction(left, right));
    }

    public Term mod(final Term left, final int right) {
        return mod(left, constant(right));
    }

    public Term mod(final Term left, final IntegerConstant right) {
        // no division by 0 or by negative integers
        if (right.getValue() == 0) {
            throw new IllegalArgumentException("Cannot modulo by zero");
        }
        if (right.getValue() < 0) {
            throw new IllegalArgumentException("Modulo a negative number is not allowed");
        }
        // x % 1 = 0
        if (right.getValue() == 1) {
            return this.zero;
        }
        // inline % in constants
        if (left instanceof IntegerConstant) {
            return constant(((IntegerConstant) left).getValue() % right.getValue());
        }
        return this.modTerms.computeIfAbsent(new Pair<>(left, right.getValue()), p -> new ModuloFunction(left, right));
    }

    public Term min(final Term left, final Term right) {
        // min(x, x) = x
        if (left.equals(right)) {
            return left;
        }
        // inline % in constants
        if (left instanceof IntegerConstant && right instanceof IntegerConstant) {
            return constant(Math.min(((IntegerConstant) left).getValue(), ((IntegerConstant) right).getValue()));
        }
        final LinkedHashSet<Term> operands = new LinkedHashSet<>(Arrays.asList(left, right));
        return minTerms.computeIfAbsent(operands, p -> new MinFunction(left, right));
    }

    public Term max(final Term left, final Term right) {
        // max(x, x) = x
        if (left.equals(right)) {
            return left;
        }
        // inline % in constants
        if (left instanceof IntegerConstant && right instanceof IntegerConstant) {
            return constant(Math.max(((IntegerConstant) left).getValue(), ((IntegerConstant) right).getValue()));
        }
        final LinkedHashSet<Term> operands = new LinkedHashSet<>(Arrays.asList(left, right));
        return maxTerms.computeIfAbsent(operands, p -> new MaxFunction(left, right));
    }

    public ComparisonPredicate comparison(final Term left, final Term right, final CspPredicate.Type type) {
        switch (type) {
            case EQ:
                return eq(left, right);
            case NE:
                return ne(left, right);
            case LT:
                return lt(left, right);
            case LE:
                return le(left, right);
            case GT:
                return gt(left, right);
            case GE:
                return ge(left, right);
            default:
                throw new IllegalArgumentException("Invalid type for comparison predicates:" + type);
        }
    }

    public ComparisonPredicate eq(final Term left, final Term right) {
        final LinkedHashSet<Term> operands = new LinkedHashSet<>(Arrays.asList(left, right));
        final ComparisonPredicate foundFormula = eqPredicates.get(operands);
        if (foundFormula != null) {
            return foundFormula;
        }
        final ComparisonPredicate predicate = new ComparisonPredicate(CspPredicate.Type.EQ, left, right, formulaFactory);
        eqPredicates.put(operands, predicate);
        return predicate;
    }

    public ComparisonPredicate ne(final Term left, final Term right) {
        final LinkedHashSet<Term> operands = new LinkedHashSet<>(Arrays.asList(left, right));
        final ComparisonPredicate foundFormula = nePredicates.get(operands);
        if (foundFormula != null) {
            return foundFormula;
        }
        final ComparisonPredicate predicate = new ComparisonPredicate(CspPredicate.Type.NE, left, right, formulaFactory);
        nePredicates.put(operands, predicate);
        return predicate;
    }

    public ComparisonPredicate lt(final Term left, final Term right) {
        return processComparison(left, right, ltPredicates, CspPredicate.Type.LT);
    }

    public ComparisonPredicate le(final Term left, final Term right) {
        return processComparison(left, right, lePredicates, CspPredicate.Type.LE);
    }

    public ComparisonPredicate gt(final Term left, final Term right) {
        return processComparison(left, right, gtPredicates, CspPredicate.Type.GT);
    }

    public ComparisonPredicate ge(final Term left, final Term right) {
        return processComparison(left, right, gePredicates, CspPredicate.Type.GE);
    }

    private ComparisonPredicate processComparison(final Term left, final Term right, final Map<Pair<Term, Term>, ComparisonPredicate> cache, final CspPredicate.Type type) {
        final Pair<Term, Term> operands = new Pair<>(left, right);
        final ComparisonPredicate foundFormula = cache.get(operands);
        if (foundFormula != null) {
            return foundFormula;
        }
        final ComparisonPredicate predicate = new ComparisonPredicate(type, left, right, formulaFactory);
        cache.put(operands, predicate);
        return predicate;
    }

    public AllDifferentPredicate allDifferent(final Collection<Term> terms) {
        final LinkedHashSet<Term> operands = new LinkedHashSet<>(terms);
        final AllDifferentPredicate foundFormula = allDifferentPredicates.get(operands);
        if (foundFormula != null) {
            return foundFormula;
        }
        final AllDifferentPredicate predicate = new AllDifferentPredicate(operands, formulaFactory);
        allDifferentPredicates.put(operands, predicate);
        return predicate;
    }

    public FormulaFactory formulaFactory() {
        return formulaFactory;
    }

    public Set<IntegerClause> decompose(final Formula formula) {
        return CspDecomposition.decompose(formula, this);
    }

    public Csp buildCsp(final Collection<CspPredicate> predicates) {
        final Set<IntegerClause> clauses = predicates.stream().flatMap(p -> p.decompose(this).stream()).collect(Collectors.toSet());
        return Csp.fromClauses(clauses);
    }

    public Csp buildCsp(final CspPredicate... predicates) {
        return buildCsp(Arrays.asList(predicates));
    }

    public List<Formula> encodeCsp(final Csp csp, final CspEncodingContext context) {
        final Csp newCsp;
        final EncodingResult result = EncodingResult.resultForFormula(formulaFactory);
        switch (context.getAlgorithm()) {
            case Order:
                newCsp = OrderReduction.reduce(csp, context, formulaFactory);
                OrderEncoding.encode(newCsp, context, result, this);
                return result.result();
            default:
                throw new UnsupportedOperationException("Unsupported csp encoding algorithm: " + context.getAlgorithm());
        }
    }

    public List<Formula> encodeVariable(final IntegerVariable variable, final CspEncodingContext context) {
        final EncodingResult result = EncodingResult.resultForFormula(formulaFactory);
        switch (context.getAlgorithm()) {
            case Order:
                OrderEncoding.encodeVariable(variable, context, result, this);
                return result.result();
            default:
                throw new UnsupportedOperationException("Unsupported csp encoding algorithm: " + context.getAlgorithm());
        }
    }

    public List<Formula> encodeConstraint(final CspPredicate predicate, final CspEncodingContext context) {
        Set<IntegerClause> clauses = predicate.decompose(this);
        final EncodingResult result = EncodingResult.resultForFormula(formulaFactory);
        switch (context.getAlgorithm()) {
            case Order:
                clauses = OrderReduction.reduce(clauses, context, formulaFactory);
                OrderEncoding.encodeClauses(clauses, context, result, this);
                return result.result();
            default:
                throw new UnsupportedOperationException("Unsupported csp encoding algorithm: " + context.getAlgorithm());
        }
    }

    public CspAssignment decode(final Assignment model, final Csp csp, final CspEncodingContext context) {
        return decode(model, csp.getIntegerVariables(), context);
    }

    public CspAssignment decode(final Assignment model, final Collection<IntegerVariable> variables, final CspEncodingContext context) {
        switch (context.getAlgorithm()) {
            case Order:
                return OrderDecoding.decode(model, variables, context, this);
            default:
                throw new UnsupportedOperationException("Unsupported csp encoding algorithm: " + context.getAlgorithm());
        }
    }
}


