/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import java.util.Set;

import com.opengamma.engine.ComputationTarget;
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

  /**
   * The function advertised by this rule can validly produce the desired
   * output only if:
   * <ol>
   * <li>The function can produce the output; and
   * <li>This resolution rule applies to the given computation target  
   * </ol>
   * <p>
   * The implementation has been split into two accessible components to allow
   * a resolver to cache the intermediate results. This is more efficient than
   * repeated calls to this method.
   * 
   * @param output Output you want the function to produce
   * @param target Computation target  
   * @param context Function compilation context
   * @return Null if this the function advertised by this rule cannot produce 
   * the desired output, a valid ValueSpecification otherwise - as returned by
   * the function. The specification is not composed against the requirement
   * constraints.
   */
  public ValueSpecification getResult(ValueRequirement output, ComputationTarget target, FunctionCompilationContext context) {
    final Set<ValueSpecification> resultSpecs = getResults(target, context);
    if (resultSpecs == null) {
      return null;
    }
    return getResult(output, target, resultSpecs);
  }

  /**
   * The first half of the full {@link #getResult(ValueRequirement,ComputationTarget,FunctionCompilationContext)} implementation
   * returning the set of all function outputs for use by {@link #getResult(ValueRequirement,ComputationTarget,FunctionCompilationContext,Set)}.
   * 
   * @param target the computation target
   * @param context Function compilation context
   * @return the set of all value specifications produced by the function, null if none can be produced
   */
  public Set<ValueSpecification> getResults(final ComputationTarget target, final FunctionCompilationContext context) {
    final CompiledFunctionDefinition function = _parameterizedFunction.getFunction();
    // check the function can apply to the target
    if (!function.canApplyTo(context, target)) {
      return null;
    }
    // return the maximal set of results the function can produce for the target
    return function.getResults(context, target);
  }

  /**
   * The second half of the full
   * {@link #getResult(ValueRequirement, ComputationTarget, FunctionCompilationContext)})
   * implementation taking the set of all function outputs produced by {@link #getResults}.
   * 
   * @param output Output you want the function to produce
   * @param target Computation target
   * @param resultSpecs The results from {@code getResults()}, not null
   * @return Null if this the function advertised by this rule cannot produce 
   * the desired output, a valid ValueSpecification otherwise - as returned by
   * the function. The specification is not composed against the requirement
   * constraints.
   */
  public ValueSpecification getResult(final ValueRequirement output, final ComputationTarget target, final Set<ValueSpecification> resultSpecs) {
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
    // Apply the target filter for this rule (this is applied last because filters probably rarely exclude compared to the other tests)
    if (!_computationTargetFilter.accept(target)) {
      return null;
    }
    return validSpec;
  }

  @Override
  public String toString() {
    return "ResolutionRule[" + getFunction() + " at priority " + getPriority() + "]";
  }

}
