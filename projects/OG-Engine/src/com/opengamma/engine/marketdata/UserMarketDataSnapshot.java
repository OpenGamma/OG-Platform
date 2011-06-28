/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.time.Instant;

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
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;

/**
 * Represents a market data snapshot from a {@link MarketDataSnapshotSource}.
 */
public class UserMarketDataSnapshot implements MarketDataSnapshot {

  private final MarketDataSnapshotSource _snapshotSource;
  private final UniqueIdentifier _snapshotId;
  private StructuredMarketDataSnapshot _snapshot;
  
  public UserMarketDataSnapshot(MarketDataSnapshotSource snapshotSource, UniqueIdentifier snapshotId) {
    _snapshotSource = snapshotSource;
    _snapshotId = snapshotId;
  }
  
  //-------------------------------------------------------------------------
  @Override
  public Instant getSnapshotTimeIndication() {
    init();
    return getSnapshotTime();
  }

  @Override
  public void init() {
    _snapshot = getSnapshotSource().getSnapshot(getSnapshotId());
  }
  
  @Override
  public void init(Set<ValueRequirement> valuesRequired, long timeout, TimeUnit unit) {
    init();
  }

  @Override
  public Instant getSnapshotTime() {
    // TODO [PLAT-1393] should explicitly store a snapshot time, which the user might choose to customise
    Instant latestTimestamp = null;
    Map<YieldCurveKey, YieldCurveSnapshot> yieldCurves = getSnapshot().getYieldCurves();
    if (yieldCurves != null) {
      for (YieldCurveSnapshot yieldCurveSnapshot : yieldCurves.values()) {
        if (latestTimestamp == null || latestTimestamp.isBefore(yieldCurveSnapshot.getValuationTime())) {
          latestTimestamp = yieldCurveSnapshot.getValuationTime();
        }
      }
    }
    if (latestTimestamp == null) {
      // What else can we do until one is guaranteed to be stored with the snapshot?
      latestTimestamp = Instant.now();
    }
    return latestTimestamp;
  }

  @Override
  public boolean hasStructuredData() {
    return true;
  }

  @Override
  public Object query(ValueRequirement requirement) {
    UnstructuredMarketDataSnapshot globalValues = getSnapshot().getGlobalValues();
    MarketDataValueSpecification marketDataValueSpecification = new MarketDataValueSpecification(
        getTargetType(requirement), requirement.getTargetSpecification().getUniqueId());
    
    Map<String, ValueSnapshot> map = globalValues.getValues().get(marketDataValueSpecification);
    if (map == null) {
      return null;
    }
    ValueSnapshot valueSnapshot = map.get(requirement.getValueName());
    return query(valueSnapshot);
  }

  @Override
  public Object query(StructuredMarketDataKey marketDataKey) {
    if (marketDataKey instanceof YieldCurveKey) {
      YieldCurveSnapshot yieldCurveSnapshot = getSnapshot().getYieldCurves().get(marketDataKey);
      if (yieldCurveSnapshot == null) {
        return new SnapshotDataBundle(); //NOTE: this is not the same as return null;
      }
      return buildSnapshot(yieldCurveSnapshot);
    } else if (marketDataKey instanceof VolatilityCubeKey) {
      VolatilityCubeSnapshot volCubeSnapshot = getSnapshot().getVolatilityCubes().get(marketDataKey);
      if (volCubeSnapshot == null) {
        return new VolatilityCubeData(); //NOTE: this is not the same as return null;
      }
      return buildVolatilityCubeData(volCubeSnapshot);
    } else {
      throw new IllegalArgumentException(MessageFormat.format("Don''t know what {0} means.", marketDataKey));
    }
  }
  
  //------------------------------------------------------------------------- 
  private StructuredMarketDataSnapshot getSnapshot() {
    if (_snapshot == null) {
      throw new IllegalStateException("Snapshot has not been initialised");
    }
    return _snapshot;
  }
  
  private MarketDataSnapshotSource getSnapshotSource() {
    return _snapshotSource;
  }
  
  private UniqueIdentifier getSnapshotId() {
    return _snapshotId;
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
    UnstructuredMarketDataSnapshot values = yieldCurveSnapshot.getValues();
    return buildBundle(values);
  }

  private SnapshotDataBundle buildBundle(UnstructuredMarketDataSnapshot values) {
    SnapshotDataBundle ret = new SnapshotDataBundle();
    HashMap<UniqueIdentifier, Double> points = new HashMap<UniqueIdentifier, Double>();
    for (Entry<MarketDataValueSpecification, Map<String, ValueSnapshot>> entry : values.getValues().entrySet()) {
      Double value = query(entry.getValue().get(MarketDataRequirementNames.MARKET_VALUE));
      points.put(entry.getKey().getUniqueId(), value);
    }
    ret.setDataPoints(points);
    return ret;
  }
  
  private VolatilityCubeData buildVolatilityCubeData(VolatilityCubeSnapshot volCubeSnapshot) {
    HashMap<VolatilityPoint, Double> dataPoints = new HashMap<VolatilityPoint, Double>();
    for (Entry<VolatilityPoint, ValueSnapshot> entry : volCubeSnapshot.getValues().entrySet()) {
      ValueSnapshot value = entry.getValue();
      Double query = query(value);
      if (query != null) {
        dataPoints.put(entry.getKey(), query);
      }
    }
    SnapshotDataBundle otherData = buildBundle(volCubeSnapshot.getOtherValues());
    VolatilityCubeData ret = new VolatilityCubeData();
    ret.setDataPoints(dataPoints);
    
    ret.setOtherData(otherData);
    return ret;
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
