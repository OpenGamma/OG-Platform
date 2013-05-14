/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.pnl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.analytics.financial.timeseries.returns.TimeSeriesReturnCalculatorFactory;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 */
public class EquityPnLFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getPosition().getSecurity() instanceof EquitySecurity;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    //final String currency = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    final String currency = FinancialSecurityUtils.getCurrency(target.getPosition().getSecurity()).getCode();
    // Please see http://jira.opengamma.com/browse/PLAT-2330 for information about the PROPERTY_PNL_CONTRIBUTIONS constant
    final ValueProperties properties = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, currency)
        .withAny(ValuePropertyNames.SAMPLING_PERIOD)
        .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
        .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
        .withAny(ValuePropertyNames.RETURN_CALCULATOR)
        .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, "Delta").get();
    return Collections.singleton(new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> samplingPeriodName = constraints.getValues(ValuePropertyNames.SAMPLING_PERIOD);
    if (samplingPeriodName == null || samplingPeriodName.size() != 1) {
      return null;
    }
    final Set<String> scheduleCalculatorName = constraints.getValues(ValuePropertyNames.SCHEDULE_CALCULATOR);
    if (scheduleCalculatorName == null || scheduleCalculatorName.size() != 1) {
      return null;
    }
    final Set<String> samplingFunctionName = constraints.getValues(ValuePropertyNames.SAMPLING_FUNCTION);
    if (samplingFunctionName == null || samplingFunctionName.size() != 1) {
      return null;
    }
    final Set<String> returnCalculatorName = constraints.getValues(ValuePropertyNames.RETURN_CALCULATOR);
    if (returnCalculatorName == null || returnCalculatorName.size() != 1) {
      return null;
    }
    //final String currency = FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    final String currency = FinancialSecurityUtils.getCurrency(target.getPosition().getSecurity()).getCode();
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    final ValueProperties priceSeriesProperties = ValueProperties.builder()
        .with(ValuePropertyNames.CURRENCY, currency)
        .with(ValuePropertyNames.SAMPLING_PERIOD, samplingPeriodName)
        .with(ValuePropertyNames.SCHEDULE_CALCULATOR, scheduleCalculatorName)
        .with(ValuePropertyNames.SAMPLING_FUNCTION, samplingFunctionName).get();
    // final ComputationTargetSpecification targetSpec = target.toSpecification();
    final ComputationTargetSpecification targetSpec = new ComputationTargetSpecification(ComputationTargetType.SECURITY, target.getPosition().getSecurity().getUniqueId());
    requirements.add(new ValueRequirement(ValueRequirementNames.FAIR_VALUE, targetSpec, ValueProperties.with(ValuePropertyNames.CURRENCY, currency).get()));
    requirements.add(new ValueRequirement(ValueRequirementNames.PRICE_SERIES, targetSpec, priceSeriesProperties));
    return requirements;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final ComputedValue fairValueCV = inputs.getComputedValue(ValueRequirementNames.FAIR_VALUE);
    final Object fairValueObj = fairValueCV.getValue();
    if (fairValueObj == null) {
      throw new OpenGammaRuntimeException("Asset fair value was null");
    }
    final double fairValue = (Double) fairValueObj;
    final Object priceSeriesObj = inputs.getValue(ValueRequirementNames.PRICE_SERIES);
    if (priceSeriesObj == null) {
      throw new OpenGammaRuntimeException("Asset price series was null");
    }
    final Set<String> returnCalculatorNames = desiredValue.getConstraints().getValues(ValuePropertyNames.RETURN_CALCULATOR);
    final TimeSeriesReturnCalculator returnCalculator = getTimeSeriesReturnCalculator(returnCalculatorNames);
    final LocalDateDoubleTimeSeries returnSeries = (LocalDateDoubleTimeSeries) returnCalculator.evaluate((LocalDateDoubleTimeSeries) priceSeriesObj);
    final ValueSpecification resultSpec = new ValueSpecification(ValueRequirementNames.PNL_SERIES, target.toSpecification(), desiredValue.getConstraints());
    //final Object result = returnSeries.multiply(fairValue);
    final Object result = returnSeries.multiply(fairValue).multiply(target.getPosition().getQuantity().doubleValue());
    return Collections.singleton(new ComputedValue(resultSpec, result));
  }

  private TimeSeriesReturnCalculator getTimeSeriesReturnCalculator(final Set<String> calculatorNames) {
    if (calculatorNames == null || calculatorNames.isEmpty() || calculatorNames.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique return calculator name: " + calculatorNames);
    }
    return TimeSeriesReturnCalculatorFactory.getReturnCalculator(calculatorNames.iterator().next());
  }
}
