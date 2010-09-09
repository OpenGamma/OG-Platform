/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.covariance;

import java.util.Iterator;
import java.util.List;

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
   * @throws IllegalArgumentException If the percentage of bad data points {@latex.inline $p$} does not satisfy {@latex.inline $0 \\leq p \\leq 1$}
   */
  public HistoricalVolatilityCalculator(final CalculationMode mode, final double percentBadDataPoints) {
    ArgumentChecker.isInRangeInclusive(0, 1, percentBadDataPoints);
    _mode = mode;
    _percentBadDataPoints = percentBadDataPoints;
  }

  /**
   * Tests the array of time series 
   * @param x An array of time series
   * @throws IllegalArgumentException If the array is null; if the array is empty; if the first element of the array is null
   */
  protected void testInput(final DoubleTimeSeries<?>[] x) {
    Validate.notNull(x);
    ArgumentChecker.notEmpty(x, "x");
    Validate.notNull(x[0], "first time series");
  }

  /**
   * Tests that each time series has a minimum length
   * @param x An array of time series 
   * @param minLength The minimum allowed length of a time series
   * @throws IllegalArgumentException If a time series is less than the minimum 
   */
  protected void testTimeSeries(final DoubleTimeSeries<?>[] x, final int minLength) {
    for (final DoubleTimeSeries<?> ts : x) {
      if (ts.size() < minLength) {
        throw new IllegalArgumentException("Need at least two data points to calculate volatility");
      }
    }
  }

  /**
   * Tests that each time series contains the same dates.
   * @param x An array of time series
   * @throws IllegalArgumentException If the time series are not all the same length; if the time series contain different dates
   */
  protected void testDatesCoincide(final DoubleTimeSeries<?>[] x) {
    final int size = x[0].size();
    for (int i = 1; i < x.length; i++) {
      if (x[i].size() != size) {
        throw new TimeSeriesException("Time series were not all the same length");
      }
    }
    final List<?> times1 = x[0].times();
    List<?> times2;
    for (int i = 1; i < x.length; i++) {
      times2 = x[i].times();
      for (final Object t : times1) {
        if (!times2.contains(t)) {
          throw new TimeSeriesException("Time series did not all contain the same dates");
        }
      }
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
