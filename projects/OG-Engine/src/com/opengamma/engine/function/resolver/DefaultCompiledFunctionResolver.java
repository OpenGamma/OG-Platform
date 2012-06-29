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
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.MemoryUtils;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.function.blacklist.FunctionBlacklistQuery;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Triple;

/**
 * Default implementation of the compiled function resolver.
 * <p>
 * The aim of the resolution is to find functions that are capable of satisfying a requirement. In addition, a priority mechanism is used to return functions in priority order from highest to lowest.
 * <p>
 * This class is not thread-safe. It is possible to call {@link #resolveFunction} concurrently from multiple threads, the rule manipulation methods require external locking.
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
   * Cache of targets. The values are weak so that when the function iterators drop out of scope as the requirements on the target are resolved the entry can be dropped.
   */
  private final ConcurrentMap<ComputationTargetSpecification, Pair<ResolutionRule[], Collection<ValueSpecification>[]>> _targetCache = new MapMaker().weakValues().makeMap();

  /**
   * Creates a resolver.
   * 
   * @param functionCompilationContext the context, not null
   */
  public DefaultCompiledFunctionResolver(final FunctionCompilationContext functionCompilationContext) {
    this(functionCompilationContext, Collections.<ResolutionRule>emptyList());
  }

  /**
   * Creates a resolver.
   * 
   * @param functionCompilationContext the context, not null
   * @param resolutionRules the resolution rules, not null
   */
  public DefaultCompiledFunctionResolver(final FunctionCompilationContext functionCompilationContext, Collection<ResolutionRule> resolutionRules) {
    ArgumentChecker.notNull(functionCompilationContext, "functionCompilationContext");
    ArgumentChecker.notNull(resolutionRules, "resolutionRules");
    _functionCompilationContext = functionCompilationContext;
    addRules(resolutionRules);
  }

  /**
   * Adds a single rule to the resolver.
   * 
   * @param resolutionRule the rule to add, not null
   */
  public void addRule(ResolutionRule resolutionRule) {
    addRules(Collections.singleton(resolutionRule));
  }

  /**
   * Adds rules to the resolver.
   * 
   * @param resolutionRules the rules to add, no nulls, not null
   */
  public void addRules(Iterable<ResolutionRule> resolutionRules) {
    for (ResolutionRule resolutionRule : resolutionRules) {
      final ComputationTargetType type = resolutionRule.getParameterizedFunction().getFunction().getTargetType();
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
   * Returns the target resolver. Computation targets are held by specification only internally to reduce the memory overhead.
   * 
   * @return the target resolver, not null
   */
  protected ComputationTargetResolver getTargetResolver() {
    return getFunctionCompilationContext().getComputationTargetResolver();
  }

  /**
   * Returns the graph building blacklist. The iterator will never return elements that are matched by the blacklist rules.
   * 
   * @return the current graph building blacklist, not null
   */
  protected FunctionBlacklistQuery getBlacklist() {
    return getFunctionCompilationContext().getGraphBuildingBlacklist();
  }

  /**
   * Comparator to give a fixed ordering of functions at the same priority so that we at least have deterministic behavior between runs.
   */
  private static final Comparator<Pair<ResolutionRule, Collection<ValueSpecification>>> RULE_COMPARATOR = new Comparator<Pair<ResolutionRule, Collection<ValueSpecification>>>() {
    @Override
    public int compare(Pair<ResolutionRule, Collection<ValueSpecification>> o1, Pair<ResolutionRule, Collection<ValueSpecification>> o2) {
      int c = o1.getFirst().getParameterizedFunction().getFunction().getFunctionDefinition().getUniqueId()
          .compareTo(o2.getFirst().getParameterizedFunction().getFunction().getFunctionDefinition().getUniqueId());
      if (c != 0) {
        return c;
      }
      // Have the same function, can try and order the "FunctionParameters" as we know it implements a hash code
      c = o1.getFirst().getParameterizedFunction().getParameters().hashCode() - o2.getFirst().getParameterizedFunction().getParameters().hashCode();
      if (c != 0) {
        return c;
      }
      throw new OpenGammaRuntimeException("Rule priority conflict - cannot order " + o1 + " against " + o2);
    }
  };

  private static Collection<ValueSpecification> reduceMemory(final Set<ValueSpecification> specifications) {
    if (specifications.size() == 1) {
      final ValueSpecification specification = specifications.iterator().next();
      final ValueSpecification reducedSpecification = MemoryUtils.instance(specification);
      if (specification == reducedSpecification) {
        return specifications;
      } else {
        return Collections.singleton(reducedSpecification);
      }
    } else {
      final Collection<ValueSpecification> result = new ArrayList<ValueSpecification>(specifications.size());
      for (ValueSpecification specification : specifications) {
        result.add(MemoryUtils.instance(specification));
      }
      return result;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public Iterator<Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>>> resolveFunction(final ValueRequirement requirement, final ComputationTarget target) {
    final ComputationTargetSpecification targetSpecification = MemoryUtils.instance(target.toSpecification());
    Pair<ResolutionRule[], Collection<ValueSpecification>[]> cached = _targetCache.get(targetSpecification);
    if (cached == null) {
      final LinkedList<ResolutionRule> resolutionRules = new LinkedList<ResolutionRule>();
      final LinkedList<Collection<ValueSpecification>> resolutionResults = new LinkedList<Collection<ValueSpecification>>();
      final SortedMap<Integer, Collection<ResolutionRule>> priority2Rules = _type2Priority2Rules.get(target.getType());
      if (priority2Rules != null) {
        for (Collection<ResolutionRule> rules : priority2Rules.values()) {
          int rulesFound = 0;
          for (ResolutionRule rule : rules) {
            final Set<ValueSpecification> results = rule.getResults(target, getFunctionCompilationContext());
            if ((results != null) && !results.isEmpty()) {
              resolutionRules.add(rule);
              resolutionResults.add(reduceMemory(results));
              rulesFound++;
            }
          }
          if (rulesFound > 1) {
            // sort only the sub-list of rules associated with the priority
            final Iterator<ResolutionRule> rulesIterator = resolutionRules.descendingIterator();
            final Iterator<Collection<ValueSpecification>> resultsIterator = resolutionResults.descendingIterator();
            final Pair<ResolutionRule, Collection<ValueSpecification>>[] found = new Pair[rulesFound];
            for (int i = 0; i < rulesFound; i++) {
              found[i] = Pair.of(rulesIterator.next(), resultsIterator.next());
              rulesIterator.remove();
              resultsIterator.remove();
            }
            // TODO [ENG-260] re-order the last "rulesFound" rules in the list with a cost-based heuristic (cheapest first)
            // TODO [ENG-260] throw an exception if there are two rules which can't be re-ordered
            // REVIEW 2010-10-27 Andrew -- Could the above be done with a Comparator<Pair<ParameterizedFunction, ValueSpecification>>
            // provided in the compilation context? This could do away with the need for our "priority" levels as that can do ALL ordering.
            // We should wrap it at construction in something that will detect the equality case and trigger an exception.
            Arrays.sort(found, RULE_COMPARATOR);
            for (int i = 0; i < rulesFound; i++) {
              resolutionRules.add(found[i].getFirst());
              resolutionResults.add(found[i].getSecond());
            }
          }
        }
      } else {
        s_logger.warn("No rules for target type {}", target);
      }
      // TODO: the array of rules is probably getting duplicated for each similar target (e.g. all swaps probably use the same rules)
      cached = (Pair<ResolutionRule[], Collection<ValueSpecification>[]>) (Pair<?, ?>) Pair.of(resolutionRules.toArray(new ResolutionRule[resolutionRules.size()]),
          resolutionResults.toArray(new Collection[resolutionResults.size()]));
      final Pair<ResolutionRule[], Collection<ValueSpecification>[]> existing = _targetCache.putIfAbsent(targetSpecification, cached);
      if (existing != null) {
        cached = existing;
      }
    }
    return new It(target, targetSpecification, getTargetResolver(), getBlacklist(), requirement, cached);
  }

  /**
   * Iterator of functions and specifications from a dependency node.
   */
  private static final class It implements Iterator<Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>>> {

    private final ComputationTargetResolver _resolver;
    private final FunctionBlacklistQuery _blacklist;
    private final ComputationTargetSpecification _target;
    private final ValueRequirement _requirement;
    private final Pair<ResolutionRule[], Collection<ValueSpecification>[]> _values;
    private int _itr;
    private Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>> _next;

    private It(final ComputationTarget target, final ComputationTargetSpecification targetSpecification, final ComputationTargetResolver resolver, final FunctionBlacklistQuery blacklist,
        final ValueRequirement requirement,
        final Pair<ResolutionRule[], Collection<ValueSpecification>[]> values) {
      _resolver = resolver;
      _blacklist = blacklist;
      _target = targetSpecification;
      _requirement = requirement;
      _values = values;
      findNext(target);
    }

    private void findNext(final ComputationTarget target) {
      final ResolutionRule[] rules = _values.getFirst();
      final Collection<ValueSpecification>[] resultSets = _values.getSecond();
      while (_itr < rules.length) {
        final ResolutionRule rule = rules[_itr];
        if (!_blacklist.isBlacklisted(rule.getParameterizedFunction(), _target)) {
          final Collection<ValueSpecification> resultSet = resultSets[_itr++];
          final ValueSpecification result = rule.getResult(_requirement, target, resultSet);
          if (result != null) {
            _next = Triple.of(rule.getParameterizedFunction(), result, resultSet);
            return;
          }
        }
      }
      _next = null;
    }

    @Override
    public boolean hasNext() {
      if (_next == null) {
        findNext(_resolver.resolve(_target));
      }
      return _next != null;
    }

    @Override
    public Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>> next() {
      if (_next == null) {
        findNext(_resolver.resolve(_target));
      }
      Triple<ParameterizedFunction, ValueSpecification, Collection<ValueSpecification>> next = _next;
      _next = null;
      return next;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

}
