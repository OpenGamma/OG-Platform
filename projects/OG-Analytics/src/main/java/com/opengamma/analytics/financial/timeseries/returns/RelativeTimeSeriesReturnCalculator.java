/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.returns;

import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.TimeSeriesException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.CalculationMode;

/**
 * 
 */
public abstract class RelativeTimeSeriesReturnCalculator extends TimeSeriesReturnCalculator {

  public RelativeTimeSeriesReturnCalculator(final CalculationMode mode) {
    super(mode);
  }

  protected void testInputData(final DoubleTimeSeries<?>... x) {
    Validate.notNull(x, "x");
    ArgumentChecker.notEmpty(x, "x");
    Validate.notNull(x[0], "first time series");
    Validate.notNull(x[1], "second time series");
    if (getMode() == CalculationMode.STRICT) {
      final int size = x[0].size();
      for (int i = 1; i < x.length; i++) {
        if (x[i].size() != size) {
          throw new TimeSeriesException("Time series were not all the same length");
        }
      }
      final List<?> times1 = x[0].times();
      List<?> times2;
      for (int i = 1; i < x.length; i++) {
        times2 = x[1].times();
        for (final Object t : times1) {
          if (!times2.contains(t)) {
            throw new TimeSeriesException("Time series did not contain all the same dates");
          }
        }
      }
    }
  }
}
