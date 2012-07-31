/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.provider.historicaltimeseries.impl;

import java.util.Set;

import javax.time.calendar.LocalDate;

import com.opengamma.financial.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetResult;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.time.LocalDateRange;

/**
 * Simple implementation of a provider of time-series that finds nothing.
 */
public class NoneFoundHistoricalTimeSeriesProvider extends AbstractHistoricalTimeSeriesProvider {

  /**
   * Creates an instance.
   */
  public NoneFoundHistoricalTimeSeriesProvider() {
    super(".*", LocalDate.MIN_DATE);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the time-series.
   * 
   * @param externalIdBundleSet  a set containing an identifier bundle for each time-series required, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the data field, not null
   * @param dateRange  the date range to obtain, not null
   * @param isLatestOnly  true to get the l
   * @return a map of each supplied identifier bundle to the corresponding time-series, not null
   */
  protected HistoricalTimeSeriesProviderGetResult doBulkGet(
      Set<ExternalIdBundle> externalIdBundleSet, String dataProvider, String dataField, LocalDateRange dateRange, boolean isLatestOnly) {
    return new HistoricalTimeSeriesProviderGetResult();
  }

}
