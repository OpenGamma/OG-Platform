/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesFields;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
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
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames;
import com.opengamma.math.function.Function;
import com.opengamma.math.statistics.descriptive.StatisticsCalculatorFactory;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public abstract class SharpeRatioFunction extends AbstractFunction.NonCompiledInvoker {

  private static final double WORKING_DAYS_PER_YEAR = 252; //TODO this should not be hard-coded
  private final SharpeRatioCalculator _sharpeRatio;
  private final LocalDate _startDate;
  private final TimeSeriesReturnCalculator _returnCalculator;

  public SharpeRatioFunction(final String returnCalculatorName, final String expectedReturnCalculatorName, final String standardDeviationCalculatorName, final String startDate) {
    Validate.notNull(returnCalculatorName, "return calculator name");
    Validate.notNull(expectedReturnCalculatorName, "expected excess return calculator name");
    Validate.notNull(standardDeviationCalculatorName, "standard deviation calculator name");
    Validate.notNull(startDate, "start date");
    _returnCalculator = TimeSeriesReturnCalculatorFactory.getReturnCalculator(returnCalculatorName);
    final Function<double[], Double> expectedExcessReturnCalculator = StatisticsCalculatorFactory.getCalculator(expectedReturnCalculatorName);
    final Function<double[], Double> standardDeviationCalculator = StatisticsCalculatorFactory.getCalculator(standardDeviationCalculatorName);
    _sharpeRatio = new SharpeRatioCalculator(WORKING_DAYS_PER_YEAR, new DoubleTimeSeriesStatisticsCalculator(expectedExcessReturnCalculator),
        new DoubleTimeSeriesStatisticsCalculator(standardDeviationCalculator));
    _startDate = LocalDate.parse(startDate);
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
    final HistoricalTimeSeries benchmarkTSObject = historicalSource.getHistoricalTimeSeries(
        HistoricalTimeSeriesFields.LAST_PRICE, bundle.getCAPMMarket(), null, HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME, _startDate, true, now, false);
    if (benchmarkTSObject == null) {
      throw new NullPointerException("Benchmark time series was null");
    }
    final Object assetPnLObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.PNL_SERIES, positionOrNode)); //TODO replace with return series when portfolio weights are in
    if (assetPnLObject == null) {
      throw new NullPointerException("Asset P&L series was null");
    }
    final Object assetFairValueObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.FAIR_VALUE, positionOrNode));
    if (assetFairValueObject == null) {
      throw new NullPointerException("Asset fair value was null");
    }
    final double fairValue = (Double) assetFairValueObject;
    DoubleTimeSeries<?> assetReturnTS = ((DoubleTimeSeries<?>) assetPnLObject).divide(fairValue);
    DoubleTimeSeries<?> benchmarkReturnTS = _returnCalculator.evaluate(benchmarkTSObject.getTimeSeries());
    assetReturnTS = assetReturnTS.intersectionFirstValue(benchmarkReturnTS);
    benchmarkReturnTS = benchmarkReturnTS.intersectionFirstValue(assetReturnTS);
    final double ratio = _sharpeRatio.evaluate(assetReturnTS, benchmarkReturnTS);
    return Sets.newHashSet(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.SHARPE_RATIO, positionOrNode), getUniqueId()), ratio));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {
      final Object positionOrNode = getTarget(target);
      return Sets.newHashSet(new ValueRequirement(ValueRequirementNames.PNL_SERIES, positionOrNode), new ValueRequirement(ValueRequirementNames.FAIR_VALUE, positionOrNode));
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      final Object positionOrNode = getTarget(target);
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.SHARPE_RATIO, positionOrNode), getUniqueId()));
    }
    return null;
  }

  public abstract Object getTarget(ComputationTarget target);
}
