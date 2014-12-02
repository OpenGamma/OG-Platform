/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import java.util.Set;

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
  private static class DataSource implements MarketDataSource {

    @Override
    public MarketDataResults get(Set<MarketDataRequest> requests) {
      MarketDataResults.Builder builder = MarketDataResults.builder();

      for (MarketDataRequest request : requests) {
        builder.missing(request);
      }
      return builder.build();
    }
  }
}
