/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

  /**
   * The map is sorted from highest to lowest priority
   */
  private Map<Integer, Collection<ResolutionRule>> _priority2Rules = new TreeMap<Integer, Collection<ResolutionRule>>(new Comparator<Integer>() {
    @Override
    public int compare(Integer o1, Integer o2) {
      return -o1.compareTo(o2);
    }
  });

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
      Collection<ResolutionRule> rules = _priority2Rules.get(resolutionRule.getPriority());
      if (rules == null) {
        rules = new ArrayList<ResolutionRule>();
        _priority2Rules.put(resolutionRule.getPriority(), rules);
      }
      rules.add(resolutionRule);
    }
  }

  protected FunctionCompilationContext getFunctionCompilationContext() {
    return _functionCompilationContext;
  }

  @Override
  public List<Pair<ParameterizedFunction, ValueSpecification>> resolveFunction(ValueRequirement requirement, DependencyNode atNode) {

    // the idea is to consider highest priority rules first, then the ones with priority
    // just below the highest, and so on until the lowest

    final List<Pair<ParameterizedFunction, ValueSpecification>> applicableRules = new ArrayList<Pair<ParameterizedFunction, ValueSpecification>>();
    for (Map.Entry<Integer, Collection<ResolutionRule>> entry : _priority2Rules.entrySet()) {
      Integer priority = entry.getKey();
      Collection<ResolutionRule> rules = entry.getValue();

      int rulesFound = 0;
      for (ResolutionRule rule : rules) {
        ValueSpecification result = rule.getResult(requirement, atNode, getFunctionCompilationContext());
        if (result != null) {
          applicableRules.add(Pair.of(rule.getFunction(), result));
          rulesFound++;
        }
      }

      if (rulesFound > 1) {
        throw new UnsatisfiableDependencyGraphException("There is more than 1 rule with priority " + priority + " that can satisfy requirement " + requirement + " for target "
            + atNode.getComputationTarget() + ". The rules are: " + applicableRules + " (last " + rulesFound + ")");
      }
    }

    if (applicableRules.isEmpty()) {
      throw new UnsatisfiableDependencyGraphException("There is no rule that can satisfy requirement " + requirement + " for target " + atNode.getComputationTarget());
    }
    return applicableRules;
  }

}
