/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.loader.timeseries;

public interface TimeSeriesReader {

  void writeTo(TimeSeriesWriter timeseriesWriter);
  
}
