/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.timeseries.reader;

import com.opengamma.integration.copier.timeseries.writer.TimeSeriesWriter;

/**
 * The interface for a time series reader. Must be able to write whatever it reads to a time series writer.
 * (This tight linkage between reader and writer might have to change)
 */
public interface TimeSeriesReader {

  void writeTo(TimeSeriesWriter timeseriesWriter);
  
}
