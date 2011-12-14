/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.var;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTarget;
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
import com.opengamma.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.financial.var.NormalLinearVaRCalculator;
import com.opengamma.math.function.Function;
import com.opengamma.math.statistics.descriptive.StatisticsCalculatorFactory;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public abstract class NormalHistoricalVaRFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final String currency = getCurrency(inputs);
    final Object pnlSeriesObj = inputs.getValue(ValueRequirementNames.PNL_SERIES);
    if (pnlSeriesObj == null) {
      throw new OpenGammaRuntimeException("Could not get P&L series for " + target);
    }
    final DoubleTimeSeries<?> pnlSeries = (DoubleTimeSeries<?>) pnlSeriesObj;
    if (pnlSeries.isEmpty()) {
      throw new OpenGammaRuntimeException("P&L series for " + target + " was empty");
    }
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final Set<String> scheduleCalculatorNames = desiredValue.getConstraints().getValues(ValuePropertyNames.SCHEDULE_CALCULATOR);
    final Set<String> meanCalculatorNames = desiredValue.getConstraints().getValues(ValuePropertyNames.MEAN_CALCULATOR);
    final Set<String> stdDevCalculatorNames = desiredValue.getConstraints().getValues(ValuePropertyNames.STD_DEV_CALCULATOR);
    final Set<String> confidenceLevelNames = desiredValue.getConstraints().getValues(ValuePropertyNames.CONFIDENCE_LEVEL);
    final Set<String> horizonNames = desiredValue.getConstraints().getValues(ValuePropertyNames.HORIZON);
    final Function<DoubleTimeSeries<?>, Double> varCalculator = getVaRCalculator(scheduleCalculatorNames, meanCalculatorNames, stdDevCalculatorNames, horizonNames, confidenceLevelNames);
    final double var = varCalculator.evaluate(pnlSeries);
    final ValueProperties resultProperties = getResultProperties(currency, desiredValues.iterator().next());
    final ValueRequirement vr = new ValueRequirement(ValueRequirementNames.HISTORICAL_VAR, getTarget(target), resultProperties);
    return Sets.newHashSet(new ComputedValue(new ValueSpecification(vr, getUniqueId()), var));
  }

  private String getCurrency(final FunctionInputs inputs) {
    String currency = null;
    for (ComputedValue value : inputs.getAllValues()) {
      currency = value.getSpecification().getProperty(ValuePropertyNames.CURRENCY);
      if (currency != null) {
        break;
      }
    }
    return currency;
  }

  private String getCurrency(Map<ValueSpecification, ValueRequirement> inputs) {
    String currency = null;
    for (Entry<ValueSpecification, ValueRequirement> entry : inputs.entrySet()) {
      currency = entry.getKey().getProperty(ValuePropertyNames.CURRENCY);
      if (currency != null) {
        break;
      }
    }
    return currency;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {
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
      final ValueProperties properties = ValueProperties.builder()
        .with(ValuePropertyNames.SAMPLING_PERIOD, samplingPeriodName.iterator().next())
        .with(ValuePropertyNames.SCHEDULE_CALCULATOR, scheduleCalculatorName.iterator().next())
        .with(ValuePropertyNames.SAMPLING_FUNCTION, samplingFunctionName.iterator().next())
        //.withAny(ValuePropertyNames.RETURN_CALCULATOR)
        .withAny(ValuePropertyNames.CURRENCY).get();
      return Sets.newHashSet(new ValueRequirement(ValueRequirementNames.PNL_SERIES, getTarget(target), properties));
    }
    return null;
  }
  

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      final ValueProperties properties = createValueProperties()
        .withAny(ValuePropertyNames.CURRENCY)
        .withAny(ValuePropertyNames.SAMPLING_PERIOD)
        .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
        .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
        .withAny(ValuePropertyNames.MEAN_CALCULATOR)
        .withAny(ValuePropertyNames.STD_DEV_CALCULATOR)
        .withAny(ValuePropertyNames.CONFIDENCE_LEVEL)
        .withAny(ValuePropertyNames.HORIZON).get();
      return Sets.newHashSet(new ValueSpecification(
          new ValueRequirement(ValueRequirementNames.HISTORICAL_VAR, getTarget(target), properties), getUniqueId()));
    }
    return null;
  }
  
  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target, Map<ValueSpecification, ValueRequirement> inputs) {
    if (canApplyTo(context, target)) {
      String currency = getCurrency(inputs);
      if (currency == null) {
        return null;
      }
      final ValueProperties properties = getResultProperties(currency);
      return Sets.newHashSet(new ValueSpecification(
          new ValueRequirement(ValueRequirementNames.HISTORICAL_VAR, getTarget(target), properties), getUniqueId()));
    }
    return null;
  }

  private ValueProperties getResultProperties(final String currency) {
    final ValueProperties properties = createValueProperties()
      .with(ValuePropertyNames.CURRENCY, currency)
      .withAny(ValuePropertyNames.SAMPLING_PERIOD)
      .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
      .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
      .withAny(ValuePropertyNames.MEAN_CALCULATOR)
      .withAny(ValuePropertyNames.STD_DEV_CALCULATOR)
      .withAny(ValuePropertyNames.CONFIDENCE_LEVEL)
      .withAny(ValuePropertyNames.HORIZON).get();
    return properties;
  }

  private ValueProperties getResultProperties(final String currency, final ValueRequirement desiredValue) {
    final ValueProperties properties = createValueProperties()
      .with(ValuePropertyNames.CURRENCY, currency)
      .with(ValuePropertyNames.SAMPLING_PERIOD, desiredValue.getConstraint(ValuePropertyNames.SAMPLING_PERIOD))
      .with(ValuePropertyNames.SCHEDULE_CALCULATOR, desiredValue.getConstraint(ValuePropertyNames.SCHEDULE_CALCULATOR))
      .with(ValuePropertyNames.SAMPLING_FUNCTION, desiredValue.getConstraint(ValuePropertyNames.SAMPLING_FUNCTION))
      .with(ValuePropertyNames.MEAN_CALCULATOR, desiredValue.getConstraint(ValuePropertyNames.MEAN_CALCULATOR))
      .with(ValuePropertyNames.STD_DEV_CALCULATOR, desiredValue.getConstraint(ValuePropertyNames.STD_DEV_CALCULATOR))
      .with(ValuePropertyNames.CONFIDENCE_LEVEL, desiredValue.getConstraint(ValuePropertyNames.CONFIDENCE_LEVEL))
      .with(ValuePropertyNames.HORIZON, desiredValue.getConstraint(ValuePropertyNames.HORIZON)).get();
    return properties;
  }
  
  protected abstract Object getTarget(final ComputationTarget target);
  
  private Function<DoubleTimeSeries<?>, Double> getVaRCalculator(final Set<String> scheduleCalculatorNames, final Set<String> meanCalculatorNames,
      final Set<String> stdDevCalculatorNames, final Set<String> horizonNames, final Set<String> confidenceLevelNames) {
    if (scheduleCalculatorNames == null || scheduleCalculatorNames.isEmpty() || scheduleCalculatorNames.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique schedule calculator name: " + scheduleCalculatorNames);
    }
    if (meanCalculatorNames == null || meanCalculatorNames.isEmpty() || meanCalculatorNames.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique mean calculator name: " + meanCalculatorNames);
    }
    if (stdDevCalculatorNames == null || stdDevCalculatorNames.isEmpty() || stdDevCalculatorNames.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique standard deviation calculator name: " + stdDevCalculatorNames);
    }
    if (horizonNames == null || horizonNames.isEmpty() || horizonNames.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique horizon name: " + horizonNames);
    }
    if (confidenceLevelNames == null || confidenceLevelNames.isEmpty() || confidenceLevelNames.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique confidence level name: " + confidenceLevelNames);
    }
    final DoubleTimeSeriesStatisticsCalculator meanCalculator = 
      new DoubleTimeSeriesStatisticsCalculator(StatisticsCalculatorFactory.getCalculator(meanCalculatorNames.iterator().next()));
    final DoubleTimeSeriesStatisticsCalculator stdDevCalculator = 
      new DoubleTimeSeriesStatisticsCalculator(StatisticsCalculatorFactory.getCalculator(stdDevCalculatorNames.iterator().next()));
    return new NormalLinearVaRCalculator<DoubleTimeSeries<?>>(Double.valueOf(horizonNames.iterator().next()), 
        VaRFunctionUtils.getPeriodsPerYear(scheduleCalculatorNames.iterator().next()), Double.valueOf(confidenceLevelNames.iterator().next()), 
        meanCalculator, stdDevCalculator);

  }
}
