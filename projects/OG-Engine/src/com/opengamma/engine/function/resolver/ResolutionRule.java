/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Advertises a function to a FunctionResolver. 
 */
public class ResolutionRule {

  private static final Logger s_logger = LoggerFactory.getLogger(ResolutionRule.class);

  private final ParameterizedFunction _parameterizedFunction;
  private final ComputationTargetFilter _computationTargetFilter;
  private final int _priority;

  public ResolutionRule(ParameterizedFunction function, ComputationTargetFilter computationTargetFilter, int priority) {
    ArgumentChecker.notNull(function, "function");
    ArgumentChecker.notNull(computationTargetFilter, "computationTargetFilter");

    _parameterizedFunction = function;
    _computationTargetFilter = computationTargetFilter;
    _priority = priority;
  }

  /**
   * @return The function this rule is advertising
   */
  public ParameterizedFunction getFunction() {
    return _parameterizedFunction;
  }

  /**
   * The function advertised by this rule can validly produce the desired
   * output only if:
   * 
   * <ol>
   * <li>The function can produce the output
   * <li>The function (applied to the same computation target) is not already
   * in the dep graph above the current node (i.e., no cycles)
   * <li>This resolution rule applies to the given computation target  
   * </ol>
   * 
   * @param output Output you want the function to produce
   * @param atNode Where in the dep graph this function would be applied.
   * Note that because the method is called during dep graph construction,
   * you can only validly access:
   * <ul>
   * <li>The computation target of the node
   * <li>Functions and computation targets of the nodes above this node
   * </ul>  
   * @param context This should really be refactored out.
   * @return Null if this the function advertised by this rule cannot produce 
   * the desired output, a valid ValueSpecification otherwise - as returned by
   * the function. The specification is not composed against the requirement
   * constraints.
   */
  public ValueSpecification getResult(ValueRequirement output, DependencyNode atNode, FunctionCompilationContext context) {
    s_debugGetResults++;
    s_debugGetResultsTime -= System.nanoTime();
    try {
      CompiledFunctionDefinition function = _parameterizedFunction.getFunction();
      ComputationTarget target = atNode.getComputationTarget();

      // First check that the function can produce the output

      s_debugCanApplyTo++;
      s_debugCanApplyToTime -= System.nanoTime();
      try {
        if (!function.canApplyTo(context, target)) {
          return null;
        }
      } finally {
        s_debugCanApplyToTime += System.nanoTime();
      }

      s_debugFunctionResults++;
      s_debugFunctionResultsTime -= System.nanoTime();
      Set<ValueSpecification> resultSpecs = null;
      try {
        incrementOrPut(s_debugFunctionResultsMap, Pair.of(function.getFunctionDefinition(), target));
        try {
          resultSpecs = function.getResults(context, target);
        } catch (Throwable t) {
          s_logger.debug("Exception thrown by getResults", t);
        }
        if (resultSpecs == null) {
          // Exceptions and null returns are okay - the backtracking will hopefully find a way to satisfy the graph requirements
          return null;
        }
      } finally {
        s_debugFunctionResultsTime += System.nanoTime();
      }

      s_debugIsSatisfiedBy++;
      s_debugIsSatisfiedByTime -= System.nanoTime();
      ValueSpecification validSpec = null;
      try {
        for (ValueSpecification resultSpec : resultSpecs) {
          if (output.isSatisfiedBy(resultSpec)) {
            validSpec = resultSpec;
          }
        }
      } finally {
        s_debugIsSatisfiedByTime += System.nanoTime();
      }

      if (validSpec == null) {
        return null;
      }

      // Then check that the function (applied to the same computation target) is not already
      // in the dep graph above the current node (i.e., no cycles)

      s_debugCycleTest++;
      s_debugCycleTestTime -= System.nanoTime();
      try {
        DependencyNode parent = atNode.getDependentNode();
        while (parent != null) {
          if (parent.getFunction().equals(getFunction()) && parent.getComputationTarget().equals(target)) {
            return null;
          }
          parent = parent.getDependentNode();
        }
      } finally {
        s_debugCycleTestTime += System.nanoTime();
      }

      // Finally check that the computation target is a valid computation target for this rule
      if (!_computationTargetFilter.accept(atNode)) {
        return null;
      }

      return validSpec;
    } finally {
      s_debugGetResultsTime += System.nanoTime();
    }
  }

  /**
   * If multiple rules can produce a given output, the one with the highest 
   * priority is chosen.
   * 
   * @return The priority. 
   */
  public int getPriority() {
    return _priority;
  }

  @Override
  public String toString() {
    return "ResolutionRule[" + getFunction() + " at priority " + getPriority() + "]";
  }

  // Reporting for PLAT-501 only

  private static int s_debugGetResults;
  private static long s_debugGetResultsTime;
  private static int s_debugCanApplyTo;
  private static long s_debugCanApplyToTime;
  private static int s_debugFunctionResults;
  private static long s_debugFunctionResultsTime;
  private static int s_debugIsSatisfiedBy;
  private static long s_debugIsSatisfiedByTime;
  private static int s_debugCycleTest;
  private static long s_debugCycleTestTime;
  private static Map<Pair<FunctionDefinition, ComputationTarget>, AtomicInteger> s_debugFunctionResultsMap = new HashMap<Pair<FunctionDefinition, ComputationTarget>, AtomicInteger>();

  private static <T> void incrementOrPut(final Map<T, AtomicInteger> map, final T key) {
    AtomicInteger v = map.get(key);
    if (v == null) {
      v = new AtomicInteger(1);
      map.put(key, v);
    } else {
      v.incrementAndGet();
    }
  }

  public static void report(final Logger logger) {
    logger.debug("getResults {} in {}ms", s_debugGetResults, (double) s_debugGetResultsTime / 1e6);
    s_debugGetResults = 0;
    s_debugGetResultsTime = 0;
    logger.debug("canApplyTo {} in {}ms", s_debugCanApplyTo, (double) s_debugCanApplyToTime / 1e6);
    s_debugCanApplyTo = 0;
    s_debugCanApplyToTime = 0;
    logger.debug("functionResults {} in {}ms", s_debugFunctionResults, (double) s_debugFunctionResultsTime / 1e6);
    s_debugFunctionResults = 0;
    s_debugFunctionResultsTime = 0;
    logger.debug("isSatisfiedBy {} in {}ms", s_debugIsSatisfiedBy, (double) s_debugIsSatisfiedByTime / 1e6);
    s_debugIsSatisfiedBy = 0;
    s_debugIsSatisfiedByTime = 0;
    logger.debug("cycleTest {} in {}ms", s_debugCycleTest, (double) s_debugCycleTestTime / 1e6);
    s_debugCycleTest = 0;
    s_debugCycleTestTime = 0;
    final Map<Integer, AtomicInteger> counts = new HashMap<Integer, AtomicInteger>();
    for (Map.Entry<Pair<FunctionDefinition, ComputationTarget>, AtomicInteger> results : s_debugFunctionResultsMap.entrySet()) {
      incrementOrPut(counts, results.getValue().intValue());
    }
    logger.debug("resultsCount {}", counts);
  }

}
