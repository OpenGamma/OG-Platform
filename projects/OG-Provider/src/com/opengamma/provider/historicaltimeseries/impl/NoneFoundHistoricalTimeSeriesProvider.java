/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.historicaltimeseries.impl;

import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetRequest;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetResult;

/**
 * Simple implementation of a provider of time-series that finds nothing.
 */
public class NoneFoundHistoricalTimeSeriesProvider extends AbstractHistoricalTimeSeriesProvider {

  /**
   * Creates an instance.
   */
  public NoneFoundHistoricalTimeSeriesProvider() {
  }

  //-------------------------------------------------------------------------
  @Override
  protected HistoricalTimeSeriesProviderGetResult doBulkGet(HistoricalTimeSeriesProviderGetRequest request) {
    return new HistoricalTimeSeriesProviderGetResult();
  }

}
