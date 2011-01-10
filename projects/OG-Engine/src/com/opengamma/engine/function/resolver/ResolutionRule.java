/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

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
    CompiledFunctionDefinition function = _parameterizedFunction.getFunction();
    ComputationTarget target = atNode.getComputationTarget();

    // First check that the function can produce the output

    if (!function.canApplyTo(context, target)) {
      return null;
    }

    Set<ValueSpecification> resultSpecs = null;
    try {
      resultSpecs = function.getResults(context, target);
    } catch (Throwable t) {
      s_logger.debug("Exception thrown by getResults", t);
    }
    if (resultSpecs == null) {
      // Exceptions and null returns are okay - the backtracking will hopefully find a way to satisfy the graph requirements
      return null;
    }

    ValueSpecification validSpec = null;
    for (ValueSpecification resultSpec : resultSpecs) {
      if (output.isSatisfiedBy(resultSpec)) {
        validSpec = resultSpec;
      }
    }

    if (validSpec == null) {
      return null;
    }

    // Then check that the function (applied to the same computation target) is not already
    // in the dep graph above the current node (i.e., no cycles)

    DependencyNode parent = atNode.getDependentNode();
    while (parent != null) {
      if (parent.getFunction().equals(getFunction()) && parent.getComputationTarget().equals(target)) {
        return null;
      }
      parent = parent.getDependentNode();
    }

    // Finally check that the computation target is a valid computation target for this rule
    if (!_computationTargetFilter.accept(atNode)) {
      return null;
    }

    return validSpec;
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

}
