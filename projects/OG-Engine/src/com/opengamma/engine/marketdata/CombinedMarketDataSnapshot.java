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

import com.google.common.collect.Maps;
import com.opengamma.engine.value.ValueRequirement;

/**
 * 
 */
public class CombinedMarketDataSnapshot extends AbstractMarketDataSnapshot {
  
  private final Map<MarketDataProvider, MarketDataSnapshot> _snapshotByProvider;
  private final MarketDataSnapshot _prefferedSnapshot;
  private final CombinedMarketDataProvider _combinedMarketDataProvider;

  public CombinedMarketDataSnapshot(MarketDataSnapshot prefferedSnapshot, Map<MarketDataProvider, MarketDataSnapshot> snapshotByProvider, CombinedMarketDataProvider combinedMarketDataProvider) {
    _prefferedSnapshot = prefferedSnapshot;
    _snapshotByProvider = snapshotByProvider;
    _combinedMarketDataProvider = combinedMarketDataProvider;
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

  @Override
  public Map<ValueRequirement, Object> query(final Set<ValueRequirement> requirements) {
    final Map<ValueRequirement, Object> result = Maps.newHashMapWithExpectedSize(requirements.size());
    final Map<MarketDataProvider, Set<ValueRequirement>> groupByProvider = _combinedMarketDataProvider.groupByProvider(requirements);
    for (Entry<MarketDataProvider, Set<ValueRequirement>> entry : groupByProvider.entrySet()) {
      final Map<ValueRequirement, Object> values = _snapshotByProvider.get(entry.getKey()).query(entry.getValue());
      if (values != null) {
        result.putAll(values);
      }
    }
    return result;
  }

}
