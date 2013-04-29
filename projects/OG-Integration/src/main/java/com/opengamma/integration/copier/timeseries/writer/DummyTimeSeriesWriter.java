/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.timeseries.writer;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.id.ExternalId;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 * A dummy time series writer that merely logs the written data points
 */
public class DummyTimeSeriesWriter implements TimeSeriesWriter {

  private static final Logger s_logger = LoggerFactory.getLogger(DummyTimeSeriesWriter.class);

  @Override
  public LocalDateDoubleTimeSeries writeDataPoints(ExternalId htsId, String dataSource, String dataProvider, String dataField, 
      String observationTime, LocalDateDoubleTimeSeries series) {
    s_logger.info("Time Series: (id " + htsId + ", Field " + dataField + ") " + series.timesArray().toString() + Arrays.toString(series.valuesArray()));
    return series;
  }

  @Override
  public void flush() {
  }

}
