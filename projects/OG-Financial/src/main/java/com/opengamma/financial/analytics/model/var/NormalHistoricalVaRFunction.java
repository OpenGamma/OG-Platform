/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.var;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.analytics.financial.var.NormalLinearVaRCalculator;
import com.opengamma.analytics.financial.var.NormalVaRParameters;
import com.opengamma.analytics.financial.var.VaRCalculationResult;
import com.opengamma.analytics.math.statistics.descriptive.StatisticsCalculatorFactory;
import com.opengamma.engine.ComputationTarget;
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
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class NormalHistoricalVaRFunction extends AbstractFunction.NonCompiledInvoker {

  /**
   * The name for the normal historical VaR calculation method
   */
  public static final String NORMAL_VAR = "Normal";

  /**
   * The property for the VaR distribution type
   */
  public static final String PROPERTY_VAR_DISTRIBUTION = "VaRDistributionType";
  /**
   * The default PnLContribution property value.
   */
  public static final String DEFAULT_PNL_CONTRIBUTIONS = "Delta";

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Object pnlSeriesObj = inputs.getValue(ValueRequirementNames.PNL_SERIES);
    if (pnlSeriesObj == null) {
      throw new OpenGammaRuntimeException("Could not get P&L series for " + target);
    }
    final DoubleTimeSeries<?> pnlSeries = (DoubleTimeSeries<?>) pnlSeriesObj;
    if (pnlSeries.isEmpty()) {
      throw new OpenGammaRuntimeException("P&L series for " + target + " was empty");
    }
    // TODO kirk 2012-06-22 -- See TODO below in getResults().
    // We assume the constraints are all going to be the same for the results.
    // Being more restrictive would change this logic and probably not be desirable
    // but someone other than me should confirm.
    ValueProperties constraints = null;
    boolean computeVar = false;
    boolean computeStddev = false;
    for (final ValueRequirement desiredValue : desiredValues) {
      constraints = (constraints == null) ? desiredValue.getConstraints() : constraints;
      if (ValueRequirementNames.HISTORICAL_VAR.equals(desiredValue.getValueName())) {
        computeVar = true;
      }
      if (ValueRequirementNames.HISTORICAL_VAR_STDDEV.equals(desiredValue.getValueName())) {
        computeStddev = true;
      }
    }
    final Set<String> scheduleCalculatorNames = constraints.getValues(ValuePropertyNames.SCHEDULE_CALCULATOR);
    final Set<String> meanCalculatorNames = constraints.getValues(ValuePropertyNames.MEAN_CALCULATOR);
    final Set<String> stdDevCalculatorNames = constraints.getValues(ValuePropertyNames.STD_DEV_CALCULATOR);
    final Set<String> confidenceLevelNames = constraints.getValues(ValuePropertyNames.CONFIDENCE_LEVEL);
    final Set<String> horizonNames = constraints.getValues(ValuePropertyNames.HORIZON);
    final NormalVaRParameters parameters = getParameters(scheduleCalculatorNames, horizonNames, confidenceLevelNames);
    final NormalLinearVaRCalculator<DoubleTimeSeries<?>> varCalculator = getVaRCalculator(meanCalculatorNames, stdDevCalculatorNames);
    final VaRCalculationResult calcResult = varCalculator.evaluate(parameters, pnlSeries);
    final double var = calcResult.getVaRValue();
    final double stddev = calcResult.getStdDev();
    final Set<ComputedValue> results = new HashSet<ComputedValue>();
    if (computeVar) {
      results.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.HISTORICAL_VAR, target.toSpecification(), constraints), var));
    }
    if (computeStddev) {
      results.add(new ComputedValue(new ValueSpecification(ValueRequirementNames.HISTORICAL_VAR_STDDEV, target.toSpecification(), constraints), stddev));
    }
    return results;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .withAny(ValuePropertyNames.CURRENCY)
        .withAny(ValuePropertyNames.SAMPLING_PERIOD)
        .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
        .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
        .withAny(ValuePropertyNames.MEAN_CALCULATOR)
        .withAny(ValuePropertyNames.STD_DEV_CALCULATOR)
        .withAny(ValuePropertyNames.CONFIDENCE_LEVEL)
        .withAny(ValuePropertyNames.HORIZON)
        .withAny(ValuePropertyNames.AGGREGATION)
        .withAny(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS)
        .with(PROPERTY_VAR_DISTRIBUTION, NORMAL_VAR).get();
    final ValueSpecification hVaRSpec = new ValueSpecification(ValueRequirementNames.HISTORICAL_VAR, target.toSpecification(), properties);
    // TODO kirk 2012-06-22 -- These are certainly not the optimal properties. Rather,
    // things like CONFIDENCE_LEVEL actually depend on the stddev, so this doesn't make
    // 100% of sense. However, in an effort to make this the simplest addition possible
    // I'm just reusing the identical properties.
    final ValueSpecification stddevSpec = new ValueSpecification(ValueRequirementNames.HISTORICAL_VAR_STDDEV, target.toSpecification(), properties);
    return Sets.newHashSet(hVaRSpec, stddevSpec);
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
    final Set<String> meanCalculatorName = constraints.getValues(ValuePropertyNames.MEAN_CALCULATOR);
    if (meanCalculatorName == null || meanCalculatorName.size() != 1) {
      return null;
    }
    final Set<String> stdDevCalculatorName = constraints.getValues(ValuePropertyNames.STD_DEV_CALCULATOR);
    if (stdDevCalculatorName == null || stdDevCalculatorName.size() != 1) {
      return null;
    }
    final Set<String> pnlContributionNames = constraints.getValues(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS);
    if (pnlContributionNames != null && pnlContributionNames.size() != 1) {
      return null;
    }
    String pnlContributionName = pnlContributionNames != null ? pnlContributionNames.iterator().next() : DEFAULT_PNL_CONTRIBUTIONS;
    final ValueProperties.Builder properties = ValueProperties.builder()
        .with(ValuePropertyNames.SCHEDULE_CALCULATOR, scheduleCalculatorName.iterator().next())
        .with(ValuePropertyNames.SAMPLING_FUNCTION, samplingFunctionName.iterator().next())
        .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, pnlContributionName); //TODO
    if (desiredValue.getConstraint(HistoricalTimeSeriesFunctionUtils.START_DATE_PROPERTY) == null) {
      properties.with(ValuePropertyNames.SAMPLING_PERIOD, samplingPeriodName.iterator().next());
    }
    copyOptional(desiredValue.getConstraints(), properties);
    final Set<String> desiredCurrencyValues = desiredValue.getConstraints().getValues(ValuePropertyNames.CURRENCY);
    if (desiredCurrencyValues == null || desiredCurrencyValues.isEmpty()) {
      properties.withAny(ValuePropertyNames.CURRENCY);
    } else {
      properties.with(ValuePropertyNames.CURRENCY, desiredCurrencyValues);
    }
    final Set<String> aggregationStyle = constraints.getValues(ValuePropertyNames.AGGREGATION);
    if (aggregationStyle != null) {
      if (aggregationStyle.isEmpty()) {
        properties.withOptional(ValuePropertyNames.AGGREGATION);
      } else {
        if (constraints.isOptional(ValuePropertyNames.AGGREGATION)) {
          properties.withOptional(ValuePropertyNames.AGGREGATION);
        }
        properties.with(ValuePropertyNames.AGGREGATION, aggregationStyle);
      }
    }
    return Collections.singleton(new ValueRequirement(ValueRequirementNames.PNL_SERIES, target.toSpecification(), properties.get()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueSpecification input = inputs.keySet().iterator().next();
    final String currency = input.getProperty(ValuePropertyNames.CURRENCY);
    if (currency == null) {
      return null;
    }
    final ValueProperties properties = getResultProperties(
        input.getProperties(), currency, input.getProperty(ValuePropertyNames.AGGREGATION), input.getProperty(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS));
    // see note above in other getResults().
    final ValueSpecification varSpecification = new ValueSpecification(ValueRequirementNames.HISTORICAL_VAR, target.toSpecification(), properties);
    final ValueSpecification stddevSpecification = new ValueSpecification(ValueRequirementNames.HISTORICAL_VAR_STDDEV, target.toSpecification(), properties);
    return Sets.newHashSet(varSpecification, stddevSpecification);
  }

  private ValueProperties getResultProperties(ValueProperties priceTsProperties, final String currency, final String aggregationStyle, final String pnlContribution) {
    final ValueProperties.Builder properties = createValueProperties()
        .with(ValuePropertyNames.CURRENCY, currency)
        .withAny(ValuePropertyNames.SAMPLING_PERIOD)
        .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
        .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
        .withAny(ValuePropertyNames.MEAN_CALCULATOR)
        .withAny(ValuePropertyNames.STD_DEV_CALCULATOR)
        .withAny(ValuePropertyNames.CONFIDENCE_LEVEL)
        .withAny(ValuePropertyNames.HORIZON)
        .with(ValuePropertyNames.PROPERTY_PNL_CONTRIBUTIONS, pnlContribution)
        .with(PROPERTY_VAR_DISTRIBUTION, NORMAL_VAR);
    copyOptional(priceTsProperties, properties);
    if (aggregationStyle != null) {
      properties.with(ValuePropertyNames.AGGREGATION, aggregationStyle);
    }
    return properties.get();
  }

  private NormalVaRParameters getParameters(final Set<String> scheduleCalculatorNames, final Set<String> horizonNames, final Set<String> confidenceLevelNames) {
    if (scheduleCalculatorNames == null || scheduleCalculatorNames.isEmpty() || scheduleCalculatorNames.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique schedule calculator name: " + scheduleCalculatorNames);
    }
    if (horizonNames == null || horizonNames.isEmpty() || horizonNames.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique horizon name: " + horizonNames);
    }
    if (confidenceLevelNames == null || confidenceLevelNames.isEmpty() || confidenceLevelNames.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique confidence level name: " + confidenceLevelNames);
    }
    return new NormalVaRParameters(Double.valueOf(horizonNames.iterator().next()),
        VaRFunctionUtils.getBusinessDaysPerPeriod(scheduleCalculatorNames.iterator().next()), Double.valueOf(confidenceLevelNames.iterator().next()));
  }

  private NormalLinearVaRCalculator<DoubleTimeSeries<?>> getVaRCalculator(final Set<String> meanCalculatorNames,
      final Set<String> stdDevCalculatorNames) {
    if (meanCalculatorNames == null || meanCalculatorNames.isEmpty() || meanCalculatorNames.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique mean calculator name: " + meanCalculatorNames);
    }
    if (stdDevCalculatorNames == null || stdDevCalculatorNames.isEmpty() || stdDevCalculatorNames.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique standard deviation calculator name: " + stdDevCalculatorNames);
    }
    final DoubleTimeSeriesStatisticsCalculator meanCalculator =
        new DoubleTimeSeriesStatisticsCalculator(StatisticsCalculatorFactory.getCalculator(meanCalculatorNames.iterator().next()));
    final DoubleTimeSeriesStatisticsCalculator stdDevCalculator =
        new DoubleTimeSeriesStatisticsCalculator(StatisticsCalculatorFactory.getCalculator(stdDevCalculatorNames.iterator().next()));
    return new NormalLinearVaRCalculator<DoubleTimeSeries<?>>(meanCalculator, stdDevCalculator);
  }
  
  private ValueProperties.Builder copyOptional(ValueProperties origProps, ValueProperties.Builder propBuilder) {
    for (String prop: origProps.getProperties()) {
      if (origProps.isOptional(prop)) {
        propBuilder.withOptional(prop).with(prop, origProps.getSingleValue(prop));
      }
    }
    return propBuilder;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PORTFOLIO_NODE.or(ComputationTargetType.POSITION);
  }
}
