/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.loader.timeseries;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.loader.PortfolioLoaderTool;
import com.opengamma.id.ExternalId;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

public class DummyTimeSeriesWriter implements TimeSeriesWriter {

  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioLoaderTool.class);

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
