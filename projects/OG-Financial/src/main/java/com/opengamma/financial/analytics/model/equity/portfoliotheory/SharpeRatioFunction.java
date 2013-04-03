/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.portfoliotheory;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.riskreward.SharpeRatioCalculator;
import com.opengamma.analytics.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.analytics.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.analytics.financial.timeseries.returns.TimeSeriesReturnCalculatorFactory;
import com.opengamma.analytics.math.statistics.descriptive.StatisticsCalculatorFactory;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
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
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.id.ExternalId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.TimeSeriesIntersector;
import com.opengamma.util.ArgumentChecker;

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
  public boolean canApplyTo(final FunctionCompilationContext compilationContext, final ComputationTarget target) {
    return true;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) {
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final ValueProperties constraints = desiredValue.getConstraints();
    final HistoricalTimeSeries benchmarkTSObject = (HistoricalTimeSeries) inputs.getValue(ValueRequirementNames.HISTORICAL_TIME_SERIES);
    final Object assetPnLObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.PNL_SERIES, targetSpec)); //TODO replace with return series when portfolio weights are in
    if (assetPnLObject == null) {
      throw new OpenGammaRuntimeException("Asset P&L series was null");
    }
    final Object assetFairValueObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.FAIR_VALUE, targetSpec));
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
    return Sets.newHashSet(new ComputedValue(new ValueSpecification(ValueRequirementNames.SHARPE_RATIO, targetSpec, resultProperties), ratio));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> samplingPeriodNames = constraints.getValues(ValuePropertyNames.SAMPLING_PERIOD);
    if (samplingPeriodNames == null || samplingPeriodNames.size() != 1) {
      return null;
    }
    final String samplingPeriodName = samplingPeriodNames.iterator().next();
    final Set<String> scheduleCalculatorNames = constraints.getValues(ValuePropertyNames.SCHEDULE_CALCULATOR);
    if (scheduleCalculatorNames == null || scheduleCalculatorNames.size() != 1) {
      return null;
    }
    final Set<String> samplingFunctionNames = constraints.getValues(ValuePropertyNames.SAMPLING_FUNCTION);
    if (samplingFunctionNames == null || samplingFunctionNames.size() != 1) {
      return null;
    }
    final Set<String> returnCalculatorNames = constraints.getValues(ValuePropertyNames.RETURN_CALCULATOR);
    if (returnCalculatorNames == null || returnCalculatorNames.size() != 1) {
      return null;
    }
    final ComputationTargetSpecification targetSpec = target.toSpecification();
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    requirements.add(new ValueRequirement(ValueRequirementNames.PNL_SERIES, targetSpec, ValueProperties.builder()
        .withAny(ValuePropertyNames.CURRENCY)
        .with(ValuePropertyNames.SAMPLING_PERIOD, samplingPeriodName)
        .with(ValuePropertyNames.SCHEDULE_CALCULATOR, scheduleCalculatorNames.iterator().next())
        .with(ValuePropertyNames.SAMPLING_FUNCTION, samplingFunctionNames.iterator().next())
        .with(ValuePropertyNames.RETURN_CALCULATOR, returnCalculatorNames.iterator().next()).get()));
    requirements.add(new ValueRequirement(ValueRequirementNames.FAIR_VALUE, targetSpec));
    final HistoricalTimeSeriesResolver resolver = OpenGammaCompilationContext.getHistoricalTimeSeriesResolver(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext.getConventionBundleSource(context);
    final ConventionBundle bundle = conventionSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "USD_CAPM"));
    final HistoricalTimeSeriesResolutionResult timeSeries = resolver.resolve(bundle.getCAPMMarket(), null, null, null, MarketDataRequirementNames.MARKET_VALUE, _resolutionKey);
    if (timeSeries == null) {
      return null;
    }
    requirements.add(HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries, MarketDataRequirementNames.MARKET_VALUE,
        DateConstraint.VALUATION_TIME.minus(samplingPeriodName), true, DateConstraint.VALUATION_TIME, true));
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueSpecification(ValueRequirementNames.SHARPE_RATIO, target.toSpecification(), getResultProperties()));
    }
    return null;
  }

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
