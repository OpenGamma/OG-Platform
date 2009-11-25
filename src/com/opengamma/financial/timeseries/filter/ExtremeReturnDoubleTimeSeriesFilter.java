/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.timeseries.ArrayDoubleTimeSeries;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public class ExtremeReturnDoubleTimeSeriesFilter extends DoubleTimeSeriesFilter {
  private static final Logger s_Log = LoggerFactory.getLogger(ExtremeReturnDoubleTimeSeriesFilter.class);
  private TimeSeriesReturnCalculator _returnCalculator;
  private final ExtremeValueDoubleTimeSeriesFilter _filter;

  public ExtremeReturnDoubleTimeSeriesFilter(final double minValue, final double maxValue, final TimeSeriesReturnCalculator returnCalculator) {
    if (returnCalculator == null)
      throw new IllegalArgumentException("Return calculator was null");
    _returnCalculator = returnCalculator;
    _filter = new ExtremeValueDoubleTimeSeriesFilter(minValue, maxValue);
  }

  public void setMinimumValue(final double minValue) {
    _filter.setMinimumValue(minValue);
  }

  public void setMaximumValue(final double maxValue) {
    _filter.setMaximumValue(maxValue);
  }

  public void setRange(final double minValue, final double maxValue) {
    _filter.setRange(minValue, maxValue);
  }

  public void setReturnCalculator(final TimeSeriesReturnCalculator returnCalculator) {
    if (returnCalculator == null)
      throw new IllegalArgumentException("Return calculator was null");
    _returnCalculator = returnCalculator;
  }

  @Override
  public FilteredDoubleTimeSeries evaluate(final DoubleTimeSeries ts) {
    if (ts == null)
      throw new IllegalArgumentException("Time series was null");
    if (ts.isEmpty()) {
      s_Log.info("Time series was empty");
      return new FilteredDoubleTimeSeries(ArrayDoubleTimeSeries.EMPTY_SERIES, null);
    }
    final DoubleTimeSeries returnTS = _returnCalculator.evaluate(ts);
    return _filter.evaluate(returnTS);
  }

}
