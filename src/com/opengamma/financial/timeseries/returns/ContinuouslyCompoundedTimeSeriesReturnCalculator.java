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

import javax.time.InstantProvider;

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
 *         a time series. This is defined at time <i>t</i> as:<br>
 *         <i>r<sub>t</sub> = ln(P<sub>t</sub>/P<sub>t-1</sub>)</i><br>
 *         where <i>P<sub>t</sub></i> is the price at time <i>t</i> and
 *         <i>P<sub>t-1</sub></i> is the price at time <i>t-1</i>.
 */

public class ContinuouslyCompoundedTimeSeriesReturnCalculator extends TimeSeriesReturnCalculator {

  public ContinuouslyCompoundedTimeSeriesReturnCalculator(CalculationMode mode) {
    super(mode);
  }

  /**
   * @param x
   *          An array of DoubleTimeSeries. Only the first element is used - if
   *          the array is longer then the other elements are ignored.
   * @throws TimeSeriesException
   *           Throws an exception if: the array is null; it has no elements;
   *           the time series has less than two entries; the calculation mode
   *           is strict and there are zeroes in the price series.
   * @return A DoubleTimeSeries containing the return series. In strict mode,
   *         this will always be one element shorter than the original price
   *         series. In lenient mode, any data points with zero in will be
   *         ignored, so each zero in the price series will decrease the length
   *         of the return series by two.
   */
  @Override
  public DoubleTimeSeries evaluate(DoubleTimeSeries... x) throws TimeSeriesException {
    if (x == null)
      throw new TimeSeriesException("Time series array was null");
    if (x.length == 0)
      throw new TimeSeriesException("Time series array was empty");
    DoubleTimeSeries ts = x[0];
    if (ts.size() <= 1)
      throw new TimeSeriesException("Must have at least two data points to calculate return");
    Iterator<Map.Entry<InstantProvider, Double>> iter = ts.iterator();
    Map.Entry<InstantProvider, Double> previousEntry = iter.next();
    Map.Entry<InstantProvider, Double> entry;
    List<InstantProvider> dates = new ArrayList<InstantProvider>();
    List<Double> data = new ArrayList<Double>();
    while (iter.hasNext()) {
      entry = iter.next();
      if (isValueNonZero(previousEntry.getValue()) && isValueNonZero(entry.getValue())) {
        dates.add(entry.getKey());
        data.add(Math.log(entry.getValue() / previousEntry.getValue()));
      }
      previousEntry = entry;
    }
    return new ArrayDoubleTimeSeries(dates, data);
  }
}
