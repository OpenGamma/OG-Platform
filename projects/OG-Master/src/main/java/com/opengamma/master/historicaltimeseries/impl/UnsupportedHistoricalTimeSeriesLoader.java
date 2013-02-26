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

/**
 * Simple implementation of a loader that is unsupported.
 */
public class UnsupportedHistoricalTimeSeriesLoader implements HistoricalTimeSeriesLoader {

  /**
   * Creates an instance.
   */
  public UnsupportedHistoricalTimeSeriesLoader() {
    super();
  }

  //-------------------------------------------------------------------------
  @Override
  public Map<ExternalId, UniqueId> addTimeSeries(Set<ExternalId> identifiers, String dataProvider, String dataField, LocalDate startDate, LocalDate endDate) {
    throw new UnsupportedOperationException("Historical Time Series loading is not supported");
  }

  @Override
  public boolean updateTimeSeries(UniqueId uniqueId) {
    throw new UnsupportedOperationException("Historical Time Series loading is not supported");
  }

}
