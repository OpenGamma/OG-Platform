/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;

/**
 * A factory for {@link UserMarketDataProvider} instances.
 */
public class UserMarketDataProviderFactory implements MarketDataProviderFactory {

  private final MarketDataSnapshotSource _snapshotSource;
  
  public UserMarketDataProviderFactory(MarketDataSnapshotSource snapshotSource) {
    _snapshotSource = snapshotSource;
  }
  
  @Override
  public MarketDataProvider create(MarketDataSpecification marketDataSpec) {
    UserMarketDataSpecification userMarketDataSpec = (UserMarketDataSpecification) marketDataSpec;
    return new UserMarketDataProvider(getSnapshotSource(), userMarketDataSpec.getUserSnapshotId());
  }
  
  //-------------------------------------------------------------------------
  private MarketDataSnapshotSource getSnapshotSource() {
    return _snapshotSource;
  }

}
