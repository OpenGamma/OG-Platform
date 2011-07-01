/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Spring factory bean for {@link UserMarketDataProviderFactory}.
 */
public class UserMarketDataProviderFactoryFactoryBean extends SingletonFactoryBean<UserMarketDataProviderFactory> {

  private MarketDataSnapshotSource _snapshotSource;
  
  public MarketDataSnapshotSource getSnapshotSource() {
    return _snapshotSource;
  }
  
  public void setSnapshotSource(MarketDataSnapshotSource snapshotSource) {
    _snapshotSource = snapshotSource;
  }
  
  @Override
  protected UserMarketDataProviderFactory createObject() {
    ArgumentChecker.notNullInjected(getSnapshotSource(), "snapshotSource");
    return new UserMarketDataProviderFactory(getSnapshotSource());
  }

}
