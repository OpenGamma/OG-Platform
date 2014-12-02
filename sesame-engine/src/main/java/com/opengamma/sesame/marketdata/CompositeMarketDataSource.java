/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;

/**
 * Market data source that gets data from multiple underlying sources.
 */
public class CompositeMarketDataSource implements MarketDataSource {

  private final List<MarketDataSource> _dataSources;

  /**
   * @param dataSources the underlying data sources that provide the data
   */
  public CompositeMarketDataSource(List<MarketDataSource> dataSources) {
    _dataSources = ImmutableList.copyOf(dataSources);
  }

  @Override
  public MarketDataResults get(Set<MarketDataRequest> requests) {
    MarketDataResults.Builder builder = MarketDataResults.builder();
    Set<MarketDataRequest> outstandingRequests = requests;

    for (MarketDataSource dataSource : _dataSources) {
      MarketDataResults response = dataSource.get(outstandingRequests);
      outstandingRequests = response.getUnavailableRequests();
      builder.addAll(response);
    }
    return builder.build();
  }
}
