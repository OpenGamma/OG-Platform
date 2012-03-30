/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.covariance;

import static com.opengamma.analytics.financial.timeseries.util.TimeSeriesDataTestUtils.testNotNullOrEmpty;
import static com.opengamma.analytics.financial.timeseries.util.TimeSeriesDataTestUtils.testTimeSeriesDates;
import static com.opengamma.analytics.financial.timeseries.util.TimeSeriesDataTestUtils.testTimeSeriesSize;

import java.util.Iterator;

import org.apache.commons.lang.Validate;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.CalculationMode;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.TimeSeriesException;

/**
 * Base class for historical volatility calculators. 
 */
public abstract class HistoricalVolatilityCalculator implements VolatilityCalculator {
  private static final CalculationMode DEFAULT_CALCULATION_MODE = CalculationMode.STRICT;
  private static final double DEFAULT_PERCENT_BAD_DATA_POINTS = 0.0;
  private final CalculationMode _mode;
  private final double _percentBadDataPoints;

  /**
   * Sets the calculation mode and acceptable percentage of bad data points to the default value (strict and 0 respectively)
   */
  public HistoricalVolatilityCalculator() {
    this(DEFAULT_CALCULATION_MODE, DEFAULT_PERCENT_BAD_DATA_POINTS);
  }

  /**
   * Sets the acceptable percentage of bad data points to the default value, 0
   * @param mode The calculation mode
   */
  public HistoricalVolatilityCalculator(final CalculationMode mode) {
    this(mode, DEFAULT_PERCENT_BAD_DATA_POINTS);
  }

  /**
   * 
   * @param mode The calculation mode
   * @param percentBadDataPoints The acceptable percentage of bad data points
   * @throws IllegalArgumentException If the percentage of bad data points $p$ does not satisfy $0 \leq p \leq 1$
   */
  public HistoricalVolatilityCalculator(final CalculationMode mode, final double percentBadDataPoints) {
    ArgumentChecker.isInRangeInclusive(0, 1, percentBadDataPoints);
    _mode = mode;
    _percentBadDataPoints = percentBadDataPoints;
  }

  protected void testTimeSeries(final DoubleTimeSeries<?>[] tsArray, final int minLength) {
    Validate.notNull(tsArray, "array of time series");
    Validate.notEmpty(tsArray, "array of time series");
    testNotNullOrEmpty(tsArray[0]);
    final DoubleTimeSeries<?> ts = tsArray[0];
    testTimeSeriesSize(ts, minLength);
    for (int i = 1; i < tsArray.length; i++) {
      testTimeSeriesSize(tsArray[i], minLength);
      testTimeSeriesDates(ts, tsArray[i]);
    }
  }

  /**
   * Tests that the high price for a date is greater than the low value for the same date.
   * @param high The period high price time series
   * @param low The period low price time series
   * @throws IllegalArgumentException Strict calculation mode: if the low value for a date is greater than the high value. Lenient calculation mode: if the percentage of times 
   * that the low value for a date is greater than the high value is greater than the maximum allowed
   */
  protected void testHighLow(final DoubleTimeSeries<?> high, final DoubleTimeSeries<?> low) {
    final double size = high.size();
    int count = 0;
    final Iterator<Double> highIter = high.valuesIterator();
    final Iterator<Double> lowIter = low.valuesIterator();
    boolean compare;
    while (highIter.hasNext()) {
      compare = highIter.next() < lowIter.next();
      if (compare) {
        if (_mode == CalculationMode.STRICT) {
          throw new TimeSeriesException("Not all values in the high series were greater than the values in the low series");
        }
        count++;
      }
    }
    final double percent = count / size;
    if (percent > _percentBadDataPoints) {
      throw new TimeSeriesException("Percent " + percent + " of bad data points is greater than " + _percentBadDataPoints);
    }
  }

  /**
   * Tests that the high price for a date is greater than the low value for the same date and that the close price falls in this (inclusive) range
   * @param high The period high price time series
   * @param low The period low price time series
   * @param close The period close price time series
   * @throws IllegalArgumentException Strict calculation mode: if the low value for a date is greater than the high value or if the close value is not in the range bounded
   * by the high and low prices. Lenient calculation mode: if the percentage of times that the low value for a date is greater than the high value or that the close value is
   * not in the range bounded by the high and low values is greater than the maximum allowed
   */
  protected void testHighLowClose(final DoubleTimeSeries<?> high, final DoubleTimeSeries<?> low, final DoubleTimeSeries<?> close) {
    final double size = high.size();
    int count = 0;
    final Iterator<Double> highIter = high.valuesIterator();
    final Iterator<Double> lowIter = low.valuesIterator();
    final Iterator<Double> closeIter = close.valuesIterator();
    boolean compare;
    double highValue, lowValue, closeValue;
    while (highIter.hasNext()) {
      highValue = highIter.next();
      lowValue = lowIter.next();
      closeValue = closeIter.next();
      compare = highValue < lowValue || closeValue > highValue || closeValue < lowValue;
      if (compare) {
        if (_mode == CalculationMode.STRICT) {
          throw new TimeSeriesException("Not all values in the high series were greater than the values in the low series");
        }
        count++;
      }
    }
    final double percent = count / size;
    if (percent > _percentBadDataPoints) {
      throw new TimeSeriesException("Percent " + percent + " of bad data points is greater than " + _percentBadDataPoints);
    }
  }

  /**
   * 
   * @return The default calculation mode
   */
  protected static CalculationMode getDefaultCalculationMode() {
    return DEFAULT_CALCULATION_MODE;
  }

  /**
   * 
   * @return The default percentage of bad data points
   */
  protected static double getDefaultBadDataPoints() {
    return DEFAULT_PERCENT_BAD_DATA_POINTS;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_mode == null) ? 0 : _mode.hashCode());
    long temp;
    temp = Double.doubleToLongBits(_percentBadDataPoints);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final HistoricalVolatilityCalculator other = (HistoricalVolatilityCalculator) obj;
    if (_mode != other._mode) {
      return false;
    }
    if (Double.doubleToLongBits(_percentBadDataPoints) != Double.doubleToLongBits(other._percentBadDataPoints)) {
      return false;
    }
    return true;
  }
}
