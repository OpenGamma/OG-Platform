/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import java.util.Set;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionInvoker;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.riskreward.TreynorRatioCalculator;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculatorFactory;
import com.opengamma.math.function.Function;
import com.opengamma.math.statistics.descriptive.StatisticsCalculatorFactory;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastArrayLongDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * 
 */
public class TreynorRatioEquityFunction extends AbstractFunction implements FunctionInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(TreynorRatioEquityFunction.class);
  //private static final String RISK_FREE_REFERENCE_TICKER;
  private static final double DAYS_PER_YEAR = 365.25;
  private final TimeSeriesReturnCalculator _returnCalculator;
  private final TreynorRatioCalculator _calculator;
  private final LocalDate _startDate;

  public TreynorRatioEquityFunction(final String returnCalculatorName, final String expectedAssetReturnCalculatorName, final String expectedRiskFreeReturnCalculatorName, final String startDate) {
    Validate.notNull(returnCalculatorName, "return calculator name");
    Validate.notNull(expectedAssetReturnCalculatorName, "expected asset return calculator name");
    Validate.notNull(expectedRiskFreeReturnCalculatorName, "expected risk-free return calculator name");
    _returnCalculator = TimeSeriesReturnCalculatorFactory.getReturnCalculator(returnCalculatorName);
    final Function<double[], Double> expectedAssetReturnCalculator = StatisticsCalculatorFactory.getCalculator(expectedAssetReturnCalculatorName);
    final Function<double[], Double> expectedRiskFreeReturnCalculator = StatisticsCalculatorFactory.getCalculator(expectedRiskFreeReturnCalculatorName);
    _calculator = new TreynorRatioCalculator(_returnCalculator, new DoubleTimeSeriesStatisticsCalculator(expectedAssetReturnCalculator), new DoubleTimeSeriesStatisticsCalculator(
        expectedRiskFreeReturnCalculator));
    _startDate = LocalDate.parse(startDate);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Position position = target.getPosition();
    final Object betaObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.CAPM_BETA, position));
    final Object assetTSObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.PRICE_SERIES, position.getSecurity()));
    if (assetTSObject != null && betaObject != null) {
      DoubleTimeSeries<?> riskFreeRateTS = getRiskFreeRateTS((DoubleTimeSeries<?>) assetTSObject);
      DoubleTimeSeries<?> assetTS = (DoubleTimeSeries<?>) assetTSObject;
      final double beta = (Double) betaObject;
      assetTS = assetTS.intersectionFirstValue(riskFreeRateTS);
      riskFreeRateTS = riskFreeRateTS.intersectionFirstValue(assetTS);
      final double ratio = _calculator.evaluate(assetTS, riskFreeRateTS, beta);
      s_logger.warn("Treynor ratio = " + ratio);
      return Sets.newHashSet(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.TREYNOR_RATIO, position), getUniqueIdentifier()), ratio));
    }
    throw new NullPointerException("Could not get asset time series and beta");
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.POSITION && target.getPosition().getSecurity() instanceof EquitySecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueRequirement(ValueRequirementNames.PRICE_SERIES, target.getPosition().getSecurity()), new ValueRequirement(ValueRequirementNames.CAPM_BETA, target.getPosition()));
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.TREYNOR_RATIO, target.getPosition()), getUniqueIdentifier()));
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "TreynorRatioModel";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  private DoubleTimeSeries<?> getRiskFreeRateTS(final DoubleTimeSeries<?> assetPriceTS) {
    final FastLongDoubleTimeSeries ts = assetPriceTS.toFastLongDoubleTimeSeries();
    final long[] times = ts.timesArrayFast();
    final DateTimeNumericEncoding encoding = ts.getEncoding();
    final int n = times.length;
    final double[] values = new double[n];
    Arrays.fill(values, 0.01 / DAYS_PER_YEAR);
    return new FastArrayLongDoubleTimeSeries(encoding, times, values);
  }

}
