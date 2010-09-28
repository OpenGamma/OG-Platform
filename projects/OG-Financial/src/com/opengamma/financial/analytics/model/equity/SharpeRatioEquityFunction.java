/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

import java.util.Set;

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
import com.opengamma.financial.riskreward.SharpeRatioCalculator;
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
public class SharpeRatioEquityFunction extends AbstractFunction implements FunctionInvoker {
  private static final Logger s_logger = LoggerFactory.getLogger(SharpeRatioEquityFunction.class);
  //private static final String RISK_FREE_RETURN_REFERENCE_TICKER = "US0003M Index"; //TODO remember to get appropriate risk free return (i.e. if frequency is daily then rf = rf / 365)
  private static final double DAYS_PER_YEAR = 365.25; //TODO this should not be hard-coded
  private final TimeSeriesReturnCalculator _returnCalculator;
  private final SharpeRatioCalculator _sharpeRatio;

  //TODO consider schedule for market reference
  public SharpeRatioEquityFunction(final String returnCalculatorName, final String expectedReturnCalculatorName, final String standardDeviationCalculatorName) {
    Validate.notNull(returnCalculatorName, "return calculator name");
    Validate.notNull(expectedReturnCalculatorName, "expected excess return calculator name");
    Validate.notNull(standardDeviationCalculatorName, "standard deviation calculator name");
    _returnCalculator = TimeSeriesReturnCalculatorFactory.getReturnCalculator(returnCalculatorName);
    final Function<double[], Double> expectedExcessReturnCalculator = StatisticsCalculatorFactory.getCalculator(expectedReturnCalculatorName);
    final Function<double[], Double> standardDeviationCalculator = StatisticsCalculatorFactory.getCalculator(standardDeviationCalculatorName);
    _sharpeRatio = new SharpeRatioCalculator(DAYS_PER_YEAR, new DoubleTimeSeriesStatisticsCalculator(expectedExcessReturnCalculator), new DoubleTimeSeriesStatisticsCalculator(
        standardDeviationCalculator));
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) {
    final Position position = target.getPosition();
    final Object assetTSObject = inputs.getValue(new ValueRequirement(ValueRequirementNames.PRICE_SERIES, target.getPosition().getSecurity()));
    if (assetTSObject != null) {
      final DoubleTimeSeries<?> assetPriceTS = (DoubleTimeSeries<?>) assetTSObject;
      DoubleTimeSeries<?> riskFreeRateTS = getRiskFreeRateTS(assetPriceTS);
      DoubleTimeSeries<?> assetReturnTS = _returnCalculator.evaluate(assetPriceTS);
      assetReturnTS = assetReturnTS.intersectionFirstValue(riskFreeRateTS);
      riskFreeRateTS = riskFreeRateTS.intersectionFirstValue(assetReturnTS);
      final double ratio = _sharpeRatio.evaluate(assetReturnTS, riskFreeRateTS);
      s_logger.warn("Sharpe ratio = " + ratio);
      return Sets.newHashSet(new ComputedValue(new ValueSpecification(new ValueRequirement(ValueRequirementNames.SHARPE_RATIO, position), getUniqueIdentifier()), ratio));
    }
    throw new NullPointerException("Could not get both position return series and risk-free series");
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.POSITION && target.getPosition().getSecurity() instanceof EquitySecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueRequirement(ValueRequirementNames.PRICE_SERIES, target.getPosition().getSecurity()));
    }
    return null;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    if (canApplyTo(context, target)) {
      return Sets.newHashSet(new ValueSpecification(new ValueRequirement(ValueRequirementNames.SHARPE_RATIO, target.getPosition()), getUniqueIdentifier()));
    }
    return null;
  }

  @Override
  public String getShortName() {
    return "SharpeRatioFunction";
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
