/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.engine.value.ValueSpecification;

/**
 *
 */
/* package */ class HistoricalTimeSeriesFormatter extends AbstractFormatter<HistoricalTimeSeries> {

  private final LocalDateDoubleTimeSeriesFormatter _delegate = new LocalDateDoubleTimeSeriesFormatter();

  /* package */ HistoricalTimeSeriesFormatter() {
    super(HistoricalTimeSeries.class);
  }

  @Override
  public Object formatCell(HistoricalTimeSeries value, ValueSpecification valueSpec, Object inlineKey) {
    return _delegate.formatCell(value.getTimeSeries(), valueSpec, inlineKey);
  }

  @Override
  public Object format(HistoricalTimeSeries value, ValueSpecification valueSpec, Format format, Object inlineKey) {
    return _delegate.format(value.getTimeSeries(), valueSpec, format, inlineKey);
  }

  @Override
  public DataType getDataType() {
    return DataType.TIME_SERIES;
  }
}
