/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.opengamma.util.ArgumentChecker;

/**
 * Market data source that gets data from multiple underlying sources.
 */
public class CompositeMarketDataSource implements MarketDataSource {

  private final List<MarketDataSource> _dataSources;

  public CompositeMarketDataSource(List<MarketDataSource> dataSources) {
    _dataSources = new ArrayList<>(ArgumentChecker.notNull(dataSources, "dataSources"));
  }

  @Override
  public MarketDataResponse get(Set<MarketDataRequest> requests) {
    MarketDataResponse.Builder builder = MarketDataResponse.builder();
    Set<MarketDataRequest> outstandingRequests = new HashSet<>(ArgumentChecker.notNull(requests, "requests"));

    for (MarketDataSource dataSource : _dataSources) {
      MarketDataResponse response = dataSource.get(outstandingRequests);
      builder.addAll(response);
    }
    return builder.build();
  }
}
