/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.util.Map;
import java.util.Set;

import org.threeten.bp.LocalDate;

import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoader;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoaderRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoaderResult;
import com.opengamma.util.ArgumentChecker;

/**
 * Abstract implementation of a loader of time-series information.
 * <p>
 * This provides default implementations of the interface methods that delegate to a
 * protected method that subclasses must implement.
 */
public abstract class AbstractHistoricalTimeSeriesLoader implements HistoricalTimeSeriesLoader {

  /**
   * Creates an instance.
   */
  protected AbstractHistoricalTimeSeriesLoader() {
  }

  //-------------------------------------------------------------------------
  @Override
  public Map<ExternalId, UniqueId> loadTimeSeries(
      Set<ExternalId> identifiers, String dataProvider, String dataField, LocalDate startDate, LocalDate endDate) {
    HistoricalTimeSeriesLoaderRequest request = HistoricalTimeSeriesLoaderRequest.create(identifiers, dataProvider, dataField, startDate, endDate);
    HistoricalTimeSeriesLoaderResult result = loadTimeSeries(request);
    return result.getResultMap();
  }

  @Override
  public HistoricalTimeSeriesLoaderResult loadTimeSeries(HistoricalTimeSeriesLoaderRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    // short-cut empty case
    if (request.getExternalIds().isEmpty()) {
      return new HistoricalTimeSeriesLoaderResult();
    }
    
    // get securities
    return doBulkLoad(request);
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the time-series.
   * 
   * @param request  the request, with a non-empty list of IDs, not null
   * @return the result, not null
   */
  protected abstract HistoricalTimeSeriesLoaderResult doBulkLoad(HistoricalTimeSeriesLoaderRequest request);

}
