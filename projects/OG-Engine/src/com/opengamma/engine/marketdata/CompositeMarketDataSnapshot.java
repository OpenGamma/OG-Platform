/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.time.Instant;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class CompositeMarketDataSnapshot implements MarketDataSnapshot {

  private final List<UnderlyingSnapshot> _snapshots;

  /* package */ CompositeMarketDataSnapshot(List<UnderlyingSnapshot> snapshots) {
    ArgumentChecker.notEmpty(snapshots, "snapshots");
    _snapshots = snapshots;
  }

  @Override
  public UniqueId getUniqueId() {
    // TODO is this unique enough? same as in the existing impls
    return UniqueId.of(MARKET_DATA_SNAPSHOT_ID_SCHEME, "CompositeMarketDataSnapshot:" + getSnapshotTime());
  }

  @Override
  public Instant getSnapshotTimeIndication() {
    return _snapshots.get(0).getSnapshot().getSnapshotTimeIndication();
  }

  /**
   * Initializes all of the underlying snapshots.
   */
  @Override
  public void init() {
    for (UnderlyingSnapshot snapshot : _snapshots) {
      snapshot.getSnapshot().init();
    }
  }

  /**
   * Initializes the underlying snapshots
   * @param valuesRequired  the values required in the snapshot, not null
   * @param timeout  the maximum time to wait for the required values
   * @param unit  the timeout unit, not null
   */
  @Override
  public void init(Set<ValueRequirement> valuesRequired, long timeout, TimeUnit unit) {
    for (UnderlyingSnapshot underlyingSnapshot : _snapshots) {
      Set<ValueRequirement> requirements = Sets.intersection(underlyingSnapshot.getRequirements(), valuesRequired);
      // TODO whole timeout? or divide timeout between all delegate snapshots and keep track of how much is left?
      // the combined snapshot does this but that seems broken to me
      if (!requirements.isEmpty()) {
        underlyingSnapshot.getSnapshot().init(requirements, timeout, unit);
      }
    }
  }

  /**
   * @return The time associated with the first delegate snapshot
   */
  @Override
  public Instant getSnapshotTime() {
    return _snapshots.get(0).getSnapshot().getSnapshotTime();
  }

  /**
   * Returns the value from one of the underlying snapshots or null if it isn't available in any of them
   * @param requirement  the value required, not null
   * @return The value from one of the underlying snapshots or null if it isn't available in any of them
   */
  @Override
  public Object query(ValueRequirement requirement) {
    for (UnderlyingSnapshot snapshot : _snapshots) {
      if (snapshot.getRequirements().contains(requirement)) {
        return snapshot.getSnapshot().query(requirement);
      }
    }
    return null;
  }

  /**
   *
   * @param requirements the values required, not null
   * @return
   */
  @Override
  public Map<ValueRequirement, Object> query(Set<ValueRequirement> requirements) {
    Map<ValueRequirement, Object> results = Maps.newHashMapWithExpectedSize(requirements.size());
    for (UnderlyingSnapshot snapshot : _snapshots) {
      Set<ValueRequirement> snapshotRequirements = requirements;
      //Set<ValueRequirement> snapshotRequirements = Sets.intersection(snapshot.getRequirements(), requirements);
      Map<ValueRequirement, Object> snapshotValues = snapshot.getSnapshot().query(snapshotRequirements);
      results.putAll(snapshotValues);
    }
    return results;
  }
}
