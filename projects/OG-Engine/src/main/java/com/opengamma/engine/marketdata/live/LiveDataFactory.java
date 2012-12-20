/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.livedata.LiveDataClient;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * Factory for building {@link LiveMarketDataProvider} instances.
 */
public class LiveDataFactory {

  private final LiveDataClient _liveDataClient;
  private final MarketDataAvailabilityProvider _availabilityProvider;
  private final SecuritySource _securitySource;

  public LiveDataFactory(LiveDataClient liveDataClient,
                         MarketDataAvailabilityProvider availabilityProvider,
                         SecuritySource securitySource) {
    ArgumentChecker.notNull(liveDataClient, "liveDataClient");
    ArgumentChecker.notNull(availabilityProvider, "availabilityProvider");
    ArgumentChecker.notNull(securitySource, "securitySource");
    _liveDataClient = liveDataClient;
    _availabilityProvider = availabilityProvider;
    _securitySource = securitySource;
  }

  /* package */ LiveMarketDataProvider create(UserPrincipal user) {
    return new LiveMarketDataProvider(_liveDataClient, _availabilityProvider, _securitySource, user);
  }
}
