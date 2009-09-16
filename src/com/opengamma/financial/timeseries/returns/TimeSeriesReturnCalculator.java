/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.returns;

import com.opengamma.math.function.Function;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.TimeSeriesException;
import com.opengamma.util.CalculationMode;
import com.opengamma.util.CompareUtils;

/**
 * 
 * @author emcleod
 *         <p>
 *         A single-period time series return calculator.
 */

public abstract class TimeSeriesReturnCalculator implements Function<DoubleTimeSeries, DoubleTimeSeries, TimeSeriesException> {
  private final CalculationMode _mode;

  public TimeSeriesReturnCalculator(CalculationMode mode) {
    _mode = mode;
  }

  @Override
  public abstract DoubleTimeSeries evaluate(DoubleTimeSeries... x) throws TimeSeriesException;

  protected boolean isValueNonZero(Double value) throws TimeSeriesException {
    if (CompareUtils.closeEquals(value, 0)) {
      if (_mode == CalculationMode.STRICT) {
        throw new TimeSeriesException("Cannot have zero in time series in strict mode");
      }
      return false;
    }
    return true;
  }
}
