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
import com.opengamma.util.PublicAPI;

/**
 * Advertises a function to a {@link CompiledFunctionResolver}. 
 */
@PublicAPI
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
   * @return The computation target filter being used.
   */
  public ComputationTargetFilter getComputationTargetFilter() {
    return _computationTargetFilter;
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
   * The implementation has been split into two accessible components to allow
   * a resolver to cache the intermediate results.
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
    final Set<ValueSpecification> resultSpecs = getResults(atNode.getComputationTarget(), context);
    if (resultSpecs == null) {
      return null;
    }
    return getResult(output, atNode, context, resultSpecs);
  }

  /**
   * The first half of the full {@link #getResult(ValueRequirement,DependencyNode,FunctionCompilationContext)} implementation
   * returning the set of all function outputs for use by {@link #getResult(ValueRequirement,DependencyNode,FunctionCompilationContext,Set)}.
   * 
   * @param target the computation target
   * @param context This should really be refactored out
   * @return the set of all value specifications produced by the function, null if none can be produced
   */
  public Set<ValueSpecification> getResults(final ComputationTarget target, final FunctionCompilationContext context) {
    final CompiledFunctionDefinition function = _parameterizedFunction.getFunction();
    // Check the function can apply to the target
    if (!function.canApplyTo(context, target)) {
      return null;
    }
    // Return the maximal set of results the function can produce for the target
    try {
      return function.getResults(context, target);
    } catch (Throwable t) {
      s_logger.debug("Exception thrown by getResults", t);
    }
    return null;
  }

  /**
   * The second half of the full
   * {@link #getResult(ValueRequirement, DependencyNode, FunctionCompilationContext)})
   * implementation taking the set of all function outputs produced by {@link #getResults}.
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
   * @param resultSpecs The results from {@code getResults()}, not null
   * @return Null if this the function advertised by this rule cannot produce 
   * the desired output, a valid ValueSpecification otherwise - as returned by
   * the function. The specification is not composed against the requirement
   * constraints.
   */
  public ValueSpecification getResult(final ValueRequirement output, final DependencyNode atNode, final FunctionCompilationContext context, final Set<ValueSpecification> resultSpecs) {
    final ComputationTarget target = atNode.getComputationTarget();
    // Of the maximal outputs, is one valid for the requirement
    ValueSpecification validSpec = null;
    for (ValueSpecification resultSpec : resultSpecs) {
      //s_logger.debug("Considering {} for {}", resultSpec, output);
      if (output.isSatisfiedBy(resultSpec)) {
        validSpec = resultSpec;
      }
    }
    if (validSpec == null) {
      return null;
    }
    // Has the function been used in the graph above the node - i.e. can we introduce it without
    // creating a cycle
    DependencyNode parent = atNode.getDependentNode();
    while (parent != null) {
      if (parent.getFunction().equals(getFunction()) && parent.getComputationTarget().equals(target)) {
        return null;
      }
      parent = parent.getDependentNode();
    }
    // Apply the target filter for this rule (this is applied last because filters probably rarely exclude compared to the other tests)
    if (!_computationTargetFilter.accept(atNode)) {
      return null;
    }
    return validSpec;
  }

  /**
   * If multiple rules can produce a given output, the one with the highest 
   * priority is chosen.
   * 
   * @return the priority
   */
  public int getPriority() {
    return _priority;
  }

  @Override
  public String toString() {
    return "ResolutionRule[" + getFunction() + " at priority " + getPriority() + "]";
  }

}
