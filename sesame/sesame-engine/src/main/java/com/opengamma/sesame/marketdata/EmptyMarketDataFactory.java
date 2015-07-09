/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.opengamma.util.result.FailureStatus;
import com.opengamma.util.result.Result;

/**
 * Market data factory that never returns any data.
 */
public class EmptyMarketDataFactory implements MarketDataFactory<EmptyMarketDataSpec> {

  @Override
  public Class<EmptyMarketDataSpec> getSpecificationType() {
    return EmptyMarketDataSpec.class;
  }

  @Override
  public MarketDataSource create(EmptyMarketDataSpec spec) {
    return new DataSource();
  }

  /**
   * Data source that never returns any data.
   */
  public static class DataSource implements MarketDataSource {

    @Override
    public Map<MarketDataRequest, Result<?>> get(Set<MarketDataRequest> requests) {
      ImmutableMap.Builder<MarketDataRequest, Result<?>> builder = ImmutableMap.builder();

      for (MarketDataRequest request : requests) {
        builder.put(request, Result.failure(FailureStatus.MISSING_DATA, "No data available for {}", request));
      }
      return builder.build();
    }
  }
}
