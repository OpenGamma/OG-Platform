/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Wraps another visitor to gather the return values of it.
 * 
 * @param <T> return type of the visit methods
 */
public class ResolutionFailureGatherer<T> extends ResolutionFailureVisitor<T> {

  private final ResolutionFailureVisitor<T> _underlying;
  private final List<T> _results = new LinkedList<T>();

  public ResolutionFailureGatherer(final ResolutionFailureVisitor<T> underlying) {
    _underlying = underlying;
  }

  public ResolutionFailureVisitor<T> getUnderlying() {
    return _underlying;
  }

  public Collection<T> getResults() {
    return Collections.unmodifiableList(_results);
  }

  private synchronized T result(final T result) {
    _results.add(result);
    return result;
  }

  @Override
  protected T visitCouldNotResolve(final ValueRequirement valueRequirement) {
    return result(getUnderlying().visitCouldNotResolve(valueRequirement));
  }

  @Override
  protected T visitNoFunctions(final ValueRequirement valueRequirement) {
    return result(getUnderlying().visitNoFunctions(valueRequirement));
  }

  @Override
  protected T visitRecursiveRequirement(final ValueRequirement valueRequirement) {
    return result(getUnderlying().visitRecursiveRequirement(valueRequirement));
  }

  @Override
  protected T visitUnsatisfied(final ValueRequirement valueRequirement) {
    return result(getUnderlying().visitUnsatisfied(valueRequirement));
  }

  @Override
  protected T visitMarketDataMissing(final ValueRequirement valueRequirement) {
    return result(getUnderlying().visitMarketDataMissing(valueRequirement));
  }

  @Override
  protected T visitSuccessfulFunction(final ValueRequirement valueRequirement, final ParameterizedFunction function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> satisfied) {
    return result(getUnderlying().visitSuccessfulFunction(valueRequirement, function, desiredOutput, satisfied));
  }

  @Override
  protected T visitFailedFunction(final ValueRequirement valueRequirement, final ParameterizedFunction function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> satisfied, final Set<ResolutionFailure> unsatisfied) {
    return result(getUnderlying().visitFailedFunction(valueRequirement, function, desiredOutput, satisfied, unsatisfied));
  }

  @Override
  protected T visitFailedFunction(final ValueRequirement valueRequirement, final ParameterizedFunction function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> satisfied, final Set<ResolutionFailure> unsatisfied, final Set<ResolutionFailure> unsatisfiedAdditional) {
    return result(getUnderlying().visitFailedFunction(valueRequirement, function, desiredOutput, satisfied, unsatisfied, unsatisfiedAdditional));
  }

  @Override
  protected T visitFunction(final ValueRequirement valueRequirement, final ParameterizedFunction function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> satisfied, final Set<ResolutionFailure> unsatisfied, final Set<ResolutionFailure> unsatisfiedAdditional) {
    return result(getUnderlying().visitFunction(valueRequirement, function, desiredOutput, satisfied, unsatisfied, unsatisfiedAdditional));
  }

  @Override
  protected T visitGetAdditionalRequirementsFailed(final ValueRequirement valueRequirement, final ParameterizedFunction function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> requirements) {
    return result(getUnderlying().visitGetAdditionalRequirementsFailed(valueRequirement, function, desiredOutput, requirements));
  }

  @Override
  protected T visitGetResultsFailed(final ValueRequirement valueRequirement, final ParameterizedFunction function, final ValueSpecification desiredOutput) {
    return result(getUnderlying().visitGetResultsFailed(valueRequirement, function, desiredOutput));
  }

  @Override
  protected T visitGetRequirementsFailed(final ValueRequirement valueRequirement, final ParameterizedFunction function, final ValueSpecification desiredOutput) {
    return result(getUnderlying().visitGetRequirementsFailed(valueRequirement, function, desiredOutput));
  }

  @Override
  protected T visitLateResolutionFailure(final ValueRequirement valueRequirement, final ParameterizedFunction function, final ValueSpecification desiredOutput,
      final Map<ValueSpecification, ValueRequirement> requirements) {
    return result(getUnderlying().visitLateResolutionFailure(valueRequirement, function, desiredOutput, requirements));
  }

}
