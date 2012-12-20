/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Map;
import java.util.Set;

import javax.time.Instant;
import javax.time.InstantProvider;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.NotCalculatedSentinel;

/**
 * A no-op function. This will never be selected during graph construction, but can be present in an execution plan as a placeholder for a suppressed function.
 */
public final class NoOpFunction implements FunctionDefinition, CompiledFunctionDefinition, FunctionInvoker {

  /**
   * Preferred identifier this function will be available in a repository as.
   */
  public static final String UNIQUE_ID = "No-op";

  @Override
  public void init(final FunctionCompilationContext context) {
    // No-op
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final InstantProvider atInstant) {
    return this;
  }

  @Override
  public String getUniqueId() {
    return UNIQUE_ID;
  }

  @Override
  public String getShortName() {
    return UNIQUE_ID;
  }

  @Override
  public FunctionParameters getDefaultParameters() {
    return new EmptyFunctionParameters();
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Set<ComputedValue> result = Sets.newHashSetWithExpectedSize(desiredValues.size());
    for (ValueRequirement desiredValue : desiredValues) {
      result.add(new ComputedValue(new ValueSpecification(desiredValue.getValueName(), desiredValue.getTargetSpecification(), desiredValue.getConstraints()), NotCalculatedSentinel.SUPPRESSED));
    }
    return result;
  }

  @Override
  public boolean canHandleMissingInputs() {
    return true;
  }

  @Override
  public FunctionDefinition getFunctionDefinition() {
    return this;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PRIMITIVE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return false;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean canHandleMissingRequirements() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<ValueRequirement> getAdditionalRequirements(final FunctionCompilationContext context, final ComputationTarget target,
      final Set<ValueSpecification> inputs, Set<ValueSpecification> outputs) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Instant getEarliestInvocationTime() {
    return null;
  }

  @Override
  public Instant getLatestInvocationTime() {
    return null;
  }

  @Override
  public FunctionInvoker getFunctionInvoker() {
    return this;
  }

}
