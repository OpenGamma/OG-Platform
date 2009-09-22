/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.returns;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.time.InstantProvider;

import com.opengamma.timeseries.ArrayDoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.TimeSeriesException;
import com.opengamma.util.CalculationMode;

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
 * @author emcleod
 */

public class SimpleGrossTimeSeriesReturnCalculator extends TimeSeriesReturnCalculator {

  public SimpleGrossTimeSeriesReturnCalculator(CalculationMode mode) {
    super(mode);
  }

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
   * @throws TimeSeriesException
   *           Throws an exception if: the array is null; it has no elements;
   *           the time series has less than two entries; if the calculation
   *           mode is strict and there are zeroes in the price series.
   * @return A DoubleTimeSeries containing the return series. This will always
   *         be one element shorter than the original price series.
   */
  @Override
  public DoubleTimeSeries evaluate(DoubleTimeSeries... x) {
    if (x == null)
      throw new TimeSeriesException("Time series array was null");
    if (x.length == 0)
      throw new TimeSeriesException("Need at least one time series to calculate return");
    final DoubleTimeSeries ts = x[0];
    if (ts.size() < 2)
      throw new TimeSeriesException("Need at least two data points to calculate return series");
    final DoubleTimeSeries d = x.length > 1 ? x[1] : null;
    final List<InstantProvider> times = new ArrayList<InstantProvider>();
    final List<Double> data = new ArrayList<Double>();
    final Iterator<Map.Entry<InstantProvider, Double>> iter = ts.iterator();
    Map.Entry<InstantProvider, Double> previousEntry = iter.next();
    Map.Entry<InstantProvider, Double> entry;
    double dividend;
    while (iter.hasNext()) {
      entry = iter.next();
      if (isValueNonZero(previousEntry.getValue())) {
        times.add(entry.getKey());
        try {
          dividend = d == null ? 0 : d.getDataPoint(entry.getKey());
          data.add((entry.getValue() + dividend) / previousEntry.getValue());
        } catch (final ArrayIndexOutOfBoundsException e) {
          data.add(entry.getValue() / previousEntry.getValue());
        } catch (final NoSuchElementException e) {
          data.add(entry.getValue() / previousEntry.getValue());
        }
      }
      previousEntry = entry;
    }
    return new ArrayDoubleTimeSeries(times, data);
  }
}
