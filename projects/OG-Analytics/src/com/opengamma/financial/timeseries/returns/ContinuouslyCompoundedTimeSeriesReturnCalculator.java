/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.returns;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.CalculationMode;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.TimeSeriesException;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;

/**
 * <p>
 * This class contains a function that calculates the continuously compounded
 * one-period simple return (also known as the log return) of an asset that pays
 * a dividend periodically. This is defined at time <i>t</i> as:<br>
 * <i>r<sub>t</sub> = ln(P<sub>t</sub>+D<sub>t</sub>)-ln(P<sub>t-1</sub>)</i><br>
 * where <i>P<sub>t</sub></i> is the price at time <i>t</i>,
 * <i>D<sub>t</sub></i> is the dividend at price <i>t</i> and
 * <i>P<sub>t-1</sub></i> is the price at time <i>t-1</i>.
 * 
 */

public class ContinuouslyCompoundedTimeSeriesReturnCalculator extends TimeSeriesReturnCalculator {

  public ContinuouslyCompoundedTimeSeriesReturnCalculator(final CalculationMode mode) {
    super(mode);
  }

  /**
   * @param x
   *          An array of DoubleTimeSeries. If the array has only one element,
   *          then this is assumed to be the price series and the result is the
   *          continuously-compounded return. The dividend series is assumed to
   *          be the second element. It does not have to be the same length as
   *          the price series (in which case, dates without dividends are
   *          treated as if the dividend was zero), and the dividend data points
   *          do not have to correspond to any of the dates in the price series
   *          (in which case, the result is the continuously-compounded return).
   * @throws IllegalArgumentException
   *           If the array is null
   * @throws TimeSeriesException
   *           Throws an exception if: it has no elements;
   *           the time series has less than two entries; if the calculation
   *           mode is strict and there are zeroes in the price series.
   * @return A DoubleTimeSeries containing the return series. This will always
   *         be one element shorter than the original price series.
   */
  @Override
  public DoubleTimeSeries<?> evaluate(final DoubleTimeSeries<?>... x) {
    Validate.notNull(x, "x");
    ArgumentChecker.notEmpty(x, "x");
    Validate.notNull(x[0], "first time series");
    final FastLongDoubleTimeSeries ts = x[0].toFastLongDoubleTimeSeries();
    if (ts.size() < 2) {
      throw new TimeSeriesException("Need at least two data points to calculate return series");
    }
    FastLongDoubleTimeSeries d = null;
    if (x.length > 1) {
      if (x[1] != null) {
        d = x[1].toFastLongDoubleTimeSeries();
      }
    }
    final int n = ts.size();
    final long[] times = new long[n];
    final double[] data = new double[n];
    final Iterator<Map.Entry<Long, Double>> iter = ts.iterator();
    Map.Entry<Long, Double> previousEntry = iter.next();
    Map.Entry<Long, Double> entry;
    double dividend;
    Double dividendTSData;
    int i = 0;
    while (iter.hasNext()) {
      entry = iter.next();
      if (isValueNonZero(previousEntry.getValue()) && isValueNonZero(entry.getValue())) {
        times[i] = entry.getKey();
        if (d == null) {
          dividend = 0;
        } else {
          dividendTSData = d.getValue(entry.getKey());
          dividend = dividendTSData == null ? 0 : dividendTSData;
        }
        data[i++] = Math.log((entry.getValue() + dividend) / previousEntry.getValue());
      }
      previousEntry = entry;
    }
    return getSeries(ts, times, data, i);
  }
}
