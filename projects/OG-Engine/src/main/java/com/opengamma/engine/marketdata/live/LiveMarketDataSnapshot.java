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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.opengamma.engine.marketdata.AbstractMarketDataSnapshot;
import com.opengamma.engine.marketdata.InMemoryLKVMarketDataSnapshot;
import com.opengamma.engine.marketdata.MarketDataListener;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;

/**
 * A {@link MarketDataSnapshot} for live data.
 */
public class LiveMarketDataSnapshot extends AbstractMarketDataSnapshot {
  private static final Logger s_logger = LoggerFactory.getLogger(LiveMarketDataSnapshot.class);

  private final InMemoryLKVMarketDataSnapshot _underlyingSnapshot;
  private final LiveMarketDataProvider _liveMarketDataProvider;

  public LiveMarketDataSnapshot(final InMemoryLKVMarketDataSnapshot underlyingSnapshot,
      final LiveMarketDataProvider liveMarketDataProvider) {
    _underlyingSnapshot = underlyingSnapshot;
    _liveMarketDataProvider = liveMarketDataProvider;
  }

  @Override
  public UniqueId getUniqueId() {
    return UniqueId.of(MARKET_DATA_SNAPSHOT_ID_SCHEME, "LiveMarketDataSnapshot:" + getSnapshotTime());
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
  public void init(final Set<ValueSpecification> values, final long timeout, final TimeUnit unit) {
    if (values != null && !values.isEmpty()) {
      final Set<ValueSpecification> unavailable = Collections
          .newSetFromMap(new ConcurrentHashMap<ValueSpecification, Boolean>());
      unavailable.addAll(values);
      final CountDownLatch awaitingValuesLatch = new CountDownLatch(1);
      final MarketDataListener listener = new MarketDataListener() {

        @Override
        public void subscriptionsSucceeded(final Collection<ValueSpecification> valueSpecifications) {
        }

        @Override
        public void subscriptionFailed(final ValueSpecification valueSpecification, final String msg) {
        }

        @Override
        public void subscriptionStopped(final ValueSpecification valueSpecification) {
        }

        @Override
        public void valuesChanged(final Collection<ValueSpecification> valueSpecifications) {
          unavailable.removeAll(valueSpecifications);
          if (unavailable.isEmpty()) {
            awaitingValuesLatch.countDown();
          }
        }
      };
      _liveMarketDataProvider.addListener(listener);
      try {
        _underlyingSnapshot.init(); // TODO We need something to query, but snapshotting twice is a bit overkill
        for (final ValueSpecification value : values) {
          if (_underlyingSnapshot.query(value) != null) {
            unavailable.remove(value);
          } else if (_liveMarketDataProvider.isActive(value)) { //PLAT-1429
            unavailable.remove(value);
          }
        }
        if (!unavailable.isEmpty()) {
          try {
            if (!awaitingValuesLatch.await(timeout, unit)) {
              s_logger.warn(MessageFormat.format(
                  "Timed out while waiting {0} {1} for required values to become available: {2}", timeout, unit,
                  unavailable));
            }
          } catch (final InterruptedException e) {
            s_logger.warn(MessageFormat.format(
                "Interrupted while waiting for required values to become available: {0}", unavailable), e);
          }
        }
      } finally {
        _liveMarketDataProvider.removeListener(listener);
      }
    }
    _underlyingSnapshot.init(values, timeout, unit);
  }

  @Override
  public boolean isInitialized() {
    return _underlyingSnapshot.isInitialized();
  }

  @Override
  public boolean isEmpty() {
    assertInitialized();
    return _underlyingSnapshot.isEmpty();
  }

  @Override
  public Instant getSnapshotTime() {
    return _underlyingSnapshot.getSnapshotTime();
  }

  @Override
  public Object query(final ValueSpecification value) {
    //TODO: return useful error message if failed
    return _underlyingSnapshot.query(value);
  }

}
