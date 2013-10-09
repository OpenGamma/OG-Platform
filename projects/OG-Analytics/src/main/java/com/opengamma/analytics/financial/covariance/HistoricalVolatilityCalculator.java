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

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.TimeSeriesException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.CalculationMode;

/**
 * Base class for historical volatility calculators.
 */
public abstract class HistoricalVolatilityCalculator implements VolatilityCalculator {
  /** The default calculation mode */
  private static final CalculationMode DEFAULT_CALCULATION_MODE = CalculationMode.STRICT;
  /** The default percentage of bad data points allowed */
  private static final double DEFAULT_PERCENT_BAD_DATA_POINTS = 0.0;
  /** The calculation mode */
  private final CalculationMode _mode;
  /** The percentage of bad data points allowed */
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
   * @param mode The calculation mode, not null
   * @param percentBadDataPoints The acceptable percentage of bad data points, must be >= 0 and <= 1
   */
  public HistoricalVolatilityCalculator(final CalculationMode mode, final double percentBadDataPoints) {
    ArgumentChecker.notNull(mode, "mode");
    ArgumentChecker.isInRangeInclusive(0, 1, percentBadDataPoints);
    _mode = mode;
    _percentBadDataPoints = percentBadDataPoints;
  }

  /**
   * Checks that the time series array is not null, empty and that the first entry is not null
   * @param tsArray The time series array, not null or empty
   * @param minLength The minimum of entries in the time series
   */
  protected void testTimeSeries(final DoubleTimeSeries<?>[] tsArray, final int minLength) {
    ArgumentChecker.notEmpty(tsArray, "array of time series");
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
