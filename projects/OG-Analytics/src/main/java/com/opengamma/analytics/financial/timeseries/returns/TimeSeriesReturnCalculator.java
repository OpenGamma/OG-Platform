/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.returns;

import cern.colt.Arrays;

import com.opengamma.analytics.math.function.Function;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.CalculationMode;
import com.opengamma.util.CompareUtils;
import com.opengamma.util.timeseries.TimeSeriesException;
import com.opengamma.util.timeseries.fast.integer.FastArrayIntDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 * <p>
 * A single-period time series return calculator.
 * 
 */

public abstract class TimeSeriesReturnCalculator implements Function<LocalDateDoubleTimeSeries, LocalDateDoubleTimeSeries> {
  private final CalculationMode _mode;

  public TimeSeriesReturnCalculator(final CalculationMode mode) {
    ArgumentChecker.notNull(mode, "mode");
    _mode = mode;
  }

  @Override
  public abstract LocalDateDoubleTimeSeries evaluate(LocalDateDoubleTimeSeries... x);

  protected boolean isValueNonZero(final double value) {
    if (CompareUtils.closeEquals(value, 0)) {
      if (_mode == CalculationMode.STRICT) {
        throw new TimeSeriesException("Cannot have zero in time series in strict mode");
      }
      return false;
    }
    return true;
  }

  protected CalculationMode getMode() {
    return _mode;
  }

  protected LocalDateDoubleTimeSeries getSeries(final LocalDateDoubleTimeSeries x, final int[] filteredDates, final double[] filteredData, final int i) {
    return new ArrayLocalDateDoubleTimeSeries(new FastArrayIntDoubleTimeSeries(x.getFastSeries().getEncoding(), Arrays.trimToCapacity(filteredDates, i), Arrays.trimToCapacity(filteredData, i)));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_mode == null) ? 0 : _mode.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final TimeSeriesReturnCalculator other = (TimeSeriesReturnCalculator) obj;
    if (_mode != other._mode) {
      return false;
    }
    return true;
  }
}
