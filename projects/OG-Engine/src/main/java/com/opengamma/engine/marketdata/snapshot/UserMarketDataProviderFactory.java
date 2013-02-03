/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.snapshot;

import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.engine.marketdata.ExternalIdBundleLookup;
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
  private final ExternalIdBundleLookup _identifierLookup;
  private final MarketDataAvailabilityProvider _baseMarketDataAvailabilityProvider;

  public UserMarketDataProviderFactory(MarketDataSnapshotSource snapshotSource, ExternalIdBundleLookup identifierLookup) {
    this(snapshotSource, identifierLookup, null);
  }

  public UserMarketDataProviderFactory(MarketDataSnapshotSource snapshotSource, ExternalIdBundleLookup identifierLookup, MarketDataAvailabilityProvider baseMarketDataAvailabilityProvider) {
    ArgumentChecker.notNull(snapshotSource, "snapshotSource");
    ArgumentChecker.notNull(identifierLookup, "identifierLookup");
    _snapshotSource = snapshotSource;
    _identifierLookup = identifierLookup;
    _baseMarketDataAvailabilityProvider = baseMarketDataAvailabilityProvider;
  }

  @Override
  public MarketDataProvider create(UserPrincipal marketDataUser,
                                   MarketDataSpecification marketDataSpec) {
    UserMarketDataSpecification userMarketDataSpec = (UserMarketDataSpecification) marketDataSpec;
    UserMarketDataProvider marketDataProvider = new UserMarketDataProvider(getSnapshotSource(), userMarketDataSpec.getUserSnapshotId(), getIdentifierLookup());
    marketDataProvider.setBaseMarketDataAvailabilityProvider(getBaseMarketDataAvailabilityProvider());
    return marketDataProvider;
  }

  private MarketDataSnapshotSource getSnapshotSource() {
    return _snapshotSource;
  }

  private ExternalIdBundleLookup getIdentifierLookup() {
    return _identifierLookup;
  }

  private MarketDataAvailabilityProvider getBaseMarketDataAvailabilityProvider() {
    return _baseMarketDataAvailabilityProvider;
  }

}
