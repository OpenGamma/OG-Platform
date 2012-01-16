/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.id.ExternalIdBundle;

/**
 * Adjusts the data points of an historical time-series.
 */
public interface HistoricalTimeSeriesAdjuster {

  /**
   * Applies the adjustment to an historical time-series.
   * 
   * @param securityIdBundle  the security identifiers associated with the time-series, not null
   * @param timeSeries  the time-series, not null
   * @return  the adjusted time-series, not null
   */
  HistoricalTimeSeries adjust(ExternalIdBundle securityIdBundle, HistoricalTimeSeries timeSeries);
  
}
