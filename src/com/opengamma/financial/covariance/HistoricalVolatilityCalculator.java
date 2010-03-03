/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.covariance;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.CalculationMode;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.TimeSeriesException;

/**
 * 
 * @author emcleod
 */
public abstract class HistoricalVolatilityCalculator<T extends DoubleTimeSeries<?>> implements VolatilityCalculator<T> {
  private static final Logger s_Log = LoggerFactory.getLogger(HistoricalVolatilityCalculator.class);
  private final CalculationMode _mode;
  private final double _percentBadDataPoints;

  public HistoricalVolatilityCalculator() {
    _mode = CalculationMode.STRICT;
    _percentBadDataPoints = 0.001;
  }

  public HistoricalVolatilityCalculator(final CalculationMode mode) {
    _mode = mode;
    _percentBadDataPoints = 0.001;
  }

  public HistoricalVolatilityCalculator(final CalculationMode mode, final double percentBadDataPoints) {
    _mode = mode;
    if (percentBadDataPoints > 1) {
      s_Log.warn("Fraction of bad high / low / close data points that will be accepted is greater than one; this is probably not what was intended");
    }
    _percentBadDataPoints = percentBadDataPoints;
  }

  protected void testInput(final T[] x) {
    if (x == null)
      throw new TimeSeriesException("Array of time series was null");
    if (x.length == 0)
      throw new TimeSeriesException("Length of time series was null");
    if (x[0] == null)
      throw new TimeSeriesException("First time series was null");
  }

  protected void testTimeSeries(final T[] x, final int minLength) {
    for (final T ts : x) {
      if (ts.size() < minLength)
        throw new TimeSeriesException("Need at least two data points to calculate volatility");
    }
  }

  protected void testDatesCoincide(final T[] x) {
    final int size = x[0].size();
    for (int i = 1; i < x.length; i++) {
      if (x[i].size() != size)
        throw new TimeSeriesException("Time series were not all the same length");
    }
    final List<?> times1 = x[0].times();
    List<?> times2;
    for (int i = 1; i < x.length; i++) {
      times2 = x[i].times();
      for (final Object t : times1) {
        if (!times2.contains(t))
          throw new TimeSeriesException("Time series did not all contain the same dates");
      }
    }
  }

  protected void testHighLow(final T high, final T low) {
    final double size = high.size();
    int count = 0;
    final Iterator<Double> highIter = high.valuesIterator();
    final Iterator<Double> lowIter = low.valuesIterator();
    boolean compare;
    while (highIter.hasNext()) {
      compare = highIter.next() < lowIter.next();
      if (compare) {
        if (_mode == CalculationMode.STRICT)
          throw new TimeSeriesException("Not all values in the high series were greater than the values in the low series");
        count++;
      }
    }
    final double percent = count / size;
    if (percent > _percentBadDataPoints)
      throw new TimeSeriesException("Percent " + percent + " of bad data points is greater than " + _percentBadDataPoints);
  }

  protected void testHighLowClose(final T high, final T low, final T close) {
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
        if (_mode == CalculationMode.STRICT)
          throw new TimeSeriesException("Not all values in the high series were greater than the values in the low series");
        count++;
      }
    }
    final double percent = count / size;
    if (percent > _percentBadDataPoints)
      throw new TimeSeriesException("Percent " + percent + " of bad data points is greater than " + _percentBadDataPoints);
  }
}
