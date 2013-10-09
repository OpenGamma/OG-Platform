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

import com.opengamma.analytics.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.CalculationMode;

/**
 * Calculates the historical volatility of a time series with a given return
 * calculation method.
 * <p>
 * The historical volatility of a time series with close price data $x_i$ is
 * given by:
 * $$
 * \begin{eqnarray*}
 * \sigma = \sqrt{\frac{1}{n(n-1)}\sum\limits_{i=1}^n (r_i - \overline{r})^2}
 * \end{eqnarray*}
 * $$
 * where $r_i$ is the $i^\text{th}$ period return of the time series and $n$ is
 * the number of data points in the return series.
 */
public class HistoricalVolatilityCloseCalculator extends HistoricalVolatilityCalculator {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(HistoricalVolatilityCloseCalculator.class);
  /** The return calculator */
  private final TimeSeriesReturnCalculator _returnCalculator;

  /**
   * Creates a historical volatility calculator with the given return
   * calculation method and default values for the calculation mode and
   * allowable percentage of bad data points
   * @param returnCalculator The return calculator, not null
   */
  public HistoricalVolatilityCloseCalculator(final TimeSeriesReturnCalculator returnCalculator) {
    super();
    ArgumentChecker.notNull(returnCalculator, "return calculator");
    _returnCalculator = returnCalculator;
  }

  /**
   * Creates a historical volatility calculator with the given return
   * calculation method and calculation mode and the default value for the
   * allowable percentage of bad data points
   * @param returnCalculator The return calculator, not null
   * @param mode The calculation mode, not null
   */
  public HistoricalVolatilityCloseCalculator(final TimeSeriesReturnCalculator returnCalculator, final CalculationMode mode) {
    super(mode);
    ArgumentChecker.notNull(returnCalculator, "return calculator");
    _returnCalculator = returnCalculator;
  }

  /**
   * Creates a historical volatility calculator with the given return
   * calculation method, calculation mode and allowable percentage of bad data
   * points
   * @param returnCalculator The return calculator
   * @param mode The calculation mode
   * @param percentBadDataPoints The maximum allowable percentage of bad data points
   * @throws IllegalArgumentException If the return calculator is null
   */
  public HistoricalVolatilityCloseCalculator(final TimeSeriesReturnCalculator returnCalculator, final CalculationMode mode, final double percentBadDataPoints) {
    super(mode, percentBadDataPoints);
    ArgumentChecker.notNull(returnCalculator, "return calculator");
    _returnCalculator = returnCalculator;
  }

  /**
   * If more than one price time series is provided, the first element of the
   * array is used.
   * @param x The array of price time series
   * @return The historical close volatility
   * @throws IllegalArgumentException If the array is null or empty; if the first element of the array is null; if the price series does not contain at least two data points
   */
  @Override
  public Double evaluate(final LocalDateDoubleTimeSeries... x) {
    testTimeSeries(x, 2);
    if (x.length > 1) {
      s_logger.info("Time series array contained more than one series; only using the first one");
    }
    testTimeSeries(x, 2);
    final DoubleTimeSeries<?> returnTS = _returnCalculator.evaluate(x);
    final Iterator<Double> iter = returnTS.valuesIterator();
    Double value;
    double sum = 0;
    double sumSq = 0;
    final int n = returnTS.size() - 1;
    while (iter.hasNext()) {
      value = iter.next();
      sum += value;
      sumSq += value * value;
    }
    return Math.sqrt((sumSq - sum * sum / (n + 1)) / n);
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
    final HistoricalVolatilityCloseCalculator other = (HistoricalVolatilityCloseCalculator) obj;
    return ObjectUtils.equals(_returnCalculator, other._returnCalculator);
  }

}
