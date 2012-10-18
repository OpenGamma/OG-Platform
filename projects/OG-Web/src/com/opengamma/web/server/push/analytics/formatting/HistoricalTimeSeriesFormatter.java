/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics.formatting;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
/* package */ class HistoricalTimeSeriesFormatter extends NoHistoryFormatter<HistoricalTimeSeries> {

  private final LocalDateDoubleTimeSeriesFormatter _delegate = new LocalDateDoubleTimeSeriesFormatter();

  @Override
  public Object formatForDisplay(HistoricalTimeSeries value, ValueSpecification valueSpec) {
    return _delegate.formatForDisplay(value.getTimeSeries(), valueSpec);
  }

  @Override
  public Object formatForExpandedDisplay(HistoricalTimeSeries value, ValueSpecification valueSpec) {
    return _delegate.formatForExpandedDisplay(value.getTimeSeries(), valueSpec);
  }

  @Override
  public FormatType getFormatType() {
    return FormatType.TIME_SERIES;
  }
}
