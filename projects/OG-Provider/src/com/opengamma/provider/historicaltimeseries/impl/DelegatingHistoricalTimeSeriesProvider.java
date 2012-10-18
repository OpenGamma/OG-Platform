/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.historicaltimeseries.impl;

import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProvider;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetRequest;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetResult;

/**
 * A historical time-series provider that delegates based on the data source in the request.
 */
public class DelegatingHistoricalTimeSeriesProvider extends AbstractHistoricalTimeSeriesProvider {

  /**
   * The delegating providers.
   */
  private final ImmutableMap<String, HistoricalTimeSeriesProvider> _providers;

  /**
   * Creates an instance.
   * 
   * @param providers  the providers to use, keyed by data source, not null
   */
  public DelegatingHistoricalTimeSeriesProvider(Map<String, HistoricalTimeSeriesProvider> providers) {
    _providers = ImmutableMap.copyOf(providers);
  }

  //-------------------------------------------------------------------------
  @Override
  protected HistoricalTimeSeriesProviderGetResult doBulkGet(HistoricalTimeSeriesProviderGetRequest request) {
    HistoricalTimeSeriesProvider underlying = _providers.get(request.getDataSource());
    if (underlying == null) {
      throw new IllegalArgumentException("Unknown data source: " + request.getDataSource());
    }
    return underlying.getHistoricalTimeSeries(request);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return DelegatingHistoricalTimeSeriesProvider.class.getSimpleName() + ImmutableList.copyOf(_providers.keySet());
  }

}
