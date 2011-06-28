/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.resolver;

import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.spec.MarketDataSnapshotSpecification;

/**
 * Implements {@link MarketDataProviderResolver} by providing a single {@link MarketDataProvider}.
 */
public class SingleMarketDataProviderResolver implements MarketDataProviderResolver {

  private final MarketDataProvider _provider;
  
  public SingleMarketDataProviderResolver(MarketDataProvider provider) {
    _provider = provider;
  }
  
  @Override
  public MarketDataProvider resolve(MarketDataSnapshotSpecification snapshotSpec) {
    return _provider;
  }

}
