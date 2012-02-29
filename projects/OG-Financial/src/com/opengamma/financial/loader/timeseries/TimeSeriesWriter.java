/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.loader.timeseries;

import com.opengamma.id.ExternalId;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

public interface TimeSeriesWriter {

  LocalDateDoubleTimeSeries writeDataPoints(ExternalId htsId, String dataSource, String dataProvider, String dataField, 
      String observationTime, LocalDateDoubleTimeSeries series);

  void flush();

}
