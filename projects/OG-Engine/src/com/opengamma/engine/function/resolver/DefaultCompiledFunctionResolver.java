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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.MapMaker;
import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Default implementation of the compiled function resolver.
 * <p>
 * The aim of the resolution is to find functions that are capable of satisfying a requirement.
 * In addition, a priority mechanism is used to return functions in priority order
 * from highest to lowest.
 * <p>
 * This class is not thread-safe. It is possible to call {@link #resolveFunction} concurrently
 * from multiple threads, the rule manipulation methods require external locking.
 */
public class DefaultCompiledFunctionResolver implements CompiledFunctionResolver {

  private static final Logger s_logger = LoggerFactory.getLogger(DefaultCompiledFunctionResolver.class);

  /**
   * The rules by target type, where the inner map is sorted high to low.
   */
  private final Map<ComputationTargetType, SortedMap<Integer, Collection<ResolutionRule>>> _type2Priority2Rules = Maps.newHashMap();
  /**
   * The compilation context.
   */
  private final FunctionCompilationContext _functionCompilationContext;
  /**
   * Cache of targets.
   */
  private final ConcurrentMap<ComputationTarget, List<Pair<ResolutionRule, Set<ValueSpecification>>>> _targetCache = new MapMaker().weakKeys().makeMap();

  /**
   * Creates a resolver.
   * 
   * @param functionCompilationContext  the context, not null
   */
  public DefaultCompiledFunctionResolver(final FunctionCompilationContext functionCompilationContext) {
    this(functionCompilationContext, Collections.<ResolutionRule>emptyList());
  }

  /**
   * Creates a resolver.
   * 
   * @param functionCompilationContext  the context, not null
   * @param resolutionRules  the resolution rules, not null
   */
  public DefaultCompiledFunctionResolver(final FunctionCompilationContext functionCompilationContext, Collection<ResolutionRule> resolutionRules) {
    ArgumentChecker.notNull(functionCompilationContext, "functionCompilationContext");
    _functionCompilationContext = functionCompilationContext;
    ArgumentChecker.notNull(resolutionRules, "resolutionRules");
    addRules(resolutionRules);
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a single rule to the resolver.
   * 
   * @param resolutionRule  the rule to add, not null
   */
  public void addRule(ResolutionRule resolutionRule) {
    addRules(Collections.singleton(resolutionRule));
  }

  /**
   * Adds rules to the resolver.
   * 
   * @param resolutionRules  the rules to add, no nulls, not null
   */
  public void addRules(Iterable<ResolutionRule> resolutionRules) {
    for (ResolutionRule resolutionRule : resolutionRules) {
      final ComputationTargetType type = resolutionRule.getFunction().getFunction().getTargetType();
      SortedMap<Integer, Collection<ResolutionRule>> priority2Rules = _type2Priority2Rules.get(type);
      if (priority2Rules == null) {
        priority2Rules = new TreeMap<Integer, Collection<ResolutionRule>>(Collections.reverseOrder());
        _type2Priority2Rules.put(type, priority2Rules);
      }
      Collection<ResolutionRule> storedRules = priority2Rules.get(resolutionRule.getPriority());
      if (storedRules == null) {
        storedRules = new ArrayList<ResolutionRule>();
        priority2Rules.put(resolutionRule.getPriority(), storedRules);
      }
      storedRules.add(resolutionRule);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public Collection<ResolutionRule> getAllResolutionRules() {
    final ArrayList<ResolutionRule> rules = new ArrayList<ResolutionRule>();
    for (Map<Integer, Collection<ResolutionRule>> priority2Rules : _type2Priority2Rules.values()) {
      for (Collection<ResolutionRule> priorityRules : priority2Rules.values()) {
        rules.addAll(priorityRules);
      }
    }
    return rules;
  }

  /**
   * Gets the function compilation context.
   * 
   * @return the context, not null
   */
  protected FunctionCompilationContext getFunctionCompilationContext() {
    return _functionCompilationContext;
  }

  /**
   * Comparator to give a fixed ordering of functions at the same priority so that we at
   * least have deterministic behavior between runs.
   */
  private static final Comparator<Pair<ResolutionRule, Set<ValueSpecification>>> RULE_COMPARATOR = new Comparator<Pair<ResolutionRule, Set<ValueSpecification>>>() {
    @Override
    public int compare(Pair<ResolutionRule, Set<ValueSpecification>> o1, Pair<ResolutionRule, Set<ValueSpecification>> o2) {
      final int c = o1.getFirst().getFunction().getFunction().getFunctionDefinition().getUniqueId().compareTo(o2.getFirst().getFunction().getFunction().getFunctionDefinition().getUniqueId());
      if (c != 0) {
        return c;
      }
      // Have the same function, can't prioritize the "FunctionInputs"
      throw new OpenGammaRuntimeException("Rule priority conflict - cannot order " + o1 + " against " + o2);
    }
  };

  @SuppressWarnings("unchecked")
  @Override
  public Iterator<Pair<ParameterizedFunction, ValueSpecification>> resolveFunction(final ValueRequirement requirement, final ComputationTarget target) {
    List<Pair<ResolutionRule, Set<ValueSpecification>>> cached = _targetCache.get(target);
    if (cached == null) {
      final LinkedList<Pair<ResolutionRule, Set<ValueSpecification>>> applicableRules = new LinkedList<Pair<ResolutionRule, Set<ValueSpecification>>>();
      final SortedMap<Integer, Collection<ResolutionRule>> priority2Rules = _type2Priority2Rules.get(target.getType());
      if (priority2Rules != null) {
        for (Collection<ResolutionRule> rules : priority2Rules.values()) {
          int rulesFound = 0;
          for (ResolutionRule rule : rules) {
            final Set<ValueSpecification> results = rule.getResults(target, getFunctionCompilationContext());
            if ((results != null) && !results.isEmpty()) {
              applicableRules.add(Pair.of(rule, results));
              rulesFound++;
            }
          }
          if (rulesFound > 1) {
            // sort only the sub-list of rules associated with the priority
            final Iterator<Pair<ResolutionRule, Set<ValueSpecification>>> iterator = applicableRules.descendingIterator();
            final Pair<ResolutionRule, Set<ValueSpecification>>[] found = new Pair[rulesFound];
            for (int i = 0; i < rulesFound; i++) {
              found[i] = iterator.next();
              iterator.remove();
            }
            // TODO [ENG-260] re-order the last "rulesFound" rules in the list with a cost-based heuristic (cheapest first)
            // TODO [ENG-260] throw an exception if there are two rules which can't be re-ordered
            // REVIEW 2010-10-27 Andrew -- Could the above be done with a Comparator<Pair<ParameterizedFunction, ValueSpecification>>
            // provided in the compilation context? This could do away with the need for our "priority" levels as that can do ALL ordering.
            // We should wrap it at construction in something that will detect the equality case and trigger an exception.
            Arrays.sort(found, RULE_COMPARATOR);
            for (int i = 0; i < rulesFound; i++) {
              applicableRules.add(found[i]);
            }
          }
        }
      } else {
        s_logger.warn("No rules for target type {}", target);
      }
      final List<Pair<ResolutionRule, Set<ValueSpecification>>> existing = _targetCache.putIfAbsent(target, applicableRules);
      cached = (existing != null) ? existing : applicableRules;
    }
    return new It(target, requirement, cached);
  }

  //-------------------------------------------------------------------------
  /**
   * Iterator of functions and specifications from a dependency node.
   */
  private static final class It implements Iterator<Pair<ParameterizedFunction, ValueSpecification>> {
    private final ComputationTarget _target;
    private final ValueRequirement _requirement;
    private final Iterator<Pair<ResolutionRule, Set<ValueSpecification>>> _values;
    private Pair<ParameterizedFunction, ValueSpecification> _next;

    private It(final ComputationTarget target, final ValueRequirement requirement, final List<Pair<ResolutionRule, Set<ValueSpecification>>> values) {
      _target = target;
      _requirement = requirement;
      _values = values.iterator();
    }

    private void takeNext() {
      if (_next != null) {
        return;
      }
      while (_values.hasNext()) {
        final Pair<ResolutionRule, Set<ValueSpecification>> value = _values.next();
        final ValueSpecification result = value.getKey().getResult(_requirement, _target, value.getValue());
        if (result != null) {
          _next = Pair.of(value.getKey().getFunction(), result);
          return;
        }
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
  }

}
