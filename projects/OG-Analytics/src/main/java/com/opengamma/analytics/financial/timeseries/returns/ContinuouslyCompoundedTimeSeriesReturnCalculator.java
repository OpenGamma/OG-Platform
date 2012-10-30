/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.returns;

import org.apache.commons.lang.Validate;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.CalculationMode;
import com.opengamma.util.timeseries.TimeSeriesException;
import com.opengamma.util.timeseries.fast.integer.FastIntDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

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
  public LocalDateDoubleTimeSeries evaluate(final LocalDateDoubleTimeSeries... x) {
    Validate.notNull(x, "x");
    ArgumentChecker.notEmpty(x, "x");
    Validate.notNull(x[0], "first time series");
    final FastIntDoubleTimeSeries ts = (FastIntDoubleTimeSeries) x[0].getFastSeries();
    if (ts.size() < 2) {
      throw new TimeSeriesException("Need at least two data points to calculate return series");
    }
    FastIntDoubleTimeSeries d = null;
    if (x.length > 1) {
      if (x[1] != null) {
        d = (FastIntDoubleTimeSeries) x[1].getFastSeries();
      }
    }

    final int[] times = ts.timesArrayFast();
    final double[] values = ts.valuesArrayFast();
    
    final int[] resultTimes = new int[times.length];
    final double[] resultValues = new double[times.length];
    
    int index = 0;
    //int previousTime = times[index];
    double previousValue = values[index];
    index++;
    
    double dividend;
    Double dividendTSData;
    int resultIndex = 0;
    
    while (index < times.length) {
      int time = times[index];
      double value = values[index];
      index++;
      
      if (isValueNonZero(previousValue) && isValueNonZero(value)) {
        resultTimes[resultIndex] = time;
        if (d == null) {
          dividend = 0;
        } else {
          dividendTSData = d.getValue(time); // Arghh, this makes it n log(n) instead of n...  Improve this.
          dividend = dividendTSData == null ? 0 : dividendTSData;
        }
        resultValues[resultIndex] = Math.log((value + dividend) / previousValue);
        resultIndex++;
      }
      //previousTime = time;
      previousValue = value;
    }
    return getSeries(x[0], resultTimes, resultValues, resultIndex);
  }
}
