/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.covariance;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.CalculationMode;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.TimeSeriesException;

/**
 * 
 */
public abstract class HistoricalVolatilityCalculator implements VolatilityCalculator {
  private static final Logger s_logger = LoggerFactory.getLogger(HistoricalVolatilityCalculator.class);
  private static final CalculationMode DEFAULT_CALCULATION_MODE = CalculationMode.STRICT;
  private static final double DEFAULT_PERCENT_BAD_DATA_POINTS = 0.0;
  private final CalculationMode _mode;
  private final double _percentBadDataPoints;

  public HistoricalVolatilityCalculator() {
    this(DEFAULT_CALCULATION_MODE);
  }

  public HistoricalVolatilityCalculator(final CalculationMode mode) {
    this(mode, DEFAULT_PERCENT_BAD_DATA_POINTS);
  }

  public HistoricalVolatilityCalculator(final CalculationMode mode, final double percentBadDataPoints) {
    _mode = mode;
    if (percentBadDataPoints > 1) {
      s_logger.warn("Fraction of bad high / low / close data points that will be accepted is greater than one; this is probably not what was intended");
    }
    _percentBadDataPoints = percentBadDataPoints;
  }

  protected void testInput(final DoubleTimeSeries<?>[] x) {
    Validate.notNull(x);
    ArgumentChecker.notEmpty(x, "x");
    Validate.notNull(x[0], "first time series");
  }

  protected void testTimeSeries(final DoubleTimeSeries<?>[] x, final int minLength) {
    for (final DoubleTimeSeries<?> ts : x) {
      if (ts.size() < minLength) {
        throw new IllegalArgumentException("Need at least two data points to calculate volatility");
      }
    }
  }

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

  protected static CalculationMode getCalculationMode() {
    return DEFAULT_CALCULATION_MODE;
  }

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
