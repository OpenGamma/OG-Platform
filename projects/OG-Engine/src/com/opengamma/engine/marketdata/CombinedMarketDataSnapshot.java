/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.time.Instant;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.UniqueId;

/**
 * 
 */
public class CombinedMarketDataSnapshot implements MarketDataSnapshot {

  private final Map<MarketDataProvider, MarketDataSnapshot> _snapshotByProvider;
  private final MarketDataSnapshot _prefferedSnapshot;
  private final CombinedMarketDataProvider _combinedMarketDataProvider;

  public CombinedMarketDataSnapshot(MarketDataSnapshot prefferedSnapshot, Map<MarketDataProvider, MarketDataSnapshot> snapshotByProvider, CombinedMarketDataProvider combinedMarketDataProvider) {
    _prefferedSnapshot = prefferedSnapshot;
    _snapshotByProvider = snapshotByProvider;
    _combinedMarketDataProvider = combinedMarketDataProvider;
  }

  @Override
  public UniqueId getUniqueId() {
    return _prefferedSnapshot.getUniqueId();
  }

  @Override
  public Instant getSnapshotTimeIndication() {
    return _prefferedSnapshot.getSnapshotTimeIndication();
  }

  @Override
  public void init() {
    for (MarketDataSnapshot entry : _snapshotByProvider.values()) {
      entry.init();
    }
  }

  @Override
  public void init(Set<ValueRequirement> valuesRequired, long timeout, TimeUnit unit) {
    //TODO: timeout should be total
    Map<MarketDataProvider, Set<ValueRequirement>> groupByProvider = _combinedMarketDataProvider.groupByProvider(valuesRequired);
    for (Entry<MarketDataProvider, Set<ValueRequirement>> entry : groupByProvider.entrySet()) {
      MarketDataSnapshot snapshot = _snapshotByProvider.get(entry.getKey());
      snapshot.init(entry.getValue(), timeout, unit);
    }
  }

  @Override
  public Instant getSnapshotTime() {
    return _prefferedSnapshot.getSnapshotTime();
  }

  @Override
  public Object query(ValueRequirement requirement) {
    MarketDataProvider provider = _combinedMarketDataProvider.getProvider(requirement);
    if (provider == null) {
      return null;
    }
    return _snapshotByProvider.get(provider).query(requirement);
  }
}
