/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.filter;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 */
public class ExtremeReturnDoubleTimeSeriesFilter extends TimeSeriesFilter {
  private static final Logger s_logger = LoggerFactory.getLogger(ExtremeReturnDoubleTimeSeriesFilter.class);
  private TimeSeriesReturnCalculator _returnCalculator;
  private final ExtremeValueDoubleTimeSeriesFilter _filter;
  private static final LocalDateDoubleTimeSeries EMPTY_SERIES = new ArrayLocalDateDoubleTimeSeries();

  public ExtremeReturnDoubleTimeSeriesFilter(final double minValue, final double maxValue, final TimeSeriesReturnCalculator returnCalculator) {
    Validate.notNull(returnCalculator, "return calculator");
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
    ArgumentChecker.notNull(returnCalculator, "return calculator");
    _returnCalculator = returnCalculator;
  }

  @Override
  public FilteredTimeSeries evaluate(final LocalDateDoubleTimeSeries ts) {
    Validate.notNull(ts, "ts");
    if (ts.isEmpty()) {
      s_logger.info("Time series was empty");
      return new FilteredTimeSeries(EMPTY_SERIES, null);
    }
    final LocalDateDoubleTimeSeries returnTS = _returnCalculator.evaluate(ts);
    return _filter.evaluate(returnTS);
  }

}
