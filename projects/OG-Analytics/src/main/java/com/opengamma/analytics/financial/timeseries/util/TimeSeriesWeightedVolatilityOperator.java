/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.util;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.timeseries.date.DateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculates a weighted volatility series from a series of absolute or relative returns.
 * A seed period can be defined. In that case, the non-weighted variance/volatility is computed for the 
 * number of returns indicated in the seed length. That volatility is used as the starting point of the volatility
 * computation. The length of the output is reduced by the number of seed.
 * The length of the output time series is (Return TS length) - (seed length) + 1.
 */
public final class TimeSeriesWeightedVolatilityOperator 
    extends Function1D<DateDoubleTimeSeries<?>, DateDoubleTimeSeries<?>> {

  /** Default seed length: 0*/
  private static final int DEFAULT_SEED_LENGTH = 0;
  /** Return operator as relative return on one period. */
  private static final TimeSeriesPercentageChangeOperator RELATIVE_CHANGE = new TimeSeriesPercentageChangeOperator();
  /** Return operator as absolute return on one period. */
  private static final TimeSeriesDifferenceOperator ABSOLUTE_CHANGE = new TimeSeriesDifferenceOperator();
  
  private final Function1D<DateDoubleTimeSeries<?>, DateDoubleTimeSeries<?>> _changeOperator;
  /** The weight used for the Exponentially Weighted Moving Average computation. */
  private final double _lambda;
  /** The length of the seed period. In the seed period, the variance is computed with equal weight. */
  private final int _seedLength;
  
  /**
   * Constructor with a generic return operator and the weight.
   * @param returnOperator The return operator for time series.
   * @param lambda The weight of the exponentially weighted moving average.
   * @param seedLength The length of the seed period. In the seed period, the variance is computed with equal weight.
   */
  public TimeSeriesWeightedVolatilityOperator(
      Function1D<DateDoubleTimeSeries<?>, DateDoubleTimeSeries<?>> returnOperator, 
      double lambda,
      int seedLength) {
    _changeOperator = returnOperator;
    ArgumentChecker.isTrue(lambda > 0.0d, "lambda should be > 0");
    ArgumentChecker.isTrue(lambda < 1.0d, "lambda should be < 1");
    _lambda = lambda;
    _seedLength = seedLength;
  }
  
  @Override
  public DateDoubleTimeSeries<?> evaluate(DateDoubleTimeSeries<?> ts) {
    ArgumentChecker.notNull(ts, "time series");
    ArgumentChecker.isTrue(ts.size() > _seedLength, "time series length must be > ", _seedLength);
    DateDoubleTimeSeries<?> returnSeries = _changeOperator.evaluate(ts);
    int n = returnSeries.size();
    int[] returnTimes = returnSeries.timesArrayFast();
    int seedLengthAdjusted = Math.max(_seedLength, 1); 
    // When seed part is 0, the first variance is computed as the square of the first return. This is the same
    // as a seed part of length 1. 
    // Seed part
    double seedVariance = 0.0;
    for (int i = 0; i < seedLengthAdjusted; i++) {
      double returnTs = returnSeries.getValueAtIndexFast(i);
      seedVariance += returnTs * returnTs;
    }
    seedVariance /= seedLengthAdjusted;
    // EWMA part
    int outputLength = n - seedLengthAdjusted + 1;
    double[] weightedVolatilities = new double[outputLength];
    int[] volatilityTimes = new int[outputLength];
    weightedVolatilities[0] = Math.sqrt(seedVariance);
    volatilityTimes[0] = returnTimes[seedLengthAdjusted - 1];
    double ewmaVariance = seedVariance;
    for (int i = 0; i < outputLength - 1; i++) {
      double returnTs = returnSeries.getValueAtIndexFast(i + seedLengthAdjusted);
      ewmaVariance = _lambda * ewmaVariance + (1 - _lambda) * returnTs * returnTs;
      weightedVolatilities[i + 1] = Math.sqrt(ewmaVariance);
      volatilityTimes[i + 1] = returnTimes[i + seedLengthAdjusted];
    }
    return ImmutableLocalDateDoubleTimeSeries.of(volatilityTimes, weightedVolatilities);
  }
  
  /**
   * Calculates weighted volatilities using the relative difference series and the default lag of 1 period in the 
   * return computation and no seed period (seed length = 0).
   * @param lambda lambda value to apply
   * @return a TimeSeriesWeightedVolatilityOperator instance
   */
  public static TimeSeriesWeightedVolatilityOperator relative(double lambda) {
    return new TimeSeriesWeightedVolatilityOperator(RELATIVE_CHANGE, lambda, DEFAULT_SEED_LENGTH);
  }

  /**
   * Calculates weighted volatilities using the absolute difference series and the default lag of 1 period in the 
   * return computation and no seed period (seed length = 0).
   * @param lambda lambda value to apply
   * @return a TimeSeriesWeightedVolatilityOperator instance
   */
  public static TimeSeriesWeightedVolatilityOperator absolute(double lambda) {
    return new TimeSeriesWeightedVolatilityOperator(ABSOLUTE_CHANGE, lambda, DEFAULT_SEED_LENGTH);
  }

}
