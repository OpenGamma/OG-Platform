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
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;

/**
 *
 */
//TODO rename
public abstract class AbstractFXSpotRateHistoricalTimeSeriesFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final String desiredCurrency = desiredValue.getConstraint(ValuePropertyNames.CURRENCY);
    final ComputedValue inputValue = Iterables.getOnlyElement(inputs.getAllValues());
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.CURRENCY, desiredCurrency).get();
    final ValueSpecification outputSpec = new ValueSpecification(ValueRequirementNames.HISTORICAL_FX_TIME_SERIES, target.toSpecification(), properties);
    return ImmutableSet.of(new ComputedValue(outputSpec, inputValue.getValue()));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getUniqueId() == null) {
      return false;
    }
    if (!(target.getSecurity() instanceof FinancialSecurity)) {
      return false;
    }
    try {
      FinancialSecurityUtils.getCurrency(target.getSecurity());
      return true;
    } catch (final UnsupportedOperationException e) {
      return false;
    }
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties().withAny(ValuePropertyNames.CURRENCY).get();
    return ImmutableSet.of(new ValueSpecification(ValueRequirementNames.HISTORICAL_FX_TIME_SERIES, target.toSpecification(), properties));
  }

}
