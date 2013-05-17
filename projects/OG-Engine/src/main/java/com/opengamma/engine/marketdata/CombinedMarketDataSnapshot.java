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

import org.threeten.bp.Instant;

import com.google.common.collect.Maps;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class CombinedMarketDataSnapshot extends AbstractMarketDataSnapshot {

  private final Map<MarketDataProvider, MarketDataSnapshot> _snapshotByProvider;
  private final MarketDataSnapshot _preferredSnapshot;
  private final CombinedMarketDataProvider _combinedMarketDataProvider;

  public CombinedMarketDataSnapshot(final MarketDataSnapshot preferredSnapshot, final Map<MarketDataProvider, MarketDataSnapshot> snapshotByProvider,
      final CombinedMarketDataProvider combinedMarketDataProvider) {
    _preferredSnapshot = preferredSnapshot;
    _snapshotByProvider = snapshotByProvider;
    _combinedMarketDataProvider = combinedMarketDataProvider;
  }

  @Override
  public UniqueId getUniqueId() {
    // REVIEW 2013-02-06 Andrew -- This isn't really a useful unique id; it should be allocated by whatever persists this object or is capable of recreating it.
    // Two snapshots containing different data but from the same time will also have the same unique identifier
    return UniqueId.of(MARKET_DATA_SNAPSHOT_ID_SCHEME, "CombinedMarketDataSnapshot:" + getSnapshotTime());
  }

  @Override
  public Instant getSnapshotTimeIndication() {
    return _preferredSnapshot.getSnapshotTimeIndication();
  }

  @Override
  public void init() {
    for (final MarketDataSnapshot entry : _snapshotByProvider.values()) {
      entry.init();
    }
  }

  @Override
  public void init(final Set<ValueSpecification> values, final long timeout, final TimeUnit unit) {
    //TODO: timeout should be total
    final Map<MarketDataProvider, Set<ValueSpecification>> groupByProvider = _combinedMarketDataProvider.getProviders(values);
    for (final Entry<MarketDataProvider, Set<ValueSpecification>> entry : groupByProvider.entrySet()) {
      final MarketDataSnapshot snapshot = _snapshotByProvider.get(entry.getKey());
      snapshot.init(entry.getValue(), timeout, unit);
    }
  }
  
  @Override
  public boolean isInitialized() {
    return _preferredSnapshot.isInitialized();
  }
  
  @Override
  public boolean isEmpty() {
    assertInitialized();
    for (MarketDataSnapshot snapshot : _snapshotByProvider.values()) {
      if (!snapshot.isEmpty()) {
        return false;
      }
    }
    return true;
  }


  @Override
  public Instant getSnapshotTime() {
    return _preferredSnapshot.getSnapshotTime();
  }

  @Override
  public Object query(final ValueSpecification value) {
    final Pair<MarketDataProvider, ValueSpecification> providerAndSpec = _combinedMarketDataProvider.getProvider(value);
    return _snapshotByProvider.get(providerAndSpec.getFirst()).query(providerAndSpec.getSecond());
  }

  @Override
  public Map<ValueSpecification, Object> query(final Set<ValueSpecification> values) {
    final Map<ValueSpecification, Object> result = Maps.newHashMapWithExpectedSize(values.size());
    final Map<MarketDataProvider, Map<ValueSpecification, ValueSpecification>> groupByProvider = _combinedMarketDataProvider.getProvidersAsMap(values);
    for (final Entry<MarketDataProvider, Map<ValueSpecification, ValueSpecification>> entry : groupByProvider.entrySet()) {
      for (Map.Entry<ValueSpecification, Object> innerResult : _snapshotByProvider.get(entry.getKey()).query(entry.getValue().keySet()).entrySet()) {
        result.put(entry.getValue().get(innerResult.getKey()), innerResult.getValue());
      }
    }
    return result;
  }

}
