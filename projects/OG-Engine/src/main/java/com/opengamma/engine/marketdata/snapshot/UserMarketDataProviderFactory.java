/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.snapshot;

import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataProviderFactory;
import com.opengamma.engine.marketdata.availability.DefaultMarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityFilter;
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
  private final MarketDataAvailabilityProvider _baseMarketDataAvailability;

  public UserMarketDataProviderFactory(final MarketDataSnapshotSource snapshotSource) {
    this(snapshotSource, null);
  }

  /**
   * Creates a market data provider based on snapshots.
   * <p>
   * An additional market data availability filter is available for PLAT-1459. If given an availability filter similar to that used by the live data providers it is possible to build the dependency
   * graph for views that require values that are not in the snapshot. The resulting graphs will not however execute successfully as the snapshot does not contain these values.
   * 
   * @param snapshotSource the snapshots, not null
   * @param baseMarketDataAvailability additional market data availability indicator (see PLAT-1459), or null for none
   */
  public UserMarketDataProviderFactory(final MarketDataSnapshotSource snapshotSource, final MarketDataAvailabilityFilter baseMarketDataAvailability) {
    ArgumentChecker.notNull(snapshotSource, "snapshotSource");
    _snapshotSource = snapshotSource;
    if (baseMarketDataAvailability != null) {
      _baseMarketDataAvailability = baseMarketDataAvailability.withProvider(new DefaultMarketDataAvailabilityProvider());
    } else {
      _baseMarketDataAvailability = null;
    }
  }

  @Override
  public MarketDataProvider create(final UserPrincipal marketDataUser, final MarketDataSpecification marketDataSpec) {
    final UserMarketDataSpecification userMarketDataSpec = (UserMarketDataSpecification) marketDataSpec;
    final UserMarketDataProvider marketDataProvider = new UserMarketDataProvider(getSnapshotSource(), userMarketDataSpec.getUserSnapshotId());
    marketDataProvider.setBaseMarketDataAvailabilityProvider(getBaseMarketDataAvailabilityProvider());
    return marketDataProvider;
  }

  private MarketDataSnapshotSource getSnapshotSource() {
    return _snapshotSource;
  }

  private MarketDataAvailabilityProvider getBaseMarketDataAvailabilityProvider() {
    return _baseMarketDataAvailability;
  }

}
