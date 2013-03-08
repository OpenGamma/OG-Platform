/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.threeten.bp.Instant;

import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * Snapshot of market data which aggregates data from multiple underlying snapshots.
 */
/* package */ class CompositeMarketDataSnapshot implements MarketDataSnapshot {

  /** The underlying snapshots. */
  private final List<MarketDataSnapshot> _snapshots;
  /** Supplies the current set of subscriptions for the underlying snapshots in the same order as the snapshots. */
  private final Supplier<List<Set<ValueRequirement>>> _subscriptionSupplier;

  /**
   * @param snapshots The underlying snapshots
   * @param subscriptionSupplier Supplies the current set of subscriptions for the underlying snapshots
   * in the same order as the snapshots. This snapshot is created before subscriptions are set up but the subscriptions
   * are available by the time this snapshot needs to use them. So the subscriptions must be requested from the
   * supplier when they're required.
   */
  /* package */ CompositeMarketDataSnapshot(List<MarketDataSnapshot> snapshots,
                                            Supplier<List<Set<ValueRequirement>>> subscriptionSupplier) {
    ArgumentChecker.notEmpty(snapshots, "snapshots");
    ArgumentChecker.notNull(subscriptionSupplier, "subscriptionSupplier");
    _subscriptionSupplier = subscriptionSupplier;
    _snapshots = snapshots;
  }

  @Override
  public UniqueId getUniqueId() {
    // TODO is this unique enough? same as in the existing impls
    return UniqueId.of(MARKET_DATA_SNAPSHOT_ID_SCHEME, "CompositeMarketDataSnapshot:" + getSnapshotTime());
  }

  /**
   * @return The first non-null indication of snapshot time from the underlying snapshots
   * @throws IllegalStateException If none of the underlying snapshots return a value
   */
  @Override
  public Instant getSnapshotTimeIndication() {
    for (MarketDataSnapshot snapshot : _snapshots) {
      Instant snapshotTimeIndication = snapshot.getSnapshotTimeIndication();
      if (snapshotTimeIndication != null) {
        return snapshotTimeIndication;
      }
    }
    throw new IllegalStateException("None of the underlying snapshots returned a snapshot time indication");
  }

  /**
   * Initializes all of the underlying snapshots.
   */
  @Override
  public void init() {
    for (MarketDataSnapshot snapshot : _snapshots) {
      snapshot.init();
    }
  }

  /**
   * Initializes the underlying snapshots.
   * @param requirements  the values required in the snapshot, not null
   * @param timeout  the maximum time to wait for the required values
   * @param unit  the timeout unit, not null
   */
  @Override
  public void init(Set<ValueRequirement> requirements, long timeout, TimeUnit unit) {
    ArgumentChecker.notNull(requirements, "requirements");
    ArgumentChecker.notNull(unit, "unit");
    List<Set<ValueRequirement>> subscriptions = _subscriptionSupplier.get();
    for (int i = 0; i < _snapshots.size(); i++) {
      MarketDataSnapshot snapshot = _snapshots.get(i);
      Set<ValueRequirement> snapshotSubscriptions = subscriptions.get(i);
      // TODO whole timeout? or divide timeout between all delegate snapshots and keep track of how much is left?
      // the combined snapshot does this but that seems broken to me
      Set<ValueRequirement> snapshotRequirements = Sets.intersection(snapshotSubscriptions, requirements);
      if (snapshotRequirements.isEmpty()) {
        snapshot.init();
      } else {
        snapshot.init(snapshotSubscriptions, timeout, unit);
      }
    }
  }
  
  @Override
  public boolean isInitialized() {
    for (MarketDataSnapshot snapshot : _snapshots) {
      if (!snapshot.isInitialized()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean isEmpty() {
    if (!isInitialized()) {
      throw new IllegalStateException("Market data snapshot is not initialized");
    }
    for (MarketDataSnapshot snapshot : _snapshots) {
      if (!snapshot.isEmpty()) {
        return false;
      }
    }
    return true;
  }

  /**
   * @return The first non-null snapshot time from the underlying snapshots
   * @throws IllegalStateException If none of the underlying snapshots return a value
   */
  @Override
  public Instant getSnapshotTime() {
    for (MarketDataSnapshot snapshot : _snapshots) {
      Instant snapshotTime = snapshot.getSnapshotTime();
      if (snapshotTime != null) {
        return snapshotTime;
      }
    }
    throw new IllegalStateException("None of the underlying snapshots returned a snapshot time");
  }

  /**
   * Returns the value from one of the underlying snapshots or null if it isn't available in any of them
   * @param requirement  the value required, not null
   * @return The value from one of the underlying snapshots or null if it isn't available in any of them
   */
  @Override
  public ComputedValue query(ValueRequirement requirement) {
    ArgumentChecker.notNull(requirement, "requirement");
    List<Set<ValueRequirement>> subscriptions = _subscriptionSupplier.get();
    for (int i = 0; i < _snapshots.size(); i++) {
      MarketDataSnapshot snapshot = _snapshots.get(i);
      Set<ValueRequirement> snapshotSubscriptions = subscriptions.get(i);
      if (snapshotSubscriptions.contains(requirement)) {
        return snapshot.query(requirement);
      }
    }
    return null;
  }

  /**
   * Returns the values from the underlying snapshots if they are available
   * @param requirements the values required, not null
   * @return The values from the underlying snapshots if they are available, values that aren't available will be
   * missing from the results map
   */
  @Override
  public Map<ValueRequirement, ComputedValue> query(Set<ValueRequirement> requirements) {
    ArgumentChecker.notNull(requirements, "requirements");
    Map<ValueRequirement, ComputedValue> results = Maps.newHashMapWithExpectedSize(requirements.size());
    List<Set<ValueRequirement>> subscriptions = _subscriptionSupplier.get();
    for (int i = 0; i < _snapshots.size(); i++) {
      MarketDataSnapshot snapshot = _snapshots.get(i);
      Set<ValueRequirement> snapshotSubscriptions = subscriptions.get(i);
      Set<ValueRequirement> snapshotRequirements = Sets.intersection(snapshotSubscriptions, requirements);
      if (!snapshotRequirements.isEmpty()) {
        Map<ValueRequirement, ComputedValue> snapshotValues = snapshot.query(snapshotRequirements);
        results.putAll(snapshotValues);
      }
    }
    return results;
  }

}
