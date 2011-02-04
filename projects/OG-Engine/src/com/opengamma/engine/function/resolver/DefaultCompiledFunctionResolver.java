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
import java.util.TreeMap;

import org.slf4j.Logger;

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

  @SuppressWarnings("unchecked")
  @Override
  public List<Pair<ParameterizedFunction, ValueSpecification>> resolveFunction(ValueRequirement requirement, DependencyNode atNode) {
    s_debugResolveFunctions++;
    s_debugResolveFunctionsTime -= System.nanoTime();
    final LinkedList<Pair<ParameterizedFunction, ValueSpecification>> applicableRules = new LinkedList<Pair<ParameterizedFunction, ValueSpecification>>();
    // process rules in descending priority order
    for (Map.Entry<Integer, Collection<ResolutionRule>> entry : _type2Priority2Rules.get(atNode.getComputationTarget().getType()).entrySet()) {
      final Collection<ResolutionRule> rules = entry.getValue();
      int rulesFound = 0;
      for (ResolutionRule rule : rules) {
        final ValueSpecification result = rule.getResult(requirement, atNode, getFunctionCompilationContext());
        if (result != null) {
          applicableRules.add(Pair.of(rule.getFunction(), result));
          rulesFound++;
        }
      }
      if (rulesFound > 1) {
        s_multipleRules++;
        s_multipleRulesTime -= System.nanoTime();
        final Iterator<Pair<ParameterizedFunction, ValueSpecification>> iterator = applicableRules.descendingIterator();
        final Pair<ParameterizedFunction, ValueSpecification>[] found = new Pair[rulesFound];
        for (int i = 0; i < rulesFound; i++) {
          found[i] = iterator.next();
          iterator.remove();
        }
        // TODO [ENG-260] re-order the last "rulesFound" rules in the list with a cost-based heuristic (cheapest first)
        // TODO [ENG-260] throw an exception if there are two rules which can't be re-ordered
        // REVIEW 2010-10-27 Andrew -- Could the above be done with a Comparator<Pair<ParameterizedFunction, ValueSpecification>> provided in the compilation
        // context? This could do away with the need for our "priority" levels as that can do ALL ordering. We should wrap it at construction in something
        // that will detect the equality case and trigger an exception.
        Arrays.<Pair<ParameterizedFunction, ValueSpecification>> sort(found, s_ruleComparator);
        for (int i = 0; i < rulesFound; i++) {
          applicableRules.add(found[i]);
        }
        s_multipleRulesTime += System.nanoTime();
      }
    }
    s_debugResolveFunctionsTime += System.nanoTime();
    if (applicableRules.isEmpty()) {
      s_debugExceptions++;
      throw new UnsatisfiableDependencyGraphException("There is no rule that can satisfy requirement " + requirement + " for target " + atNode.getComputationTarget());
    }
    return applicableRules;
  }

  // Reporting for PLAT-501 only

  private static int s_debugResolveFunctions;
  private static long s_debugResolveFunctionsTime;
  private static int s_debugExceptions;
  private static int s_multipleRules;
  private static long s_multipleRulesTime;

  public static void report(final Logger logger) {
    logger.debug("resolveFunction invocations {}", s_debugResolveFunctions);
    s_debugResolveFunctions = 0;
    logger.debug("resolveFunction {}ms", (double) s_debugResolveFunctionsTime / 1e6);
    s_debugResolveFunctionsTime = 0;
    logger.debug("exceptions {}", s_debugExceptions);
    s_debugExceptions = 0;
    logger.debug("multipleRules {}", s_multipleRules);
    s_multipleRules = 0;
    logger.debug("multipleRulesTime {}", s_multipleRulesTime);
    s_multipleRulesTime = 0;
    ResolutionRule.report(logger);
  }

}
