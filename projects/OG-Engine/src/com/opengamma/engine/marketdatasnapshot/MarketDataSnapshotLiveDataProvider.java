/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdatasnapshot;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotChangeListener;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.MarketDataValueSpecification;
import com.opengamma.core.marketdatasnapshot.MarketDataValueType;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataKey;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeData;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeKey;
import com.opengamma.core.marketdatasnapshot.VolatilityCubeSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilityPoint;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.livedata.AbstractLiveDataSnapshotProvider;
import com.opengamma.engine.livedata.LiveDataSnapshotListener;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;

/**
 * Snapshot provider will always return the data from the curated snapshot
 */
public class MarketDataSnapshotLiveDataProvider extends AbstractLiveDataSnapshotProvider {

  private final ConcurrentMap<Long, StructuredMarketDataSnapshot> _snapshots = 
    new ConcurrentHashMap<Long, StructuredMarketDataSnapshot>();
  private final CopyOnWriteArraySet<ValueRequirement> _listeningValueRequirements = new CopyOnWriteArraySet<ValueRequirement>();
  
  
  private final MarketDataSnapshotSource _marketDataSnapshotSource;
  private final UniqueIdentifier _marketDataSnapshotIdentifier;
  private final Object _listenerLock = new Object();
  private MarketDataSnapshotChangeListener _listener;

  
  
  public MarketDataSnapshotLiveDataProvider(MarketDataSnapshotSource marketDataSnapshotSource,
      UniqueIdentifier marketDataSnapshotIdentifier) {
    _marketDataSnapshotSource = marketDataSnapshotSource;
    _marketDataSnapshotIdentifier = marketDataSnapshotIdentifier;
    
    _listener = new MarketDataSnapshotChangeListener() {
      @Override
      public void snapshotChanged(UniqueIdentifier uid) {
        valueChanged(_listeningValueRequirements); // TODO: this is over kill, but since we're going to trigger a calculation anyway...
      }
    };
  }

  @Override
  public void addSubscription(UserPrincipal user, ValueRequirement valueRequirement) {
    addSubscription(user, Collections.singleton(valueRequirement));
  }

  @Override
  public void addSubscription(UserPrincipal user, Set<ValueRequirement> valueRequirements) {
    _listeningValueRequirements.addAll(valueRequirements); // TODO PLAT-485 this is a leak, but we get thrown away eventually
    subscriptionSucceeded(valueRequirements);
  }
  
  @Override
  public void addListener(LiveDataSnapshotListener listener) {
    synchronized (_listenerLock) {
      if (getListeners().size() == 0) {
        _marketDataSnapshotSource.addChangeListener(_marketDataSnapshotIdentifier, _listener);
      }
      super.addListener(listener);
    }
  }

  @Override
  public void removeListener(LiveDataSnapshotListener listener) {
    synchronized (_listenerLock) {
      super.removeListener(listener);
      if (getListeners().size() == 0) {
        _marketDataSnapshotSource.removeChangeListener(_marketDataSnapshotIdentifier, _listener);
      }
    }
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
  public Object querySnapshot(long snapshot, StructuredMarketDataKey marketDataKey) {
    if (marketDataKey instanceof YieldCurveKey) {
      YieldCurveSnapshot yieldCurveSnapshot = getSnapshot(snapshot).getYieldCurves().get(marketDataKey);
      if (yieldCurveSnapshot == null) {
        return new SnapshotDataBundle(); //NOTE: this is not the same as return null;
      }
      return buildSnapshot(yieldCurveSnapshot);
    } else if (marketDataKey instanceof VolatilityCubeKey) {
      VolatilityCubeSnapshot volCubeSnapshot = getSnapshot(snapshot).getVolatilityCubes().get(marketDataKey);
      if (volCubeSnapshot == null) {
        return new VolatilityCubeData(); //NOTE: this is not the same as return null;
      }
      return buildVolatilityCubeData(volCubeSnapshot);
    } else {
      throw new IllegalArgumentException(MessageFormat.format("Don''t know what {0} means.", marketDataKey));
    }
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
    HashMap<UniqueIdentifier, Double> points = new HashMap<UniqueIdentifier, Double>();
    UnstructuredMarketDataSnapshot values = yieldCurveSnapshot.getValues();
    for (Entry<MarketDataValueSpecification, Map<String, ValueSnapshot>> entry : values.getValues().entrySet()) {
      Double value = query(entry.getValue().get(MarketDataRequirementNames.MARKET_VALUE));
      points.put(entry.getKey().getUniqueId(), value);
    }
    ret.setDataPoints(points);
    return ret;
  }
  
  
  private VolatilityCubeData buildVolatilityCubeData(VolatilityCubeSnapshot volCubeSnapshot) {
    VolatilityCubeData ret = new VolatilityCubeData();
    HashMap<VolatilityPoint, Double> dataPoints = new HashMap<VolatilityPoint, Double>();
    for (Entry<VolatilityPoint, ValueSnapshot> entry : volCubeSnapshot.getValues().entrySet()) {
      ValueSnapshot value = entry.getValue();
      Double query = query(value);
      if (query != null) {
        dataPoints.put(entry.getKey(), query);
      }
    }
    ret.setDataPoints(dataPoints);
    return ret;
  }

  @Override
  public void releaseSnapshot(long snapshot) {
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
