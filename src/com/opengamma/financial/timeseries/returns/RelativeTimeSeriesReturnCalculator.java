/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.returns;

import java.util.Iterator;

import javax.time.InstantProvider;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.TimeSeriesException;
import com.opengamma.util.CalculationMode;

/**
 * 
 * @author emcleod
 */
public abstract class RelativeTimeSeriesReturnCalculator extends TimeSeriesReturnCalculator {

  public RelativeTimeSeriesReturnCalculator(final CalculationMode mode) {
    super(mode);
  }

  protected void testInputData(final DoubleTimeSeries[] x) {
    if (x == null)
      throw new TimeSeriesException("Time series array was null");
    if (x.length == 0)
      throw new TimeSeriesException("Need at least one time series");
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
}
