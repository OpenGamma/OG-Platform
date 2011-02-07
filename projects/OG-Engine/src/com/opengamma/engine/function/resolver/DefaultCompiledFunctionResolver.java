/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentMap;

import com.google.common.collect.MapMaker;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.UnsatisfiableDependencyGraphException;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.CompiledFunctionRepository;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class DefaultCompiledFunctionResolver implements CompiledFunctionResolver {

  private static final Comparator<Integer> s_priorityComparator = new Comparator<Integer>() {
    @Override
    public int compare(Integer o1, Integer o2) {
      return -o1.compareTo(o2);
    }
  };

  /**
   * The map is sorted from highest to lowest priority
   */
  private Map<ComputationTargetType, Map<Integer, Collection<ResolutionRule>>> _type2Priority2Rules = new HashMap<ComputationTargetType, Map<Integer, Collection<ResolutionRule>>>();

  private final FunctionCompilationContext _functionCompilationContext;

  public DefaultCompiledFunctionResolver(final FunctionCompilationContext functionCompilationContext) {
    ArgumentChecker.notNull(functionCompilationContext, "functionCompilationContext");
    _functionCompilationContext = functionCompilationContext;
  }

  public DefaultCompiledFunctionResolver(final CompiledFunctionRepository repository, final DefaultFunctionResolver.FunctionPriority prioritizer) {
    this(repository.getCompilationContext());
    Collection<ResolutionRule> resolutionRules = new ArrayList<ResolutionRule>();
    for (CompiledFunctionDefinition function : repository.getAllFunctions()) {
      ResolutionRule rule = new ResolutionRule(new ParameterizedFunction(function, function.getFunctionDefinition().getDefaultParameters()), new ApplyToAllTargets(),
          (prioritizer != null) ? prioritizer.getPriority(function) : 0);
      resolutionRules.add(rule);
    }
    addRules(resolutionRules);
  }

  public DefaultCompiledFunctionResolver(final FunctionCompilationContext functionCompilationContext, Collection<ResolutionRule> resolutionRules) {
    this(functionCompilationContext);
    ArgumentChecker.notNull(resolutionRules, "resolutionRules");
    addRules(resolutionRules);
  }

  public void addRule(ResolutionRule rule) {
    addRules(Collections.singleton(rule));
  }

  public void addRules(Collection<ResolutionRule> resolutionRules) {
    for (ResolutionRule resolutionRule : resolutionRules) {
      final ComputationTargetType type = resolutionRule.getFunction().getFunction().getTargetType();
      Map<Integer, Collection<ResolutionRule>> priority2Rules = _type2Priority2Rules.get(type);
      if (priority2Rules == null) {
        priority2Rules = new TreeMap<Integer, Collection<ResolutionRule>>(s_priorityComparator);
        _type2Priority2Rules.put(type, priority2Rules);
      }
      Collection<ResolutionRule> rules = priority2Rules.get(resolutionRule.getPriority());
      if (rules == null) {
        rules = new ArrayList<ResolutionRule>();
        priority2Rules.put(resolutionRule.getPriority(), rules);
      }
      rules.add(resolutionRule);
    }
  }

  protected FunctionCompilationContext getFunctionCompilationContext() {
    return _functionCompilationContext;
  }

  /**
   * Comparator to give a fixed ordering of value specifications for use by the rule comparator. This is not a robust implementation, working
   * only within the confines of the rule comparator's limited use.
   */
  private static final Comparator<ValueSpecification> s_valueSpecificationComparator = new Comparator<ValueSpecification>() {

    @Override
    public int compare(ValueSpecification o1, ValueSpecification o2) {
      int c = o1.getValueName().compareTo(o2.getValueName());
      if (c != 0) {
        return c;
      }
      c = o1.getProperties().compareTo(o2.getProperties());
      if (c != 0) {
        return c;
      }
      return 0;
    }

  };

  /**
   * Comparator to give a fixed ordering of functions at the same priority so that we at least have deterministic behavior between runs.
   */
  private static final Comparator<Pair<ResolutionRule, Set<ValueSpecification>>> s_ruleComparator = new Comparator<Pair<ResolutionRule, Set<ValueSpecification>>>() {

    @Override
    public int compare(Pair<ResolutionRule, Set<ValueSpecification>> o1, Pair<ResolutionRule, Set<ValueSpecification>> o2) {
      final Set<ValueSpecification> s1 = o1.getSecond();
      final Set<ValueSpecification> s2 = o2.getSecond();
      if (s1.size() < s2.size()) {
        return -1;
      } else if (s1.size() > s2.size()) {
        return 1;
      }
      final List<ValueSpecification> s1list = new ArrayList<ValueSpecification>(s1);
      final List<ValueSpecification> s2list = new ArrayList<ValueSpecification>(s2);
      Collections.sort(s1list, s_valueSpecificationComparator);
      Collections.sort(s2list, s_valueSpecificationComparator);
      for (int i = 0; i < s1list.size(); i++) {
        final int c = s_valueSpecificationComparator.compare(s1list.get(i), s2list.get(i));
        if (c != 0) {
          return c;
        }
      }
      throw new UnsatisfiableDependencyGraphException("Rule priority conflict - cannot order " + o1 + " against " + o2);
    }

  };

  private final ConcurrentMap<ComputationTarget, List<Pair<ResolutionRule, Set<ValueSpecification>>>> _targetCache = new MapMaker().weakKeys().makeMap();

  @SuppressWarnings("unchecked")
  @Override
  public Iterator<Pair<ParameterizedFunction, ValueSpecification>> resolveFunction(final ValueRequirement requirement, final DependencyNode atNode) {
    List<Pair<ResolutionRule, Set<ValueSpecification>>> cached = _targetCache.get(atNode.getComputationTarget());
    if (cached == null) {
      final LinkedList<Pair<ResolutionRule, Set<ValueSpecification>>> applicableRules = new LinkedList<Pair<ResolutionRule, Set<ValueSpecification>>>();
      for (Collection<ResolutionRule> rules : _type2Priority2Rules.get(atNode.getComputationTarget().getType()).values()) {
        int rulesFound = 0;
        for (ResolutionRule rule : rules) {
          final Set<ValueSpecification> results = rule.getResults(atNode.getComputationTarget(), getFunctionCompilationContext());
          if (results != null) {
            applicableRules.add(Pair.of(rule, results));
            rulesFound++;
          }
        }
        if (rulesFound > 1) {
          final Iterator<Pair<ResolutionRule, Set<ValueSpecification>>> iterator = applicableRules.descendingIterator();
          final Pair<ResolutionRule, Set<ValueSpecification>>[] found = new Pair[rulesFound];
          for (int i = 0; i < rulesFound; i++) {
            found[i] = iterator.next();
            iterator.remove();
          }
          // TODO [ENG-260] re-order the last "rulesFound" rules in the list with a cost-based heuristic (cheapest first)
          // TODO [ENG-260] throw an exception if there are two rules which can't be re-ordered
          // REVIEW 2010-10-27 Andrew -- Could the above be done with a Comparator<Pair<ParameterizedFunction, ValueSpecification>> provided in the compilation
          // context? This could do away with the need for our "priority" levels as that can do ALL ordering. We should wrap it at construction in something
          // that will detect the equality case and trigger an exception.
          Arrays.<Pair<ResolutionRule, Set<ValueSpecification>>> sort(found, s_ruleComparator);
          for (int i = 0; i < rulesFound; i++) {
            applicableRules.add(found[i]);
          }
        }
      }
      cached = _targetCache.putIfAbsent(atNode.getComputationTarget(), applicableRules);
      if (cached == null) {
        cached = applicableRules;
      }
    }
    final Iterator<Pair<ResolutionRule, Set<ValueSpecification>>> values = cached.iterator();
    return new Iterator<Pair<ParameterizedFunction, ValueSpecification>>() {

      private Pair<ParameterizedFunction, ValueSpecification> _next;
      private boolean _satisfied;

      private void takeNext() {
        if (_next != null) {
          return;
        }
        while (values.hasNext()) {
          final Pair<ResolutionRule, Set<ValueSpecification>> value = values.next();
          final ValueSpecification result = value.getKey().getResult(requirement, atNode, getFunctionCompilationContext(), value.getValue());
          if (result != null) {
            _next = Pair.of(value.getKey().getFunction(), result);
            _satisfied = true;
            return;
          }
        }
        if (!_satisfied) {
          throw new UnsatisfiableDependencyGraphException("There is no rule that can satisfy requirement " + requirement + " for target " + atNode.getComputationTarget());
        }
      }

      @Override
      public boolean hasNext() {
        if (_next == null) {
          takeNext();
        }
        return _next != null;
      }

      @Override
      public Pair<ParameterizedFunction, ValueSpecification> next() {
        if (_next == null) {
          takeNext();
        }
        final Pair<ParameterizedFunction, ValueSpecification> result = _next;
        _next = null;
        return result;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }

    };
  }
}
