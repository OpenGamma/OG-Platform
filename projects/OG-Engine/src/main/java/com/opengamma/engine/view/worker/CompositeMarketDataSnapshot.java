/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.threeten.bp.Instant;

import com.google.common.collect.Maps;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Snapshot of market data which aggregates data from multiple underlying snapshots.
 */
/* package */class CompositeMarketDataSnapshot implements MarketDataSnapshot {

  /** The underlying snapshots. */
  private final List<MarketDataSnapshot> _snapshots;
  /** The object that can map the value specifications to/from an underlying provider. */
  private final SnapshottingViewExecutionDataProvider.ValueSpecificationProvider _valueMap;

  /**
   * @param snapshots The underlying snapshots
   */
  /* package */CompositeMarketDataSnapshot(final List<MarketDataSnapshot> snapshots, final SnapshottingViewExecutionDataProvider.ValueSpecificationProvider valueMap) {
    ArgumentChecker.notEmpty(snapshots, "snapshots");
    ArgumentChecker.notNull(valueMap, "valueMap");
    _snapshots = snapshots;
    _valueMap = valueMap;
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
    for (final MarketDataSnapshot snapshot : _snapshots) {
      final Instant snapshotTimeIndication = snapshot.getSnapshotTimeIndication();
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
    for (final MarketDataSnapshot snapshot : _snapshots) {
      snapshot.init();
    }
  }

  /**
   * Initializes the underlying snapshots.
   * 
   * @param values the values required in the snapshot, not null
   * @param timeout the maximum time to wait for the required values
   * @param unit the timeout unit, not null
   */
  @Override
  public void init(final Set<ValueSpecification> values, final long timeout, final TimeUnit unit) {
    ArgumentChecker.notNull(values, "values");
    ArgumentChecker.notNull(unit, "unit");
    final List<Set<ValueSpecification>> valuesBySnapshot = _valueMap.getUnderlyingSpecifications(values);
    for (int i = 0; i < _snapshots.size(); i++) {
      final MarketDataSnapshot snapshot = _snapshots.get(i);
      final Set<ValueSpecification> snapshotSubscriptions = valuesBySnapshot.get(i);
      if (snapshotSubscriptions.isEmpty()) {
        snapshot.init();
      } else {
        // TODO whole timeout? or divide timeout between all delegate snapshots and keep track of how much is left?
        // the combined snapshot does this but that seems broken to me
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
    for (final MarketDataSnapshot snapshot : _snapshots) {
      final Instant snapshotTime = snapshot.getSnapshotTime();
      if (snapshotTime != null) {
        return snapshotTime;
      }
    }
    throw new IllegalStateException("None of the underlying snapshots returned a snapshot time");
  }

  /**
   * Returns the value from one of the underlying snapshots or null if it isn't available in any of them
   * 
   * @param value the value required, not null
   * @return The value from one of the underlying snapshots or null if it isn't available in any of them
   */
  @Override
  public Object query(final ValueSpecification value) {
    ArgumentChecker.notNull(value, "value");
    final Pair<Integer, ValueSpecification> lookup = _valueMap.getUnderlyingAndSpecification(value);
    if (lookup == null) {
      return null;
    }
    return _snapshots.get(lookup.getFirst()).query(lookup.getSecond());
  }

  /**
   * Returns the values from the underlying snapshots if they are available
   * 
   * @param values the values required, not null
   * @return The values from the underlying snapshots if they are available, values that aren't available will be missing from the results map
   */
  @Override
  public Map<ValueSpecification, Object> query(final Set<ValueSpecification> values) {
    ArgumentChecker.notNull(values, "values");
    final Map<ValueSpecification, Object> results = Maps.newHashMapWithExpectedSize(values.size());
    final List<Set<ValueSpecification>> valuesBySnapshot = _valueMap.getUnderlyingSpecifications(values);
    for (int i = 0; i < _snapshots.size(); i++) {
      final MarketDataSnapshot snapshot = _snapshots.get(i);
      final Set<ValueSpecification> snapshotSubscriptions = valuesBySnapshot.get(i);
      if (!snapshotSubscriptions.isEmpty()) {
        final Map<ValueSpecification, Object> snapshotResults = snapshot.query(snapshotSubscriptions);
        for (final Map.Entry<ValueSpecification, Object> snapshotResult : snapshotResults.entrySet()) {
          results.put(_valueMap.convertUnderlyingSpecification(i, snapshotResult.getKey()), snapshotResult.getValue());
        }
      }
    }
    return results;
  }

}
