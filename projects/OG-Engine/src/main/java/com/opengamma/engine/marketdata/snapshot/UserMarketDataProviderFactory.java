/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.snapshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataProviderFactory;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityFilter;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * A factory for {@link UserMarketDataProvider} instances.
 */
public class UserMarketDataProviderFactory implements MarketDataProviderFactory {

  private static final Logger s_logger = LoggerFactory.getLogger(UserMarketDataProviderFactory.class);

  private final MarketDataSnapshotSource _snapshotSource;

  public UserMarketDataProviderFactory(final MarketDataSnapshotSource snapshotSource) {
    ArgumentChecker.notNull(snapshotSource, "snapshotSource");
    _snapshotSource = snapshotSource;
  }

  /**
   * Creates a market data provider based on snapshots.
   * 
   * @param snapshotSource the snapshots, not null
   * @param baseMarketDataAvailability ignored (see PLAT-1459)
   * @deprecated baseMarketDataAvailability not used, see PLAT-1459
   */
  @Deprecated
  public UserMarketDataProviderFactory(final MarketDataSnapshotSource snapshotSource, final MarketDataAvailabilityFilter baseMarketDataAvailability) {
    this(snapshotSource);
    s_logger.error("Call to deprecated constructor (see PLAT-1459)");
  }

  @Override
  public MarketDataProvider create(final UserPrincipal marketDataUser, final MarketDataSpecification marketDataSpec) {
    final UserMarketDataSpecification userMarketDataSpec = (UserMarketDataSpecification) marketDataSpec;
    final UserMarketDataProvider marketDataProvider = new UserMarketDataProvider(getSnapshotSource(), userMarketDataSpec.getUserSnapshotId());
    return marketDataProvider;
  }

  private MarketDataSnapshotSource getSnapshotSource() {
    return _snapshotSource;
  }

}
