/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdatasnapshot;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.MarketDataValueSpecification;
import com.opengamma.core.marketdatasnapshot.MarketDataValueType;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.livedata.AbstractLiveDataSnapshotProvider;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;

/**
 * Snapshot provider will always return the data from the curated snapshot
 */
public class MarketDataSnapshotLiveDataProvider extends AbstractLiveDataSnapshotProvider {

  private final ConcurrentHashMap<Long, StructuredMarketDataSnapshot> _snapshots = new ConcurrentHashMap<Long, StructuredMarketDataSnapshot>();
  
  private final MarketDataSnapshotSource _marketDataSnapshotSource;
  private final UniqueIdentifier _marketDataSnapshotIdentifier;

  public MarketDataSnapshotLiveDataProvider(MarketDataSnapshotSource marketDataSnapshotSource,
      UniqueIdentifier marketDataSnapshotIdentifier) {
    _marketDataSnapshotSource = marketDataSnapshotSource;
    _marketDataSnapshotIdentifier = marketDataSnapshotIdentifier;
  }

  @Override
  public void addSubscription(UserPrincipal user, ValueRequirement valueRequirement) {
    addSubscription(user, Collections.singleton(valueRequirement));
  }

  @Override
  public void addSubscription(UserPrincipal user, Set<ValueRequirement> valueRequirements) {
    //No actual subscription to make, but we still need to acknowledge it.
    subscriptionSucceeded(valueRequirements);
  }

  @Override
  public long snapshot() {
    long snapshotTime = System.currentTimeMillis();
    snapshot(snapshotTime);
    return snapshotTime;
  }

  @Override
  public long snapshot(long snapshot) {
    StructuredMarketDataSnapshot snap = _marketDataSnapshotSource.getSnapshot(_marketDataSnapshotIdentifier);
    _snapshots.putIfAbsent(snapshot, snap);
    return snapshot;
  }

  private StructuredMarketDataSnapshot getSnapshot(long snapshot) {
    return _snapshots.get(snapshot);
  }
  
  @Override
  public Object querySnapshot(long snapshot, ValueRequirement requirement) {
    UnstructuredMarketDataSnapshot globalValues = getSnapshot(snapshot).getGlobalValues();
    MarketDataValueSpecification marketDataValueSpecification = new MarketDataValueSpecification(
        getTargetType(requirement), requirement.getTargetSpecification().getUniqueId());
    
    Map<String, ValueSnapshot> map = globalValues.getValues().get(marketDataValueSpecification);
    //TODO: allow falling through to real live data
    if (map == null) {
      return null;
    }
    ValueSnapshot valueSnapshot = map.get(requirement.getValueName());
    return query(valueSnapshot);
  }

  
  

  @Override
  public boolean hasStructuredData() {
    return true;
  }
  
  @Override
  public SnapshotDataBundle querySnapshot(long snapshot, YieldCurveKey yieldCurveKey) {
    YieldCurveSnapshot yieldCurveSnapshot = getSnapshot(snapshot).getYieldCurves().get(yieldCurveKey);
    if (yieldCurveSnapshot == null) {
      return new SnapshotDataBundle(); //NOTE: this is not the same as return null;
    }
    return buildSnapshot(yieldCurveSnapshot);
  }
  
  private Double query(ValueSnapshot valueSnapshot) {
    if (valueSnapshot == null) {
      return null;
    }
    //TODO configure which value to use
    if (valueSnapshot.getOverrideValue() != null) {
      return valueSnapshot.getOverrideValue();
    }
    return valueSnapshot.getMarketValue();
  }
  
  
  private SnapshotDataBundle buildSnapshot(YieldCurveSnapshot yieldCurveSnapshot) {
    SnapshotDataBundle ret = new SnapshotDataBundle();
    HashMap<Identifier, Double> points = new HashMap<Identifier, Double>();
    UnstructuredMarketDataSnapshot values = yieldCurveSnapshot.getValues();
    for (Entry<MarketDataValueSpecification, Map<String, ValueSnapshot>> entry : values.getValues().entrySet()) {
      Double value = query(entry.getValue().get(MarketDataRequirementNames.MARKET_VALUE));
      points.put(entry.getKey().getUniqueId().toIdentifier(), value);
    }
    ret.setDataPoints(points);
    return ret;
  }

  @Override
  public void releaseSnapshot(long snapshot) {
    _snapshots.remove(snapshot);
  }

  private MarketDataValueType getTargetType(ValueRequirement liveDataRequirement) {
    ComputationTargetType type = liveDataRequirement.getTargetSpecification().getType();
    switch (type) {
      case PORTFOLIO_NODE:
        throw new IllegalArgumentException();
      case POSITION:
        throw new IllegalArgumentException();
      case PRIMITIVE:
        return MarketDataValueType.PRIMITIVE;
      case SECURITY:
        return MarketDataValueType.SECURITY;
      case TRADE:
        throw new IllegalArgumentException();
      default:
        throw new IllegalArgumentException();
    }
  }
}
