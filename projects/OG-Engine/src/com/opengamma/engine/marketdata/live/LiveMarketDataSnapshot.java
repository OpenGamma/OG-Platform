/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.marketdata.AbstractMarketDataSnapshot;
import com.opengamma.engine.marketdata.InMemoryLKVMarketDataSnapshot;
import com.opengamma.engine.marketdata.MarketDataListener;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.value.ValueRequirement;

/**
 * A {@link MarketDataSnapshot} for live data.
 */
public class LiveMarketDataSnapshot extends AbstractMarketDataSnapshot {
  private static final Logger s_logger = LoggerFactory.getLogger(LiveMarketDataSnapshot.class);

  private final InMemoryLKVMarketDataSnapshot _underlyingSnapshot;
  private final LiveMarketDataProvider _liveMarketDataProvider;

  public LiveMarketDataSnapshot(InMemoryLKVMarketDataSnapshot underlyingSnapshot,
      LiveMarketDataProvider liveMarketDataProvider) {
    _underlyingSnapshot = underlyingSnapshot;
    _liveMarketDataProvider = liveMarketDataProvider;
  }

  @Override
  public Instant getSnapshotTimeIndication() {
    return _underlyingSnapshot.getSnapshotTimeIndication();
  }

  @Override
  public void init() {
    _underlyingSnapshot.init();
  }

  @Override
  public void init(Set<ValueRequirement> valuesRequired, long timeout, TimeUnit unit) {
    if (valuesRequired != null && !valuesRequired.isEmpty()) {     
      final Set<ValueRequirement> unavailableRequirements = Collections
          .newSetFromMap(new ConcurrentHashMap<ValueRequirement, Boolean>());
      unavailableRequirements.addAll(valuesRequired);
      final CountDownLatch awaitingValuesLatch = new CountDownLatch(1);
      MarketDataListener listener = new MarketDataListener() {

        @Override
        public void subscriptionSucceeded(ValueRequirement requirement) {
        }

        @Override
        public void subscriptionFailed(ValueRequirement requirement, String msg) {
        }

        @Override
        public void subscriptionStopped(ValueRequirement requirement) {
        }

        @Override
        public void valuesChanged(Collection<ValueRequirement> requirements) {
          unavailableRequirements.removeAll(requirements);
          if (unavailableRequirements.isEmpty()) {
            awaitingValuesLatch.countDown();
          }
        }
      };
      getProvider().addListener(listener);
      try {
        _underlyingSnapshot.init(); // TODO We need something to query, but snapshotting twice is a bit overkill
        for (ValueRequirement requirement : valuesRequired) {
          if (_underlyingSnapshot.query(requirement) != null) {
            unavailableRequirements.remove(requirement);
          } else if (_liveMarketDataProvider.isFailed(requirement)) { //PLAT-1429
            unavailableRequirements.remove(requirement);
          }
        }
        if (!unavailableRequirements.isEmpty()) {
          try {
            if (!awaitingValuesLatch.await(timeout, unit)) {
              s_logger.warn(MessageFormat.format(
                  "Timed out while waiting {0} {1} for required values to become available: {2}", timeout, unit,
                  unavailableRequirements));
            }
          } catch (InterruptedException e) {
            s_logger.warn(MessageFormat.format(
                "Interrupted while waiting for required values to become available: {0}", unavailableRequirements), e);
          }
        }
      } finally {
        getProvider().removeListener(listener);
      }
    }
    _underlyingSnapshot.init(valuesRequired, timeout, unit);
  }

  private LiveMarketDataProvider getProvider() {
    return _liveMarketDataProvider;
  }

  @Override
  public Instant getSnapshotTime() {
    return _underlyingSnapshot.getSnapshotTime();
  }

  @Override
  public Object query(ValueRequirement requirement) {
    //TODO: return useful error message if failed
    return _underlyingSnapshot.query(requirement);
  }
}
