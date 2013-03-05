/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityFilter;
import com.opengamma.livedata.LiveDataClient;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * Factory for building {@link LiveMarketDataProvider} instances.
 */
public class LiveDataFactory {

  private final LiveDataClient _liveDataClient;
  private final MarketDataAvailabilityFilter _availabilityFilter;

  /**
   * Creates a new factory.
   * 
   * @param liveDataClient the live data client to use to source data values
   * @param availabilityFilter the filter describing which values to source from this live data client
   */
  public LiveDataFactory(final LiveDataClient liveDataClient, final MarketDataAvailabilityFilter availabilityFilter) {
    ArgumentChecker.notNull(liveDataClient, "liveDataClient");
    ArgumentChecker.notNull(availabilityFilter, "availabilityFilter");
    _liveDataClient = liveDataClient;
    _availabilityFilter = availabilityFilter;
  }

  /* package */LiveMarketDataProvider create(final UserPrincipal user) {
    return new LiveMarketDataProvider(_liveDataClient, _availabilityFilter, user);
  }
}
