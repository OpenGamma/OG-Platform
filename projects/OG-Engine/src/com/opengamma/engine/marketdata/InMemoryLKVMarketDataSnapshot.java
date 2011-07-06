/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.value.ValueRequirement;

/**
 * An implementation of {@link MarketDataSnapshot} backed by an {@link InMemoryLKVMarketDataProvider}.
 */
public class InMemoryLKVMarketDataSnapshot implements MarketDataSnapshot {

  private static final Logger s_logger = LoggerFactory.getLogger(InMemoryLKVMarketDataSnapshot.class);
  
  private final InMemoryLKVMarketDataProvider _provider;
  private Instant _snapshotTime;
  private Map<ValueRequirement, Object> _snapshot;
  
  public InMemoryLKVMarketDataSnapshot(InMemoryLKVMarketDataProvider provider) {
    _provider = provider;
  }
  
  @Override
  public Instant getSnapshotTimeIndication() {
    return _snapshotTime != null ? _snapshotTime : Instant.now();
  }
  
  @Override
  public void init() {
    init(null, 0, null);
  }

  @Override
  public void init(Set<ValueRequirement> valuesRequired, long timeout, TimeUnit unit) {
    if (valuesRequired != null && !valuesRequired.isEmpty()) {
      final Set<ValueRequirement> unavailableRequirements = Collections.newSetFromMap(new ConcurrentHashMap<ValueRequirement, Boolean>());
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
        for (ValueRequirement requirement : valuesRequired) {
          if (getProvider().isAvailable(requirement)) {
            unavailableRequirements.remove(requirement);
          }
        }
        if (!unavailableRequirements.isEmpty()) {
          try {
            awaitingValuesLatch.await(timeout, unit);
          } catch (InterruptedException e) {
            s_logger.warn("Interrupted while waiting for required values to become available", e);
          }
        }
      } finally {
        getProvider().removeListener(listener); 
      }
    }
    _snapshot = getProvider().doSnapshot();
    _snapshotTime = Instant.now();
  }

  @Override
  public Instant getSnapshotTime() {
    return _snapshotTime;
  }

  @Override
  public Object query(ValueRequirement requirement) {
    return getSnapshot().get(requirement);
  }

  //-------------------------------------------------------------------------
  public Set<ValueRequirement> getAllValueKeys() {
    return Collections.unmodifiableSet(getSnapshot().keySet());
  }
  
  //-------------------------------------------------------------------------
  private Map<ValueRequirement, Object> getSnapshot() {
    if (_snapshot == null) {
      throw new IllegalStateException("Snapshot has not been initialised");
    }
    return _snapshot;
  }
  
  private InMemoryLKVMarketDataProvider getProvider() {
    return _provider;
  }

}
