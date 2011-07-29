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

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ResolutionRule.class);

  /**
   * The parameterized function.
   */
  private final ParameterizedFunction _parameterizedFunction;
  /**
   * The target filter.
   */
  private final ComputationTargetFilter _computationTargetFilter;
  /**
   * The priority.
   */
  private final int _priority;

  /**
   * Creates an instance.
   * 
   * @param function  the function, not null
   * @param computationTargetFilter  the filter, not null
   * @param priority  the priority
   */
  public ResolutionRule(ParameterizedFunction function, ComputationTargetFilter computationTargetFilter, int priority) {
    ArgumentChecker.notNull(function, "function");
    ArgumentChecker.notNull(computationTargetFilter, "computationTargetFilter");
    _parameterizedFunction = function;
    _computationTargetFilter = computationTargetFilter;
    _priority = priority;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the parameterized function.
   * 
   * @return the function this rule is advertising, not null
   */
  public ParameterizedFunction getFunction() {
    return _parameterizedFunction;
  }

  /**
   * Gets the filter that the rule uses.
   * 
   * @return the filter in use, not null
   */
  public ComputationTargetFilter getComputationTargetFilter() {
    return _computationTargetFilter;
  }

  /**
   * Gets the priority of the rule.
   * If multiple rules can produce a given output, the one with the highest priority is chosen.
   * 
   * @return the priority
   */
  public int getPriority() {
    return _priority;
  }

  //-------------------------------------------------------------------------
  /**
   * The function advertised by this rule can validly produce the desired
   * output only if:
   * <ol>
   * <li>The function can produce the output
   * <li>The function (applied to the same computation target) is not already
   * in the dependency graph above the current node (i.e., no cycles)
   * <li>This resolution rule applies to the given computation target  
   * </ol>
   * <p>
   * The implementation has been split into two accessible components to allow
   * a resolver to cache the intermediate results.
   * <p>
   * Note that because the method is called during dependency graph construction,
   * you can only validly access:
   * <ul>
   * <li>The computation target of the node
   * <li>Functions and computation targets of the nodes above this node
   * </ul>  
   * 
   * @param output  the output you want the function to produce, not null
   * @param atNode  where in the dependency graph this function would be applied, not null
   * @param context  the context, not null
   * @return the valid value specification returned by the function, not composed against
   *  the requirement constraints, null if the function cannot produce the desired output
   */
  public ValueSpecification getResult(ValueRequirement output, DependencyNode atNode, FunctionCompilationContext context) {
    // REVIEW 2011-07-28 SJC: removed comment suggesting that context should be factored out
    final Set<ValueSpecification> resultSpecs = getResults(atNode.getComputationTarget(), context);
    if (resultSpecs == null) {
      return null;
    }
    return getResult(output, atNode, context, resultSpecs);
  }

  /**
   * Gets the set of matching value specifications.
   * <p>
   * This is the first half of the algorithm.
   * It returns the set of all function outputs for use by the second half.
   * 
   * @param target  the computation target, not null
   * @param context  the context, not null
   * @return the set of all value specifications produced by the function, null if none can be produced
   */
  public Set<ValueSpecification> getResults(final ComputationTarget target, final FunctionCompilationContext context) {
    final CompiledFunctionDefinition function = _parameterizedFunction.getFunction();
    // check the function can apply to the target
    if (!function.canApplyTo(context, target)) {
      return null;
    }
    // return the maximal set of results the function can produce for the target
    try {
      return function.getResults(context, target);
    } catch (Throwable t) {
      s_logger.debug("Exception thrown by getResults", t);
    }
    return null;
  }

  /**
   * Picks the value specification from the supplied set.
   * <p>
   * This is the second half of the algorithm.
   * It checks to see if the output is satisfied by the input specifications.
   * <p>
   * Note that because the method is called during dependency graph construction,
   * you can only validly access:
   * <ul>
   * <li>The computation target of the node
   * <li>Functions and computation targets of the nodes above this node
   * </ul>  
   * 
   * @param output  the output you want the function to produce, not null
   * @param atNode  where in the dependency graph this function would be applied, not null
   * @param context  the context, not null
   * @param resultSpecs  the specifications to examine, not null
   * @return the valid value specification returned by the function, not composed against
   *  the requirement constraints, null if the function cannot produce the desired output
   */
  public ValueSpecification getResult(final ValueRequirement output, final DependencyNode atNode, final FunctionCompilationContext context, final Set<ValueSpecification> resultSpecs) {
    final ComputationTarget target = atNode.getComputationTarget();
    // of the maximal outputs, is one valid for the requirement
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
    // has the function been used in the graph above the node - i.e. can we introduce it without
    // creating a cycle
    if (!checkDependentNodes(getFunction(), target, atNode.getDependentNodes())) {
      return null;
    }
    // apply the target filter for this rule (this is applied last because filters probably rarely exclude compared to the other tests)
    if (!_computationTargetFilter.accept(atNode)) {
      return null;
    }
    return validSpec;
  }

  /**
   * Checks dependent nodes.
   * <p>
   * Note that because the method is called during dependency graph construction,
   * you can only validly access:
   * <ul>
   * <li>The computation target of the node
   * <li>Functions and computation targets of the nodes above this node
   * </ul>  
   * 
   * @param function  the parameterized function, not null
   * @param target  the target, not null
   * @param nodes  the set of nodes, not null
   * @return true if there is no cycle in the graph
   */
  private static boolean checkDependentNodes(final ParameterizedFunction function, final ComputationTarget target, final Set<DependencyNode> nodes) {
    for (DependencyNode node : nodes) {
      if (function.equals(node.getFunction()) && target.equals(node.getComputationTarget())) {
        return false;
      }
      if (!checkDependentNodes(function, target, node.getDependentNodes())) {
        return false;
      }
    }
    return true;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "ResolutionRule[" + getFunction() + " at priority " + getPriority() + "]";
  }

}
