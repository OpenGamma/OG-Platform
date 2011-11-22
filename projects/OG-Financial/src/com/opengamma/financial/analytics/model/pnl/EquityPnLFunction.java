/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.core.position.Position;
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
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculatorFactory;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class EquityPnLFunction extends AbstractFunction.NonCompiledInvoker {

  private final TimeSeriesReturnCalculator _returnCalculator;

  public EquityPnLFunction(final String returnCalculatorName) {
    _returnCalculator = TimeSeriesReturnCalculatorFactory.getReturnCalculator(returnCalculatorName);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {

    final Position position = target.getPosition();
    ComputedValue fairValueCV = inputs.getComputedValue(new ValueRequirement(ValueRequirementNames.FAIR_VALUE,
        ComputationTargetType.SECURITY, position.getSecurity().getUniqueId(), ValueProperties.withAny(ValuePropertyNames.CURRENCY).get()));
    
    final Object fairValueObj = fairValueCV.getValue();
    final Object priceSeriesObj = inputs.getValue(new ValueRequirement(ValueRequirementNames.PRICE_SERIES, ComputationTargetType.SECURITY, position.getSecurity().getUniqueId()));
    if (fairValueObj != null && priceSeriesObj != null) {
      final Double fairValue = (Double) fairValueObj;
      final DoubleTimeSeries<?> returnSeries = _returnCalculator.evaluate((DoubleTimeSeries<?>) priceSeriesObj);
      final ValueSpecification valueSpecification = new ValueSpecification(new ValueRequirement(
          ValueRequirementNames.PNL_SERIES, position, getCurrencyProperties(fairValueCV.getSpecification().getProperty(
              ValuePropertyNames.CURRENCY))), getUniqueId());
      //TODO how do we get dividend data for an equity?
      final ComputedValue result = new ComputedValue(valueSpecification, returnSeries.multiply(fairValue).multiply(position.getQuantity().doubleValue()));
      return Sets.newHashSet(result);
    }
    return null;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.POSITION && target.getPosition().getSecurity() instanceof EquitySecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {
      final Position position = target.getPosition();
      final EquitySecurity equity = (EquitySecurity) position.getSecurity();
      final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
      requirements.add(new ValueRequirement(ValueRequirementNames.FAIR_VALUE, ComputationTargetType.SECURITY, equity.getUniqueId(), ValueProperties.withAny(ValuePropertyNames.CURRENCY).get()));
      requirements.add(new ValueRequirement(ValueRequirementNames.PRICE_SERIES, ComputationTargetType.SECURITY, equity.getUniqueId(), ValueProperties.withAny(ValuePropertyNames.CURRENCY).get()));
      // TODO need to consider fx here?
      return requirements;
    }
    return null;
  }

  
  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target,
      Map<ValueSpecification, ValueRequirement> inputs) {
    if (!canApplyTo(context, target)) {
      return null;
    }
    String currency = null;
    for (Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      String newCurrency = entry.getKey().getProperty(ValuePropertyNames.CURRENCY);
      if (newCurrency != null) {
        if (currency != null && !newCurrency.equals(currency)) {
          //NOTE: there's no guarantee we'll get called back with the right combination 
          return null;
        }

        currency = newCurrency;
      }
    }
    if (currency == null) {
      return null;
    }
    return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.PNL_SERIES, target
        .getPosition(), getCurrencyProperties(currency)), getUniqueId()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      ValueRequirement valueReq = new ValueRequirement(ValueRequirementNames.PNL_SERIES, target.getPosition(), createValueProperties().withAny(ValuePropertyNames.CURRENCY).get());
      return Sets.newHashSet(new ValueSpecification(valueReq, getUniqueId()));
    }
    return null;
  }

  private ValueProperties getCurrencyProperties(String currency) {
    return createValueProperties().with(ValuePropertyNames.CURRENCY, currency).get();
  }

  @Override
  public String getShortName() {
    return "EquityPnL";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

}
