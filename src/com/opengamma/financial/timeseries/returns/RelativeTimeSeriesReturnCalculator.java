/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.returns;

import java.util.List;

import com.opengamma.util.CalculationMode;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.TimeSeriesException;

/**
 * 
 * @author emcleod
 */
public abstract class RelativeTimeSeriesReturnCalculator extends TimeSeriesReturnCalculator {

  public RelativeTimeSeriesReturnCalculator(final CalculationMode mode) {
    super(mode);
  }

  protected void testInputData(final DoubleTimeSeries<?>[] x) {
    if (x == null)
      throw new TimeSeriesException("Time series array was null");
    if (x.length == 0)
      throw new TimeSeriesException("Need at least one time series");
    final int size = x[0].size();
    for (int i = 1; i < x.length; i++) {
      if (x[i].size() != size)
        throw new TimeSeriesException("Time series were not all the same length");
    }
    final List<?> times1 = x[0].times();
    List<?> times2;
    for (int i = 1; i < x.length; i++) {
      times2 = x[1].times();
      for (final Object t : times1) {
        if (!times2.contains(t))
          throw new TimeSeriesException("Time series did not contain all the same dates");
      }
    }
  }
}
