/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.covariance;

import java.util.Iterator;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.financial.timeseries.returns.ContinuouslyCompoundedRelativeTimeSeriesReturnCalculator;
import com.opengamma.analytics.financial.timeseries.returns.RelativeTimeSeriesReturnCalculator;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.CalculationMode;

/**
 * The historical volatility of a price time series can be calculated using:
 * $$
 * \begin{eqnarray*}
 * \sigma = \frac{1}{{2 n \sqrt{\ln{2}}}}\sum\limits_{i=1}^n r_i
 * \end{eqnarray*}
 * $$
 * where $r_i$ is the $i^\text{th}$ period *relative* return of the high and
 * low prices of a series, and $n$ is the number of data points in the price
 * series.
 * <p>
 * Although any relative return calculator can be used, to get correct results
 * the calculator should be a {@link ContinuouslyCompoundedRelativeTimeSeriesReturnCalculator}.
 */
public class HistoricalVolatilityHighLowCalculator extends HistoricalVolatilityCalculator {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(HistoricalVolatilityHighLowCalculator.class);
  /** The relative return series calculator */
  private final RelativeTimeSeriesReturnCalculator _returnCalculator;

  /**
   * Creates a historical volatility calculator with the given relative return calculation method and default values for the calculation mode and allowable percentage of bad data points
   * @param returnCalculator The return calculator, not null
   */
  public HistoricalVolatilityHighLowCalculator(final RelativeTimeSeriesReturnCalculator returnCalculator) {
    super();
    ArgumentChecker.notNull(returnCalculator, "return calculator");
    _returnCalculator = returnCalculator;
  }

  /**
   * Creates a historical volatility calculator with the given relative return calculation method and calculation mode and the default value for the allowable percentage of bad data points
   * @param returnCalculator The return calculator, not null
   * @param mode The calculation mode, not null
   */
  public HistoricalVolatilityHighLowCalculator(final RelativeTimeSeriesReturnCalculator returnCalculator, final CalculationMode mode) {
    super(mode);
    ArgumentChecker.notNull(returnCalculator, "return calculator");
    _returnCalculator = returnCalculator;
  }

  /**
   * Creates a historical volatility calculator with the given relative return calculation method, calculation mode and allowable percentage of bad data points
   * @param returnCalculator The return calculator, not null
   * @param mode The calculation mode, not null
   * @param percentBadDataPoints The maximum allowable percentage of bad data points
   */
  public HistoricalVolatilityHighLowCalculator(final RelativeTimeSeriesReturnCalculator returnCalculator, final CalculationMode mode, final double percentBadDataPoints) {
    super(mode, percentBadDataPoints);
    ArgumentChecker.notNull(returnCalculator, "return calculator");
    _returnCalculator = returnCalculator;
  }

  /**
   * The array of time series assumes that the first series is the high series and the second the low.
   * @param x The array of price time series
   * @return The historical close volatility
   * @throws IllegalArgumentException If the array is null or empty; if the first element of the array is null; if the array does not contain two time series;
   * if the high and low time series do not satisfy the requirements (see {@link HistoricalVolatilityCalculator#testHighLow}); if the price series does not contain at
   * least two data points
   */
  @Override
  public Double evaluate(final LocalDateDoubleTimeSeries... x) {
    testTimeSeries(x, 1);
    if (x.length < 2) {
      throw new IllegalArgumentException("Need high and low time series to calculate high-low volatility");
    }
    if (x.length > 2) {
      s_logger.info("Time series array contained more than two series; only using the first two");
    }
    final LocalDateDoubleTimeSeries high = x[0];
    final LocalDateDoubleTimeSeries low = x[1];
    testHighLow(high, low);
    final LocalDateDoubleTimeSeries returnTS = _returnCalculator.evaluate(new LocalDateDoubleTimeSeries[] {high, low});
    final int n = returnTS.size();
    final Iterator<Double> iter = returnTS.valuesIterator();
    double sum = 0;
    while (iter.hasNext()) {
      sum += iter.next();
    }
    return sum / (2 * n * Math.sqrt(Math.log(2.)));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((_returnCalculator == null) ? 0 : _returnCalculator.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final HistoricalVolatilityHighLowCalculator other = (HistoricalVolatilityHighLowCalculator) obj;
    return ObjectUtils.equals(_returnCalculator, other._returnCalculator);
  }
}
