/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * 
 */
public abstract class AbstractForexSpotRateMarketDataFunction extends AbstractFunction.NonCompiledInvoker {

  private ComputationTargetType _targetType = ComputationTargetType.PRIMITIVE;

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
    ComputedValue inputValue = Iterables.getOnlyElement(inputs.getAllValues());
    ValueSpecification outputSpec = new ValueSpecification(ValueRequirementNames.SPOT_RATE, target.toSpecification(), createValueProperties().get());
    return ImmutableSet.of(new ComputedValue(outputSpec, inputValue.getValue()));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return _targetType;
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    return target.getType() == _targetType && target.getUniqueId().getScheme().equals(UnorderedCurrencyPair.OBJECT_SCHEME);
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return ImmutableSet.of(new ValueSpecification(ValueRequirementNames.SPOT_RATE, target.toSpecification(), createValueProperties().get()));
  }

}
