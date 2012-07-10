/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 *
 */
/* package */ class LocalDateDoubleTimeSeriesFormatter extends NoHistoryFormatter<LocalDateDoubleTimeSeries> {

  @Override
  public String formatForDisplay(LocalDateDoubleTimeSeries timeSeries, ValueSpecification valueSpec) {
    return "Time-series (" + timeSeries.getEarliestTime().toLocalDate() + " to " + timeSeries.getLatestTime().toLocalDate() + ")";
  }

  @Override
  public Object formatForExpandedDisplay(LocalDateDoubleTimeSeries value, ValueSpecification valueSpec) {
    // TODO implement formatForExpandedDisplay()
    throw new UnsupportedOperationException("formatForExpandedDisplay not implemented");
  }

  @Override
  public FormatType getFormatType() {
    return FormatType.TIME_SERIES;
  }
}
