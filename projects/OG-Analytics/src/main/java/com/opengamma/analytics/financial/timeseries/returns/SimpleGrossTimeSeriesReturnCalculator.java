/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.returns;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.CalculationMode;
import com.opengamma.util.timeseries.TimeSeriesException;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * <p>
 * This class contains a function that calculates the gross one-period simple
 * return of an asset that pays dividends periodically. This is defined at time
 * <i>t</i> as:<br>
 * <i>R<sub>t</sub> = (P<sub>t</sub>-D<sub>t</sub>)/P<sub>t-1</sub></i><br>
 * where <i>P<sub>t</sub></i> is the price at time <i>t</i>,
 * <i>D<sub>t</sub></i> is the dividend at time <i>t</i> and
 * <i>P<sub>t-1</sub></i> is the price at time <i>t-1</i>.
 * 
 */

public class SimpleGrossTimeSeriesReturnCalculator extends TimeSeriesReturnCalculator {

  public SimpleGrossTimeSeriesReturnCalculator(final CalculationMode mode) {
    super(mode);
  }

  // REVIEW: jim 12-Jan-2012 -- this desperately needs refactoring.
  /**
   * @param x
   *          An array of DoubleTimeSeries. If the array has only one element,
   *          then this is assumed to be the price series and the result is the
   *          simple return. The dividend series is assumed to be the second
   *          element. It does not have to be the same length as the price
   *          series (in which case, dates without dividends are treated like
   *          the dividend was zero), and the dividend data points do not have
   *          to correspond to any of the dates in the price series (in which
   *          case, the result is the simple net return).
   * @throws IllegalArgumentException
   *           If the array is null
   * @throws TimeSeriesException
   *           Throws an exception if: the array is null; it has no elements;
   *           the time series has less than two entries; if the calculation
   *           mode is strict and there are zeroes in the price series.
   * @return A DoubleTimeSeries containing the return series. This will always
   *         be one element shorter than the original price series.
   */
  @Override
  public LocalDateDoubleTimeSeries evaluate(final LocalDateDoubleTimeSeries... x) {
    Validate.notNull(x, "x");
    ArgumentChecker.notEmpty(x, "x");
    Validate.notNull(x[0], "first time series");
    final FastIntDoubleTimeSeries ts = x[0].toFastIntDoubleTimeSeries();
    if (ts.size() < 2) {
      throw new TimeSeriesException("Need at least two data points to calculate return series");
    }
    FastIntDoubleTimeSeries d = null;
    if (x.length > 1) {
      if (x[1] != null) {
        d = x[1].toFastIntDoubleTimeSeries();
      }
    }
    final int n = ts.size();
    final int[] times = new int[n];
    final double[] data = new double[n];
    final Iterator<Map.Entry<Integer, Double>> iter = ts.iterator();
    Map.Entry<Integer, Double> previousEntry = iter.next();
    Map.Entry<Integer, Double> entry;
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
        data[i++] = (entry.getValue() + dividend) / previousEntry.getValue();
      }
      previousEntry = entry;
    }
    return getSeries(x[0], times, data, i);
  }
}
