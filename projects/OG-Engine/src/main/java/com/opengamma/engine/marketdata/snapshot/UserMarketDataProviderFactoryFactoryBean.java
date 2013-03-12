/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.snapshot;

import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityFilter;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Spring factory bean for {@link UserMarketDataProviderFactory}.
 */
public class UserMarketDataProviderFactoryFactoryBean extends SingletonFactoryBean<UserMarketDataProviderFactory> {

  private MarketDataSnapshotSource _snapshotSource;
  private MarketDataAvailabilityFilter _baseMarketDataAvailability;

  public MarketDataSnapshotSource getSnapshotSource() {
    return _snapshotSource;
  }

  public void setSnapshotSource(final MarketDataSnapshotSource snapshotSource) {
    _snapshotSource = snapshotSource;
  }

  public MarketDataAvailabilityFilter getBaseMarketDataAvailability() {
    return _baseMarketDataAvailability;
  }

  public void setBaseMarketDataAvailability(final MarketDataAvailabilityFilter baseMarketDataAvailability) {
    _baseMarketDataAvailability = baseMarketDataAvailability;
  }

  @Override
  protected UserMarketDataProviderFactory createObject() {
    ArgumentChecker.notNullInjected(getSnapshotSource(), "snapshotSource");
    return new UserMarketDataProviderFactory(getSnapshotSource(), getBaseMarketDataAvailability());
  }

}
