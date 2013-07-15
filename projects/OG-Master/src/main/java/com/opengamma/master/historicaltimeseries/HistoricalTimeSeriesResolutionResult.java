/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesAdjuster;
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
public class HistoricalTimeSeriesResolutionResult extends com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesResolutionResult {

  private final ManageableHistoricalTimeSeriesInfo _historicalTimeSeriesInfo;

  public HistoricalTimeSeriesResolutionResult(ManageableHistoricalTimeSeriesInfo historicalTimeSeriesInfo) {
    this(historicalTimeSeriesInfo, null);
  }

  public HistoricalTimeSeriesResolutionResult(ManageableHistoricalTimeSeriesInfo historicalTimeSeriesInfo, HistoricalTimeSeriesAdjuster adjuster) {
    super(historicalTimeSeriesInfo, adjuster);
    _historicalTimeSeriesInfo = historicalTimeSeriesInfo;
  }

  /**
   * Gets the historical time-series information from which the full time-series may be queried.
   * 
   * @return the historical time-series information, not null
   */
  @Override
  public ManageableHistoricalTimeSeriesInfo getHistoricalTimeSeriesInfo() {
    return _historicalTimeSeriesInfo;
  }

}
