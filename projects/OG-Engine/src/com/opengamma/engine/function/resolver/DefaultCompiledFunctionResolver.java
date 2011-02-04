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
import java.util.Map;
import java.util.TreeMap;

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
   * Comparator to give a fixed ordering of functions at the same priority so that we at least have deterministic behavior between runs.
   */
  private static final Comparator<Pair<ParameterizedFunction, ValueSpecification>> s_ruleComparator = new Comparator<Pair<ParameterizedFunction, ValueSpecification>>() {

    @Override
    public int compare(Pair<ParameterizedFunction, ValueSpecification> o1, Pair<ParameterizedFunction, ValueSpecification> o2) {
      final int c = o1.getSecond().getProperties().compareTo(o2.getSecond().getProperties());
      if (c != 0) {
        return 0;
      }
      throw new UnsatisfiableDependencyGraphException("Rule priority conflict - cannot order " + o1 + " against " + o2);
    }

  };

  private static class InlineResolutionIterator implements Iterator<Pair<ParameterizedFunction, ValueSpecification>> {

    private final ValueRequirement _requirement;
    private final DependencyNode _node;
    private final FunctionCompilationContext _context;
    private final Iterator<Map.Entry<Integer, Collection<ResolutionRule>>> _entries;
    private Iterator<Pair<ParameterizedFunction, ValueSpecification>> _nexts;
    private Pair<ParameterizedFunction, ValueSpecification> _next;

    public InlineResolutionIterator(final ValueRequirement requirement, final DependencyNode node, final FunctionCompilationContext context,
        final Iterator<Map.Entry<Integer, Collection<ResolutionRule>>> entries) {
      _requirement = requirement;
      _node = node;
      _context = context;
      _entries = entries;
    }

    @Override
    public boolean hasNext() {
      if (_next == null) {
        findNext();
      }
      return _next != null;
    }

    @Override
    public Pair<ParameterizedFunction, ValueSpecification> next() {
      if (_next == null) {
        findNext();
      }
      final Pair<ParameterizedFunction, ValueSpecification> result = _next;
      _next = null;
      return result;
    }

    @SuppressWarnings("unchecked")
    private void findNext() {
      if ((_nexts != null) && _nexts.hasNext()) {
        _next = _nexts.next();
        return;
      }
      LinkedList<Pair<ParameterizedFunction, ValueSpecification>> applicableRules = null;
      while (_entries.hasNext()) {
        int rulesFound = 0;
        for (ResolutionRule rule : _entries.next().getValue()) {
          final ValueSpecification result = rule.getResult(_requirement, _node, _context);
          if (result != null) {
            if (applicableRules == null) {
              if (_next == null) {
                _next = Pair.of(rule.getFunction(), result);
              } else {
                applicableRules = new LinkedList<Pair<ParameterizedFunction, ValueSpecification>>();
                applicableRules.add(_next);
                applicableRules.add(Pair.of(rule.getFunction(), result));
              }
            } else {
              applicableRules.add(Pair.of(rule.getFunction(), result));
            }
            rulesFound++;
          }
        }
        if (rulesFound == 1) {
          _nexts = null;
          return;
        } else if (rulesFound > 1) {
          final Iterator<Pair<ParameterizedFunction, ValueSpecification>> iterator = applicableRules.descendingIterator();
          final Pair<ParameterizedFunction, ValueSpecification>[] found = new Pair[rulesFound];
          for (int i = 0; i < rulesFound; i++) {
            found[i] = iterator.next();
          }
          // TODO [ENG-260] re-order the last "rulesFound" rules in the list with a cost-based heuristic (cheapest first)
          // TODO [ENG-260] throw an exception if there are two rules which can't be re-ordered
          // REVIEW 2010-10-27 Andrew -- Could the above be done with a Comparator<Pair<ParameterizedFunction, ValueSpecification>> provided in the compilation
          // context? This could do away with the need for our "priority" levels as that can do ALL ordering. We should wrap it at construction in something
          // that will detect the equality case and trigger an exception.
          Arrays.<Pair<ParameterizedFunction, ValueSpecification>> sort(found, s_ruleComparator);
          _nexts = Arrays.asList(found).iterator();
          _next = _nexts.next();
          return;
        }
      }
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

  };

  @Override
  public Iterator<Pair<ParameterizedFunction, ValueSpecification>> resolveFunction(ValueRequirement requirement, DependencyNode atNode) {
    // process rules in descending priority order
    final Iterator<Pair<ParameterizedFunction, ValueSpecification>> itr = new InlineResolutionIterator(requirement, atNode, getFunctionCompilationContext(), _type2Priority2Rules.get(
        atNode.getComputationTarget().getType()).entrySet().iterator());
    if (itr.hasNext()) {
      return itr;
    } else {
      throw new UnsatisfiableDependencyGraphException("There is no rule that can satisfy requirement " + requirement + " for target " + atNode.getComputationTarget());
    }
  }

}
