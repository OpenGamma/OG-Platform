/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.covariance;

import java.util.Iterator;

import javax.time.InstantProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.TimeSeriesException;
import com.opengamma.util.CalculationMode;

/**
 * 
 * @author emcleod
 */
public abstract class HistoricalVolatilityCalculator implements VolatilityCalculator {
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

  protected void testInput(final DoubleTimeSeries[] x) {
    if (x == null)
      throw new TimeSeriesException("Array of time series was null");
    if (x.length == 0)
      throw new TimeSeriesException("Length of time series was null");
  }

  protected void testTimeSeries(final DoubleTimeSeries[] x, final int minLength) {
    for (final DoubleTimeSeries ts : x) {
      if (ts.size() < minLength)
        throw new TimeSeriesException("Need at least two data points to calculate volatility");
    }
  }

  protected void testDatesCoincide(final DoubleTimeSeries[] x) {
    final int size = x[0].size();
    for (int i = 1; i < x.length; i++) {
      if (x[i].size() != size)
        throw new TimeSeriesException("Time series were not all the same length");
    }
    final Iterator<InstantProvider> iter = x[0].timeIterator();
    while (iter.hasNext()) {
      final InstantProvider instant = iter.next();
      for (int i = 1; i < x.length; i++) {
        try {
          x[i].getDataPoint(instant);
        } catch (final ArrayIndexOutOfBoundsException e) {
          throw new TimeSeriesException("Time series did not all contain the same dates; " + e);
        }
      }
    }
  }

  protected void testHighLow(final DoubleTimeSeries high, final DoubleTimeSeries low) {
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

  protected void testHighLowClose(final DoubleTimeSeries high, final DoubleTimeSeries low, final DoubleTimeSeries close) {
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
