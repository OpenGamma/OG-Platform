/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoaderRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoaderResult;

/**
 * Simple implementation of a loader that is unsupported.
 */
public class UnsupportedHistoricalTimeSeriesLoader extends AbstractHistoricalTimeSeriesLoader {

  /**
   * Creates an instance.
   */
  public UnsupportedHistoricalTimeSeriesLoader() {
    super();
  }

  //-------------------------------------------------------------------------
  @Override
  protected HistoricalTimeSeriesLoaderResult doBulkLoad(HistoricalTimeSeriesLoaderRequest request) {
    throw new UnsupportedOperationException("Historical Time Series loading is not supported");
  }

  @Override
  public boolean updateTimeSeries(UniqueId uniqueId) {
    throw new UnsupportedOperationException("Historical Time Series update is not supported");
  }

}
