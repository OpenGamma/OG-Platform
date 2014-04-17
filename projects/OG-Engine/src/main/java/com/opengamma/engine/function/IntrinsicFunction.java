/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Map;
import java.util.Set;

import org.threeten.bp.Instant;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * A special case of function with special meaning to the engine.
 */
public abstract class IntrinsicFunction implements FunctionDefinition, CompiledFunctionDefinition, FunctionInvoker {

  private final String _uid;

  protected IntrinsicFunction(String uid) {
    _uid = uid;
  }

  // FunctionDefinition

  @Override
  public void init(final FunctionCompilationContext context) {
    // No-op
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return this;
  }

  @Override
  public String getUniqueId() {
    return _uid;
  }

  @Override
  public String getShortName() {
    return _uid;
  }

  // CompiledFunctionDefinition

  @Override
  public FunctionDefinition getFunctionDefinition() {
    return this;
  }

  /**
   * Special case, always returns null.
   * 
   * @return null
   */
  @Override
  public ComputationTargetType getTargetType() {
    return null;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    throw new UnsupportedOperationException();
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
      final Set<ValueSpecification> inputs, final Set<ValueSpecification> outputs) {
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

  @Override
  public FunctionParameters getDefaultParameters() {
    return EmptyFunctionParameters.INSTANCE;
  }

  // FunctionInvoker

  @Override
  public boolean canHandleMissingInputs() {
    return true;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    throw new UnsupportedOperationException();
  }

}
