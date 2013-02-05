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
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * A factory for {@link UserMarketDataProvider} instances.
 */
public class UserMarketDataProviderFactory implements MarketDataProviderFactory {

  private final MarketDataSnapshotSource _snapshotSource;
  private final MarketDataAvailabilityProvider _baseMarketDataAvailabilityProvider;

  public UserMarketDataProviderFactory(final MarketDataSnapshotSource snapshotSource) {
    this(snapshotSource, null);
  }

  public UserMarketDataProviderFactory(final MarketDataSnapshotSource snapshotSource, final MarketDataAvailabilityProvider baseMarketDataAvailabilityProvider) {
    ArgumentChecker.notNull(snapshotSource, "snapshotSource");
    _snapshotSource = snapshotSource;
    _baseMarketDataAvailabilityProvider = baseMarketDataAvailabilityProvider;
  }

  @Override
  public MarketDataProvider create(final UserPrincipal marketDataUser,
                                   final MarketDataSpecification marketDataSpec) {
    final UserMarketDataSpecification userMarketDataSpec = (UserMarketDataSpecification) marketDataSpec;
    final UserMarketDataProvider marketDataProvider = new UserMarketDataProvider(getSnapshotSource(), userMarketDataSpec.getUserSnapshotId());
    marketDataProvider.setBaseMarketDataAvailabilityProvider(getBaseMarketDataAvailabilityProvider());
    return marketDataProvider;
  }

  private MarketDataSnapshotSource getSnapshotSource() {
    return _snapshotSource;
  }

  private MarketDataAvailabilityProvider getBaseMarketDataAvailabilityProvider() {
    return _baseMarketDataAvailabilityProvider;
  }

}
