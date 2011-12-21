/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.snapshot;

import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataProviderFactory;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;

/**
 * A factory for {@link UserMarketDataProvider} instances.
 */
public class UserMarketDataProviderFactory implements MarketDataProviderFactory {

  private final MarketDataSnapshotSource _snapshotSource;
  private final MarketDataAvailabilityProvider _baseMarketDataAvailabilityProvider;
  
  public UserMarketDataProviderFactory(MarketDataSnapshotSource snapshotSource) {
    this(snapshotSource, null);
  }
  
  public UserMarketDataProviderFactory(MarketDataSnapshotSource snapshotSource, MarketDataAvailabilityProvider baseMarketDataAvailabilityProvider) {
    _snapshotSource = snapshotSource;
    _baseMarketDataAvailabilityProvider = baseMarketDataAvailabilityProvider;
  }
  
  @Override
  public MarketDataProvider create(MarketDataSpecification marketDataSpec) {
    UserMarketDataSpecification userMarketDataSpec = (UserMarketDataSpecification) marketDataSpec;
    UserMarketDataProvider marketDataProvider = new UserMarketDataProvider(getSnapshotSource(), userMarketDataSpec.getUserSnapshotId());
    marketDataProvider.setBaseMarketDataAvailabilityProvider(getBaseMarketDataAvailabilityProvider());
    return marketDataProvider;
  }
  
  //-------------------------------------------------------------------------
  private MarketDataSnapshotSource getSnapshotSource() {
    return _snapshotSource;
  }
  
  private MarketDataAvailabilityProvider getBaseMarketDataAvailabilityProvider() {
    return _baseMarketDataAvailabilityProvider;
  }

}
