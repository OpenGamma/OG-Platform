/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;

/**
 * A no-op function that relabels an input as any of the desired outputs. This will be selected during graph construction to handle any mismatches between value specifications the market data provider
 * is capable of recognizing and the value specifications corresponding to the requirements of a function. The will typically mean that a target has become re-labelled - for example the market data is
 * keyed of a {@link UniqueId} that is not easily converted to/from the {@link ExternalIdBundle} for the actual target.
 */
public final class RelabellingFunction implements FunctionDefinition, CompiledFunctionDefinition, FunctionInvoker {

  /**
   * Singleton instance.
   */
  public static final RelabellingFunction INSTANCE = new RelabellingFunction();

  /**
   * Preferred identifier this function will be available in a repository as.
   */
  public static final String UNIQUE_ID = "Alias";

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
    final Collection<ComputedValue> values = inputs.getAllValues();
    final Set<ComputedValue> result = Sets.newHashSetWithExpectedSize(desiredValues.size() * values.size());
    for (final ValueRequirement desiredValueReq : desiredValues) {
      final ValueSpecification desiredValue = new ValueSpecification(desiredValueReq.getValueName(), target.toSpecification(), desiredValueReq.getConstraints());
      for (final ComputedValue value : values) {
        result.add(new ComputedValue(desiredValue, value.getValue()));
      }
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
    return ComputationTargetType.NULL;
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

}
