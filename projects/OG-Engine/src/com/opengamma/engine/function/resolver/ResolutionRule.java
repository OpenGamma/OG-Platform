/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
      final ComputationTarget target = atNode.getComputationTarget();
      final CompiledFunctionDefinition function = _parameterizedFunction.getFunction();
      // Check the function can apply to the target
      s_debugCanApplyTo++;
      s_debugCanApplyToTime -= System.nanoTime();
      try {
        if (!function.canApplyTo(context, target)) {
          return null;
        }
      } finally {
        s_debugCanApplyToTime += System.nanoTime();
      }
      // Get the maximal set of results the function can produce for the target
      s_debugFunctionResults++;
      s_debugFunctionResultsTime -= System.nanoTime();
      Set<ValueSpecification> resultSpecs = null;
      try {
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
      // Of the maximal outputs, is one valid for the requirement
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
      // Has the function been used in the graph above the node - i.e. can we introduce it without
      // creating a cycle
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
      // Apply the target filter for this rule (this is applied last because filters probably rarely exclude compared to the other tests)
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
  }

}
