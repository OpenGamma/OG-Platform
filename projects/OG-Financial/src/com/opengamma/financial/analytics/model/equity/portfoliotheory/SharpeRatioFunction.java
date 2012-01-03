/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.portfoliotheory;

import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;
import javax.time.calendar.Period;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesFields;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
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
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.riskreward.SharpeRatioCalculator;
import com.opengamma.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculatorFactory;
import com.opengamma.id.ExternalId;
import com.opengamma.math.statistics.descriptive.StatisticsCalculatorFactory;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.TimeSeriesIntersector;

/**
 * 
 */
public abstract class SharpeRatioFunction extends AbstractFunction.NonCompiledInvoker {
  private static final double WORKING_DAYS_PER_YEAR = 252; //TODO this should not be hard-coded
  private final String _resolutionKey;

  public SharpeRatioFunction(final String resolutionKey) {
    ArgumentChecker.notNull(resolutionKey, "resolution key");
    _resolutionKey = resolutionKey;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) {
    final Object positionOrNode = getTarget(target);
    final ConventionBundleSource conventionSource = OpenGammaExecutionContext.getConventionBundleSource(executionContext);
    final ConventionBundle bundle = conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD_CAPM"));
    final Clock snapshotClock = executionContext.getValuationClock();
    final LocalDate now = snapshotClock.zonedDateTime().toLocalDate();
    final HistoricalTimeSeriesSource historicalSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final ValueProperties constraints = desiredValue.getConstraints();
    final Period samplingPeriod = getSamplingPeriod(constraints.getValues(ValuePropertyNames.SAMPLING_PERIOD));
    final LocalDate startDate = now.minus(samplingPeriod);
    final HistoricalTimeSeries benchmarkTSObject = historicalSource.getHistoricalTimeSeries(
        HistoricalTimeSeriesFields.LAST_PRICE, bundle.getCAPMMarket(), _resolutionKey, startDate, true, now, true);
    if (benchmarkTSObject == null) {
      throw new OpenGammaRuntimeException("Benchmark time series was null");
    }
    final Object assetPnLObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.PNL_SERIES, positionOrNode)); //TODO replace with return series when portfolio weights are in
    if (assetPnLObject == null) {
      throw new OpenGammaRuntimeException("Asset P&L series was null");
    }
    final Object assetFairValueObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.FAIR_VALUE, positionOrNode));
    if (assetFairValueObject == null) {
      throw new OpenGammaRuntimeException("Asset fair value was null");
    }
    final double fairValue = (Double) assetFairValueObject;
    DoubleTimeSeries<?> assetReturnTS = ((DoubleTimeSeries<?>) assetPnLObject).divide(fairValue);
    final TimeSeriesReturnCalculator returnCalculator = getReturnCalculator(constraints.getValues(ValuePropertyNames.RETURN_CALCULATOR));
    DoubleTimeSeries<?> benchmarkReturnTS = returnCalculator.evaluate(benchmarkTSObject.getTimeSeries());
    DoubleTimeSeries<?>[] series = TimeSeriesIntersector.intersect(assetReturnTS, benchmarkReturnTS);
    assetReturnTS = series[0];
    benchmarkReturnTS = series[1];
    final SharpeRatioCalculator calculator = getCalculator(constraints.getValues(ValuePropertyNames.EXCESS_RETURN_CALCULATOR),
        constraints.getValues(ValuePropertyNames.STD_DEV_CALCULATOR));
    final double ratio = calculator.evaluate(assetReturnTS, benchmarkReturnTS);
    final ValueProperties resultProperties = getResultProperties(desiredValues.iterator().next());
    return Sets.newHashSet(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.SHARPE_RATIO, positionOrNode, resultProperties), getUniqueId()), ratio));
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
      final Set<String> returnCalculatorName = constraints.getValues(ValuePropertyNames.RETURN_CALCULATOR);
      if (returnCalculatorName == null || returnCalculatorName.size() != 1) {
        return null;
      }
      final ValueProperties pnlSeriesProperties = ValueProperties.builder()
        .withAny(ValuePropertyNames.CURRENCY)
        .with(ValuePropertyNames.SAMPLING_PERIOD, samplingPeriodName.iterator().next())
        .with(ValuePropertyNames.SCHEDULE_CALCULATOR, scheduleCalculatorName.iterator().next())
        .with(ValuePropertyNames.SAMPLING_FUNCTION, samplingFunctionName.iterator().next())
        .with(ValuePropertyNames.RETURN_CALCULATOR, returnCalculatorName.iterator().next()).get();
      final Object positionOrNode = getTarget(target);
      return Sets.newHashSet(new ValueRequirement(ValueRequirementNames.PNL_SERIES, positionOrNode, pnlSeriesProperties), 
                             new ValueRequirement(ValueRequirementNames.FAIR_VALUE, positionOrNode));
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      final Object positionOrNode = getTarget(target);
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.SHARPE_RATIO, positionOrNode, getResultProperties()), getUniqueId()));
    }
    return null;
  }

  public abstract Object getTarget(ComputationTarget target);
  
  private ValueProperties getResultProperties() {
    return createValueProperties()
      .withAny(ValuePropertyNames.SAMPLING_PERIOD)
      .withAny(ValuePropertyNames.SCHEDULE_CALCULATOR)
      .withAny(ValuePropertyNames.SAMPLING_FUNCTION)
      .withAny(ValuePropertyNames.RETURN_CALCULATOR)
      .withAny(ValuePropertyNames.STD_DEV_CALCULATOR)
      .withAny(ValuePropertyNames.EXCESS_RETURN_CALCULATOR).get();
  }
  
  private ValueProperties getResultProperties(final ValueRequirement desiredValue) {
    return createValueProperties()
      .with(ValuePropertyNames.SAMPLING_PERIOD, desiredValue.getConstraint(ValuePropertyNames.SAMPLING_PERIOD))
      .with(ValuePropertyNames.SCHEDULE_CALCULATOR, desiredValue.getConstraint(ValuePropertyNames.SCHEDULE_CALCULATOR))
      .with(ValuePropertyNames.SAMPLING_FUNCTION, desiredValue.getConstraint(ValuePropertyNames.SAMPLING_FUNCTION))
      .with(ValuePropertyNames.RETURN_CALCULATOR, desiredValue.getConstraint(ValuePropertyNames.RETURN_CALCULATOR))
      .with(ValuePropertyNames.STD_DEV_CALCULATOR, desiredValue.getConstraint(ValuePropertyNames.STD_DEV_CALCULATOR))
      .with(ValuePropertyNames.EXCESS_RETURN_CALCULATOR, desiredValue.getConstraint(ValuePropertyNames.EXCESS_RETURN_CALCULATOR)).get();
  }
  
  private Period getSamplingPeriod(final Set<String> samplingPeriodNames) {
    if (samplingPeriodNames == null || samplingPeriodNames.isEmpty() || samplingPeriodNames.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique sampling period name: " + samplingPeriodNames);
    }  
    return Period.parse(samplingPeriodNames.iterator().next());
  }
  
  private TimeSeriesReturnCalculator getReturnCalculator(final Set<String> returnCalculatorNames) {
    if (returnCalculatorNames == null || returnCalculatorNames.isEmpty() || returnCalculatorNames.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique return calculator name: " + returnCalculatorNames);
    }
    return TimeSeriesReturnCalculatorFactory.getReturnCalculator(returnCalculatorNames.iterator().next());
  }
  
  private SharpeRatioCalculator getCalculator(final Set<String> excessReturnCalculatorNames, final Set<String> stdDevCalculatorNames) {
    if (excessReturnCalculatorNames == null || excessReturnCalculatorNames.isEmpty() || excessReturnCalculatorNames.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique excess return calculator name: " + excessReturnCalculatorNames);
    }
    if (stdDevCalculatorNames == null || stdDevCalculatorNames.isEmpty() || stdDevCalculatorNames.size() != 1) {
      throw new OpenGammaRuntimeException("Missing or non-unique standard deviation calculator name: " + stdDevCalculatorNames);
    }
    final DoubleTimeSeriesStatisticsCalculator excessReturnCalculator = 
      new DoubleTimeSeriesStatisticsCalculator(StatisticsCalculatorFactory.getCalculator(excessReturnCalculatorNames.iterator().next()));
    final DoubleTimeSeriesStatisticsCalculator stdDevCalculator = 
      new DoubleTimeSeriesStatisticsCalculator(StatisticsCalculatorFactory.getCalculator(stdDevCalculatorNames.iterator().next()));
    return new SharpeRatioCalculator(WORKING_DAYS_PER_YEAR, excessReturnCalculator, stdDevCalculator);
  }
}
