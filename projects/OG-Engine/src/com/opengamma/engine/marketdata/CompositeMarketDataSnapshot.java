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
 * TODO should this implement MarketDataSnapshot? the snapshot time methods don't make much sense. also init timeout
 * for historical snapshots the snapshot times are historical, for live they are the time that init() is called
 * what do the existing combined snapshots / providers do? they return the info for the first snapshot delegate
 */
/* package */ class CompositeMarketDataSnapshot implements MarketDataSnapshot {

  private final List<MarketDataSnapshot> _snapshots;
  private final List<Set<ValueRequirement>> _requirementsBySnapshot;

  /* package */ CompositeMarketDataSnapshot(List<MarketDataSnapshot> snapshots,
                                            List<Set<ValueRequirement>> requirementsBySnapshot) {
    ArgumentChecker.notEmpty(snapshots, "snapshots");
    ArgumentChecker.notNull(requirementsBySnapshot, "requirementsBySnapshot");
    if (snapshots.size() != requirementsBySnapshot.size()) {
      throw new IllegalArgumentException("snapshots and requirementsBySnapshot must be the same size, " +
                                             "snapshots.size() = " + snapshots.size() + ", " +
                                             "requirementsBySnapshot.size = " + requirementsBySnapshot.size());
    }
    _snapshots = snapshots;
    _requirementsBySnapshot = requirementsBySnapshot;
  }

  @Override
  public UniqueId getUniqueId() {
    // TODO is this unique enough? same as in the existing impls
    return UniqueId.of(MARKET_DATA_SNAPSHOT_ID_SCHEME, "CompositeMarketDataSnapshot:" + getSnapshotTime());
  }

  @Override
  public Instant getSnapshotTimeIndication() {
    return getSnapshotTime();
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
   * Initializes the underlying snapshots
   * @param valuesRequired  the values required in the snapshot, not null
   * @param timeout  the maximum time to wait for the required values
   * @param unit  the timeout unit, not null
   */
  @Override
  public void init(Set<ValueRequirement> valuesRequired, long timeout, TimeUnit unit) {
    for (int i = 0; i < _snapshots.size(); i++) {
      MarketDataSnapshot snapshot = _snapshots.get(i);
      Set<ValueRequirement> requirements = Sets.intersection(_requirementsBySnapshot.get(i), valuesRequired);
      // TODO whole timeout? or divide timeout between all delegate snapshots and keep track of how much is left?
      // the combined snapshot does this but that seems broken to me
      if (!requirements.isEmpty()) {
        snapshot.init(requirements, timeout, unit);
      }
    }
  }

  /**
   * @return The time associated with the first delegate snapshot
   */
  @Override
  public Instant getSnapshotTime() {
    return _snapshots.get(0).getSnapshotTime();
  }

  /**
   * Returns the value from one of the underlying snapshots or null if it isn't available in any of them
   * @param requirement  the value required, not null
   * @return The value from one of the underlying snapshots or null if it isn't available in any of them
   */
  @Override
  public Object query(ValueRequirement requirement) {
    for (int i = 0; i < _snapshots.size(); i++) {
      if (_requirementsBySnapshot.get(i).contains(requirement)) {
        return _snapshots.get(i).query(requirement);
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
    for (int i = 0; i < _snapshots.size(); i++) {
      Set<ValueRequirement> snapshotRequirements = Sets.intersection(_requirementsBySnapshot.get(i), requirements);
      Map<ValueRequirement, Object> snapshotValues = _snapshots.get(i).query(snapshotRequirements);
      results.putAll(snapshotValues);
    }
    return results;
  }
}
