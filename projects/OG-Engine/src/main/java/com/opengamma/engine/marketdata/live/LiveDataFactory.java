/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

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

  // [PLAT-3044] The availabilityProvider must perform suitable resolution of external id bundles etc.

  public LiveDataFactory(final LiveDataClient liveDataClient,
      final MarketDataAvailabilityProvider availabilityProvider) {
    ArgumentChecker.notNull(liveDataClient, "liveDataClient");
    ArgumentChecker.notNull(availabilityProvider, "availabilityProvider");
    _liveDataClient = liveDataClient;
    _availabilityProvider = availabilityProvider;
  }

  /* package */ LiveMarketDataProvider create(final UserPrincipal user) {
    return new LiveMarketDataProvider(_liveDataClient, _availabilityProvider, user);
  }
}
