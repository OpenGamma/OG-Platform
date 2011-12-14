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
import com.opengamma.OpenGammaRuntimeException;
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
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculatorFactory;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class EquityPnLFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Position position = target.getPosition();
    final ComputedValue fairValueCV = inputs.getComputedValue(new ValueRequirement(ValueRequirementNames.FAIR_VALUE,
        ComputationTargetType.SECURITY, position.getSecurity().getUniqueId(), ValueProperties.withAny(ValuePropertyNames.CURRENCY).get()));
    final Object fairValueObj = fairValueCV.getValue();
    if (fairValueObj == null) {
      throw new OpenGammaRuntimeException("Asset fair value was null");
    }
    final Object priceSeriesObj = inputs.getValue(new ValueRequirement(ValueRequirementNames.PRICE_SERIES, ComputationTargetType.SECURITY, position.getSecurity().getUniqueId()));
    if (priceSeriesObj == null) {
      throw new OpenGammaRuntimeException("Asset price series was null");
    }
    final String samplingPeriodName = desiredValues.iterator().next().getConstraint(ValuePropertyNames.SAMPLING_PERIOD);
    final String scheduleCalculatorName = desiredValues.iterator().next().getConstraint(ValuePropertyNames.SCHEDULE_CALCULATOR);
    final String samplingFunctionName = desiredValues.iterator().next().getConstraint(ValuePropertyNames.SAMPLING_FUNCTION);
    final Set<String> returnCalculatorNames = desiredValues.iterator().next().getConstraints().getValues(ValuePropertyNames.RETURN_CALCULATOR);
    final Double fairValue = (Double) fairValueObj;
    final TimeSeriesReturnCalculator returnCalculator = getTimeSeriesReturnCalculator(returnCalculatorNames);
    final DoubleTimeSeries<?> returnSeries = returnCalculator.evaluate((DoubleTimeSeries<?>) priceSeriesObj);
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, fairValueCV.getSpecification().getProperty(ValuePropertyNames.CURRENCY))
        .with(ValuePropertyNames.SAMPLING_PERIOD, samplingPeriodName)
        .with(ValuePropertyNames.SCHEDULE_CALCULATOR, scheduleCalculatorName)
        .with(ValuePropertyNames.SAMPLING_FUNCTION, samplingFunctionName)
        .with(ValuePropertyNames.RETURN_CALCULATOR, returnCalculatorNames.iterator().next()).get();
    final ValueSpecification valueSpecification = new ValueSpecification(new ValueRequirement(ValueRequirementNames.PNL_SERIES, position, properties), getUniqueId());
    //TODO how do we get dividend data for an equity?
    final ComputedValue result = new ComputedValue(valueSpecification, returnSeries.multiply(fairValue).multiply(position.getQuantity().doubleValue()));
    return Sets.newHashSet(result);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.POSITION && target.getPosition().getSecurity() instanceof EquitySecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {
      final ValueProperties constraints = desiredValue.getConstraints();
      final Set<String> samplingPeriodName = constraints.getValues(ValuePropertyNames.SAMPLING_PERIOD);
      if (samplingPeriodName == null || samplingPeriodName.isEmpty() || samplingPeriodName.size() != 1) {
        return null;
      }
      final Set<String> scheduleCalculatorName = constraints.getValues(ValuePropertyNames.SCHEDULE_CALCULATOR);
      if (scheduleCalculatorName == null || scheduleCalculatorName.isEmpty() || scheduleCalculatorName.size() != 1) {
        return null;
      }
      final Set<String> samplingFunctionName = constraints.getValues(ValuePropertyNames.SAMPLING_FUNCTION);
      if (samplingFunctionName == null || samplingFunctionName.isEmpty() || samplingFunctionName.size() != 1) {
        return null;
      }
      final Set<String> returnCalculatorName = constraints.getValues(ValuePropertyNames.RETURN_CALCULATOR);
      if (returnCalculatorName == null || returnCalculatorName.isEmpty() || returnCalculatorName.size() != 1) {
        return null;
      }
      final Position position = target.getPosition();
      final String currency = FinancialSecurityUtils.getCurrency(position.getSecurity()).getCode();
      final EquitySecurity equity = (EquitySecurity) position.getSecurity();
      final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
      final ValueProperties priceSeriesProperties = ValueProperties.builder()
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(ValuePropertyNames.SAMPLING_PERIOD, samplingPeriodName.iterator().next())
        .with(ValuePropertyNames.SCHEDULE_CALCULATOR, scheduleCalculatorName.iterator().next())
        .with(ValuePropertyNames.SAMPLING_FUNCTION, samplingFunctionName.iterator().next()).get();
      requirements.add(new ValueRequirement(ValueRequirementNames.FAIR_VALUE, ComputationTargetType.SECURITY, equity.getUniqueId(), ValueProperties.withAny(ValuePropertyNames.CURRENCY).get()));
      requirements.add(new ValueRequirement(ValueRequirementNames.PRICE_SERIES, ComputationTargetType.SECURITY, equity.getUniqueId(), priceSeriesProperties));
      return requirements;
    }
    return null;
  }
  
  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target,
      final Map<ValueSpecification, ValueRequirement> inputs) {
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
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, currency)
        .withAny(ValuePropertyNames.SAMPLING_PERIOD)
        .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
        .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
        .withAny(ValuePropertyNames.RETURN_CALCULATOR).get();
    return Sets.newHashSet(new ValueSpecification(
        new ValueRequirement(ValueRequirementNames.PNL_SERIES, target.getPosition(), properties), getUniqueId()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      final ValueProperties properties = createValueProperties()
        .withAny(ValuePropertyNames.CURRENCY)
        .withAny(ValuePropertyNames.SAMPLING_PERIOD)
        .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
        .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
        .withAny(ValuePropertyNames.RETURN_CALCULATOR).get();
      ValueRequirement valueReq = new ValueRequirement(ValueRequirementNames.PNL_SERIES, target.getPosition(), properties);
      return Sets.newHashSet(new ValueSpecification(valueReq, getUniqueId()));
    }
    return null;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  private TimeSeriesReturnCalculator getTimeSeriesReturnCalculator(final Set<String> calculatorNames) {
    if (calculatorNames == null || calculatorNames.isEmpty() || calculatorNames.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique return calculator name: " + calculatorNames);
    }
    return TimeSeriesReturnCalculatorFactory.getReturnCalculator(calculatorNames.iterator().next());
  }
}
