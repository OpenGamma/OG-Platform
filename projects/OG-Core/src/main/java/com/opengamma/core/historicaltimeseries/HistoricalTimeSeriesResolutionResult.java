/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries;

import com.opengamma.util.PublicSPI;

/**
 * Contains the result of resolving an historical time-series.
 * <p>
 * Time-series whose data points are derived from the same points of another time-series may be exposed by referencing the parent time-series and including an adjuster. The adjuster is expected to be
 * applied to the relevant part of the parent time-series in order to satisfy the resolution request.
 * <p>
 * For example, an adjuster may be provided to apply normalization rules to a parent time-series that contains unnormalized data.
 */
@PublicSPI
public class HistoricalTimeSeriesResolutionResult {

  private final HistoricalTimeSeriesInfo _historicalTimeSeriesInfo;
  private final HistoricalTimeSeriesAdjuster _adjuster;

  public HistoricalTimeSeriesResolutionResult(HistoricalTimeSeriesInfo historicalTimeSeriesInfo) {
    this(historicalTimeSeriesInfo, null);
  }

  public HistoricalTimeSeriesResolutionResult(HistoricalTimeSeriesInfo historicalTimeSeriesInfo, HistoricalTimeSeriesAdjuster adjuster) {
    _historicalTimeSeriesInfo = historicalTimeSeriesInfo;
    _adjuster = adjuster;
  }

  /**
   * Gets the historical time-series information from which the full time-series may be queried.
   * 
   * @return the historical time-series information, not null
   */
  public HistoricalTimeSeriesInfo getHistoricalTimeSeriesInfo() {
    return _historicalTimeSeriesInfo;
  }

  /**
   * Get the adjuster to be applied to the resolved historical time-series.
   * 
   * @return the adjuster, or null if no adjustment is required
   */
  public HistoricalTimeSeriesAdjuster getAdjuster() {
    return _adjuster;
  }

}
