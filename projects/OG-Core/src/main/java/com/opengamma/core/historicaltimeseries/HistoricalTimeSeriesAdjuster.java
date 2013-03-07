/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries;

import com.opengamma.id.ExternalIdBundle;

/**
 * Adjusts the data points of an historical time-series.
 */
public interface HistoricalTimeSeriesAdjuster {

  /**
   * Applies the adjustment to an historical time-series.
   * 
   * @param securityIdBundle the security identifiers associated with the time-series, not null
   * @param timeSeries the time-series, not null
   * @return the adjusted time-series, not null
   */
  HistoricalTimeSeries adjust(ExternalIdBundle securityIdBundle, HistoricalTimeSeries timeSeries);

  /**
   * Produces the adjustment operation for a given time series.
   * 
   * @param securityIdBundle the security identifiers associated with the time-series, not null
   * @return the adjustment operation, not null
   */
  HistoricalTimeSeriesAdjustment getAdjustment(ExternalIdBundle securityIdBundle);

}
