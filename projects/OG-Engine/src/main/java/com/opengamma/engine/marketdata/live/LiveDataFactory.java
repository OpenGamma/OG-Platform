/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityFilter;
import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.LiveDataClient;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.ArgumentChecker;

/**
 * Factory for building {@link LiveMarketDataProvider} instances.
 */
public class LiveDataFactory {

  /** Logger */
  private static final Logger s_logger = LoggerFactory.getLogger(LiveDataFactory.class);

  /** Underlying source of the live data. */
  private final LiveDataClient _liveDataClient;
  /** For checking availability of live data. */
  private final MarketDataAvailabilityFilter _availabilityFilter;
  /**
   * All providers created by this factory. They are accessed via weak references so they will be GCd when they're
   * no longer being used by the engine. This will grow indefinitely unless {@link #resubscribe} is called. If that
   * turns out to be a problem then a periodic task to clean out empty references will be needed.
   */
  private final List<WeakReference<InMemoryLKVLiveMarketDataProvider>> _providers = Lists.newArrayList();
  /** Lock for accessing the list of providers. */
  private final Object _providerListLock = new Object();

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

  /* package */ LiveMarketDataProvider create(final UserPrincipal user) {
    InMemoryLKVLiveMarketDataProvider provider =
        new InMemoryLKVLiveMarketDataProvider(_liveDataClient, _availabilityFilter, user);
    synchronized (_providerListLock) {
      _providers.add(new WeakReference<>(provider));
    }
    return provider;
  }

  /**
   * If a data provider becomes available this method will be invoked with the schemes handled by the provider.
   * This gives market data providers the opportunity to reattempt previously failed subscriptions.
   * @param schemes The schemes for which market data subscriptions should be reattempted.
   */
  /* package */ void resubscribe(Set<ExternalScheme> schemes) {
    synchronized (_providerListLock) {
      s_logger.info("Telling providers to resubscribe to data for schemes: {}", schemes);
      for (Iterator<WeakReference<InMemoryLKVLiveMarketDataProvider>> it = _providers.iterator(); it.hasNext(); ) {
        WeakReference<InMemoryLKVLiveMarketDataProvider> ref = it.next();
        InMemoryLKVLiveMarketDataProvider provider = ref.get();
        if (provider != null) {
          provider.resubscribe(schemes);
        } else {
          it.remove();
        }
      }
    }
  }
}
