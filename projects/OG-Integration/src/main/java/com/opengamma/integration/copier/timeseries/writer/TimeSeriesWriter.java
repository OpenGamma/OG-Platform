/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.timeseries.writer;

import com.opengamma.id.ExternalId;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 * The interface for a time series writer, which must be able to accept a list of data points to write to a specified
 * time series.
 */
public interface TimeSeriesWriter {

  LocalDateDoubleTimeSeries writeDataPoints(ExternalId htsId, String dataSource, String dataProvider, String dataField, 
      String observationTime, LocalDateDoubleTimeSeries series);

  void flush();

}
