/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame;

import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.sesame.marketdata.MarketDataFactory;
import com.opengamma.sesame.marketdata.MarketDataSource;

/**
 * Test market data factory that always returns the same data source when passed any market data specification.
 */
public class TestMarketDataFactory implements MarketDataFactory<LiveMarketDataSpecification> {

  private final MarketDataSource _dataSource;

  /**
   * @param dataSource the data source that is always returned from {@link #create(LiveMarketDataSpecification)}.
   */
  public TestMarketDataFactory(MarketDataSource dataSource) {
    _dataSource = dataSource;
  }

  @Override
  public Class<LiveMarketDataSpecification> getSpecificationType() {
    return LiveMarketDataSpecification.class;
  }

  @Override
  public MarketDataSource create(LiveMarketDataSpecification spec) {
    return _dataSource;
  }
}
