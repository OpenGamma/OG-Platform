/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import java.util.HashSet;
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
import com.opengamma.financial.riskreward.TreynorRatioCalculator;
import com.opengamma.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.id.ExternalId;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames;
import com.opengamma.math.function.Function;
import com.opengamma.math.statistics.descriptive.StatisticsCalculatorFactory;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public abstract class TreynorRatioFunction extends AbstractFunction.NonCompiledInvoker {

  private static final double DAYS_PER_YEAR = 365.25; //TODO
  private final TreynorRatioCalculator _treynorRatio;
  private final LocalDate _startDate;

  public TreynorRatioFunction(final String expectedAssetReturnCalculatorName, final String expectedRiskFreeReturnCalculatorName, final String startDate) {
    Validate.notNull(expectedAssetReturnCalculatorName, "expected excess return calculator name");
    Validate.notNull(expectedRiskFreeReturnCalculatorName, "standard deviation calculator name");
    Validate.notNull(startDate, "start date");
    final Function<double[], Double> expectedExcessReturnCalculator = StatisticsCalculatorFactory.getCalculator(expectedAssetReturnCalculatorName);
    final Function<double[], Double> expectedRiskFreeReturnCalculator = StatisticsCalculatorFactory.getCalculator(expectedRiskFreeReturnCalculatorName);
    _treynorRatio = new TreynorRatioCalculator(new DoubleTimeSeriesStatisticsCalculator(expectedExcessReturnCalculator), new DoubleTimeSeriesStatisticsCalculator(
        expectedRiskFreeReturnCalculator));
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
    final HistoricalTimeSeries riskFreeRateTSObject = historicalSource.getHistoricalTimeSeries(
        HistoricalTimeSeriesFields.LAST_PRICE, bundle.getCAPMRiskFreeRate(), null, HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME, _startDate, true, now, false);
    if (riskFreeRateTSObject == null) {
      throw new NullPointerException("Risk free rate series was null");
    }
    final Object assetPnLObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.PNL_SERIES, positionOrNode)); //TODO replace with return series when portfolio weights are in
    if (assetPnLObject == null) {
      throw new NullPointerException("Asset P&L was null");
    }
    final Object assetFairValueObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.FAIR_VALUE, positionOrNode));
    if (assetFairValueObject == null) {
      throw new NullPointerException("Asset fair value was null");
    }
    final Object betaObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.CAPM_BETA, positionOrNode));
    if (betaObject == null) {
      throw new NullPointerException("Beta was null");
    }
    final double beta = (Double) betaObject;
    final double fairValue = (Double) assetFairValueObject;
    DoubleTimeSeries<?> assetReturnTS = ((DoubleTimeSeries<?>) assetPnLObject).divide(fairValue);
    DoubleTimeSeries<?> riskFreeReturnTS = riskFreeRateTSObject.getTimeSeries().divide(100 * DAYS_PER_YEAR);
    assetReturnTS = assetReturnTS.intersectionFirstValue(riskFreeReturnTS);
    riskFreeReturnTS = riskFreeReturnTS.intersectionFirstValue(assetReturnTS);
    final double ratio = _treynorRatio.evaluate(assetReturnTS, riskFreeReturnTS, beta);
    return Sets.newHashSet(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.TREYNOR_RATIO, positionOrNode), getUniqueId()), ratio));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    if (canApplyTo(context, target)) {
      final Object positionOrNode = getTarget(target);
      final Set<ValueRequirement> result = new HashSet<ValueRequirement>();
      result.add(new ValueRequirement(ValueRequirementNames.PNL_SERIES, positionOrNode));
      result.add(new ValueRequirement(ValueRequirementNames.FAIR_VALUE, positionOrNode));
      result.add(new ValueRequirement(ValueRequirementNames.CAPM_BETA, positionOrNode));
      return result;
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      final Object positionOrNode = getTarget(target);
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.TREYNOR_RATIO, positionOrNode), getUniqueId()));
    }
    return null;
  }

  public abstract Object getTarget(ComputationTarget target);
}
