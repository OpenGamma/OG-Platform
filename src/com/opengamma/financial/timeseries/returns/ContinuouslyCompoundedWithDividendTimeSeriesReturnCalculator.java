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

import com.opengamma.math.function.Function;
import com.opengamma.timeseries.ArrayDoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.TimeSeriesException;
import com.opengamma.util.CalculationMode;

/**
 * 
 * @author emcleod
 *         <p>
 *         This class contains a function that calculates the continuously
 *         compounded one-period simple return (also known as the log return) of
 *         an asset that pays a dividend periodically. This is defined at time
 *         <i>t</i> as:<br>
 *         <i>r<sub>t</sub> =
 *         ln(P<sub>t</sub>+D<sub>t</sub>)-ln(P<sub>t-1</sub>)</i><br>
 *         where <i>P<sub>t</sub></i> is the price at time <i>t</i>,
 *         <i>D<sub>t</sub></i> is the dividend at price <i>t</i> and
 *         <i>P<sub>t-1</sub></i> is the price at time <i>t-1</i>.
 */

public class ContinuouslyCompoundedWithDividendTimeSeriesReturnCalculator extends TimeSeriesReturnCalculator {
  private final Function<DoubleTimeSeries, DoubleTimeSeries, TimeSeriesException> _noDividendCalculator;

  public ContinuouslyCompoundedWithDividendTimeSeriesReturnCalculator(CalculationMode mode) {
    super(mode);
    _noDividendCalculator = new ContinuouslyCompoundedTimeSeriesReturnCalculator(mode);
  }

  /**
   * @param x
   *          An array of DoubleTimeSeries. If the array has only one element,
   *          then this is assumed to be the price series and the result is the
   *          continuously-compounded return. The dividend series is assumed to
   *          be the second element. It does not have to be the same length as
   *          the price series (in which case, dates without dividends are
   *          treated like the dividend was zero), and the dividend data points
   *          do not have to correspond to any of the dates in the price series
   *          (in which case, the result is the continuously-compounded return).
   * @throws TimeSeriesException
   *           Throws an exception if: the array is null; it has no elements;
   *           the time series has less than two entries; if the calculation
   *           mode is strict and there are zeroes in the price series.
   * @return A DoubleTimeSeries containing the return series. This will always
   *         be one element shorter than the original price series.
   */
  @Override
  public DoubleTimeSeries evaluate(DoubleTimeSeries... x) throws TimeSeriesException {
    if (x == null)
      throw new TimeSeriesException("Time series array was null");
    if (x.length < 2)
      return _noDividendCalculator.evaluate(x);
    DoubleTimeSeries ts = x[0];
    if (ts.size() < 2)
      throw new TimeSeriesException("Need at least two data points to calculate return series");
    DoubleTimeSeries d = x[1];
    if (d.size() == 0)
      return _noDividendCalculator.evaluate(x);
    List<InstantProvider> times = new ArrayList<InstantProvider>();
    List<Double> data = new ArrayList<Double>();
    Iterator<Map.Entry<InstantProvider, Double>> iter = ts.iterator();
    Map.Entry<InstantProvider, Double> previousEntry = iter.next();
    Map.Entry<InstantProvider, Double> entry;
    double dividend;
    while (iter.hasNext()) {
      entry = iter.next();
      times.add(entry.getKey());
      if (isValueNonZero(previousEntry.getValue())) {
        try {
          dividend = d.getDataPoint(entry.getKey());
          data.add(Math.log((entry.getValue() + dividend) / previousEntry.getValue()));
        } catch (ArrayIndexOutOfBoundsException e) {
          data.add(Math.log(entry.getValue() / previousEntry.getValue()));
        } catch (NoSuchElementException e) {
          data.add(Math.log(entry.getValue() / previousEntry.getValue()));
        }
      }
      previousEntry = entry;
    }
    return new ArrayDoubleTimeSeries(times, data);
  }

}
