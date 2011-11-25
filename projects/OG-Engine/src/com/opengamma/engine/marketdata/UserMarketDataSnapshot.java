/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceKey;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceSnapshot;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

// REVIEW jonathan 2011-06-29 -- The user market data provider classes, including this, no longer need to be in the
// engine and they simply introduce dependencies on the MarketDataSnapshotSource and specific StructuredMarketDataKeys.
// They are a perfect example of adding a custom market data source and should be moved elsewhere.
/**
 * Represents a market data snapshot from a {@link MarketDataSnapshotSource}.
 */
public class UserMarketDataSnapshot implements MarketDataSnapshot {

  private static final Map<String, StructuredMarketDataKeyFactory> s_structuredKeyFactories = new HashMap<String, StructuredMarketDataKeyFactory>();
  
  private final MarketDataSnapshotSource _snapshotSource;
  private final UniqueId _snapshotId;
  private StructuredMarketDataSnapshot _snapshot;
  
  /**
   * Factory for {@link StructuredMarketDataKey} instances.
   */
  private abstract static class StructuredMarketDataKeyFactory {

    
    /**
     * Gets the {@link StructuredMarketDataKey} corresponding to a value requirement.
     * 
     * @param valueRequirement  the value requirement, not null
     * @return the structured market data key, null if the value requirement does not correspond to a key
     */
    public abstract StructuredMarketDataKey fromRequirement(ValueRequirement valueRequirement);
    
    protected Currency getCurrency(ValueRequirement valueRequirement) {
      UniqueId targetId = getTarget(valueRequirement);
      if (targetId == null) {
        return null;
      }
      if (!Currency.OBJECT_SCHEME.equals(targetId.getScheme())) {
        return null;
      }
      Currency currency = Currency.of(targetId.getValue());
      return currency;
    }
    
    protected UniqueId getTarget(ValueRequirement valueRequirement) {
      if (valueRequirement.getTargetSpecification().getType() != ComputationTargetType.PRIMITIVE) {
        return null;
      }
      return valueRequirement.getTargetSpecification().getUniqueId();
    }
    
  }

  static {
    registerStructuredKeyFactory(ValueRequirementNames.YIELD_CURVE_MARKET_DATA, new StructuredMarketDataKeyFactory() {

      @Override
      public StructuredMarketDataKey fromRequirement(ValueRequirement valueRequirement) {
        Currency currency = getCurrency(valueRequirement);
        if (currency == null) {
          return null;
        }
        String curveName = valueRequirement.getConstraint(ValuePropertyNames.CURVE);
        if (curveName == null) {
          return new YieldCurveKey(currency, null);
        }
        return new YieldCurveKey(currency, curveName);
      }
      
    });
    
    registerStructuredKeyFactory(ValueRequirementNames.VOLATILITY_CUBE_MARKET_DATA, new StructuredMarketDataKeyFactory() {

      @Override
      public StructuredMarketDataKey fromRequirement(ValueRequirement valueRequirement) {
        Currency currency = getCurrency(valueRequirement);
        if (currency == null) {
          return null;
        }
        String cubeName = valueRequirement.getConstraint(ValuePropertyNames.CUBE);
        if (cubeName == null) {
          return new VolatilityCubeKey(currency, null);
        }
        return new VolatilityCubeKey(currency, cubeName);
      }
      
    });
    
    registerStructuredKeyFactory(ValueRequirementNames.VOLATILITY_SURFACE_DATA, new StructuredMarketDataKeyFactory() {

      @Override
      public StructuredMarketDataKey fromRequirement(ValueRequirement valueRequirement) {
        UniqueIdentifiable target = getTarget(valueRequirement);
        if (target == null) {
          return null;
        }
        String name = valueRequirement.getConstraint(ValuePropertyNames.SURFACE);
        String instrumentType = valueRequirement.getConstraint("InstrumentType");
        if (valueRequirement.getConstraints().getProperties() != null && valueRequirement.getConstraints().getProperties().size() > 2) {
          //Don't satisfy random constraints, perhaps this is a derived surface
          return null;
        }
        return new VolatilitySurfaceKey(target, name, instrumentType);
      }
      
    });
  }
  
  public UserMarketDataSnapshot(MarketDataSnapshotSource snapshotSource, UniqueId snapshotId) {
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
  public Object query(ValueRequirement requirement) {
    StructuredMarketDataKey structuredKey = getStructuredKey(requirement);
    if (structuredKey != null) {
      return queryStructured(structuredKey);
    } else {
      return queryUnstructured(requirement);
    }
  }

  //-------------------------------------------------------------------------
  private static void registerStructuredKeyFactory(String valueRequirementName, StructuredMarketDataKeyFactory factory) {
    s_structuredKeyFactories.put(valueRequirementName, factory);
  }
  
  public static StructuredMarketDataKey getStructuredKey(ValueRequirement valueRequirement) {
    StructuredMarketDataKeyFactory factory = s_structuredKeyFactories.get(valueRequirement.getValueName());
    if (factory == null) {
      return null;
    }
    return factory.fromRequirement(valueRequirement);
  }

  private Object queryUnstructured(ValueRequirement requirement) {
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

  private Object queryStructured(StructuredMarketDataKey marketDataKey) {
    if (marketDataKey instanceof YieldCurveKey) {
      YieldCurveKey yieldcurveKey = (YieldCurveKey) marketDataKey;
      YieldCurveSnapshot yieldCurveSnapshot = getYieldCurveSnapshot(yieldcurveKey);
      if (yieldCurveSnapshot == null) {
        return null;
      }
      return buildSnapshot(yieldCurveSnapshot);
    } else if (marketDataKey instanceof VolatilityCubeKey) {
      VolatilityCubeKey volCubeKey = (VolatilityCubeKey) marketDataKey;
      VolatilityCubeSnapshot volCubeSnapshot = getVolCubeSnapshot(volCubeKey);
      if (volCubeSnapshot == null) {
        return null;
      }
      return buildVolatilityCubeData(volCubeSnapshot);
    } else if (marketDataKey instanceof VolatilitySurfaceKey) {
      return getVolSurfaceSnapshot((VolatilitySurfaceKey) marketDataKey);
    } else {
      throw new IllegalArgumentException(MessageFormat.format("Don''t know what {0} means.", marketDataKey));
    }
  }

  private YieldCurveSnapshot getYieldCurveSnapshot(YieldCurveKey yieldcurveKey) {
    if (yieldcurveKey.getName() == null) {
      //Any curve will do
      for (Entry<YieldCurveKey, YieldCurveSnapshot> entry : getSnapshot().getYieldCurves().entrySet()) {
        //This could return any old value, but hey, that's what they asked for right?
        if (entry.getKey().getCurrency().equals(yieldcurveKey.getCurrency())) {
          return entry.getValue();
        }
      }
      return null;
    } else {
      YieldCurveSnapshot yieldCurveSnapshot = getSnapshot().getYieldCurves().get(yieldcurveKey);
      return yieldCurveSnapshot;
    }
  }

  private VolatilityCubeSnapshot getVolCubeSnapshot(VolatilityCubeKey volCubeKey) {
    if (volCubeKey.getName() == null) {
      //Any cube will do
      for (Entry<VolatilityCubeKey, VolatilityCubeSnapshot> entry : getSnapshot().getVolatilityCubes().entrySet()) {
        //This could return any old cube, but hey, that's what they asked for right?
        if (entry.getKey().getCurrency().equals(volCubeKey.getCurrency())) {
          return entry.getValue();
        }
      }
      return null;
    } else {
      VolatilityCubeSnapshot volCubeSnapshot = getSnapshot().getVolatilityCubes().get(volCubeKey);
      return volCubeSnapshot;
    }
  }
  
  private VolatilitySurfaceData<Object, Object> getVolSurfaceSnapshot(VolatilitySurfaceKey volSurfaceKey) {
    if (volSurfaceKey.getName() != null && volSurfaceKey.getInstrumentType() != null)
    {
      VolatilitySurfaceSnapshot volatilitySurfaceSnapshot = getSnapshot().getVolatilitySurfaces().get(volSurfaceKey);
      if (volatilitySurfaceSnapshot == null) {
        return null;
      }
      return buildVolatilitySurfaceData(volatilitySurfaceSnapshot, (VolatilitySurfaceKey) volSurfaceKey);
    }
    
    //Match with wildcards
    for (Entry<VolatilitySurfaceKey, VolatilitySurfaceSnapshot> entry : getSnapshot().getVolatilitySurfaces().entrySet()) {
      //This could return any old surface, but hey, that's what they asked for right?
      VolatilitySurfaceKey key = entry.getKey();
      if (key.getTarget().equals(volSurfaceKey.getTarget())
          && (volSurfaceKey.getInstrumentType() == null || key.getInstrumentType() == volSurfaceKey.getInstrumentType())
          && (volSurfaceKey.getName() == null || key.getName() == volSurfaceKey.getName())) {
        return buildVolatilitySurfaceData(entry.getValue(), entry.getKey());
      }
    }
    return null;

  }
  
  private StructuredMarketDataSnapshot getSnapshot() {
    if (_snapshot == null) {
      throw new IllegalStateException("Snapshot has not been initialised");
    }
    return _snapshot;
  }
  
  private MarketDataSnapshotSource getSnapshotSource() {
    return _snapshotSource;
  }
  
  private UniqueId getSnapshotId() {
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
    HashMap<UniqueId, Double> points = new HashMap<UniqueId, Double>();
    for (Entry<MarketDataValueSpecification, Map<String, ValueSnapshot>> entry : values.getValues().entrySet()) {
      Double value = query(entry.getValue().get(MarketDataRequirementNames.MARKET_VALUE));
      points.put(entry.getKey().getUniqueId(), value);
    }
    ret.setDataPoints(points);
    return ret;
  }
  
  private VolatilityCubeData buildVolatilityCubeData(VolatilityCubeSnapshot volCubeSnapshot) {
    Map<VolatilityPoint, ValueSnapshot> values = volCubeSnapshot.getValues();
    HashMap<VolatilityPoint, Double> dataPoints = buildVolValues(values);
    HashMap<Pair<Tenor, Tenor>, Double> strikes = buildVolStrikes(volCubeSnapshot.getStrikes());
    SnapshotDataBundle otherData = buildBundle(volCubeSnapshot.getOtherValues());
    
    VolatilityCubeData ret = new VolatilityCubeData();
    ret.setDataPoints(dataPoints);
    ret.setOtherData(otherData);
    ret.setATMStrikes(strikes);
    
    return ret;
  }

  private HashMap<Pair<Tenor, Tenor>, Double> buildVolStrikes(Map<Pair<Tenor, Tenor>, ValueSnapshot> strikes) {
    HashMap<Pair<Tenor, Tenor>, Double> dataPoints = new HashMap<Pair<Tenor, Tenor>, Double>();
    for (Entry<Pair<Tenor, Tenor>, ValueSnapshot> entry : strikes.entrySet()) {
      ValueSnapshot value = entry.getValue();
      Double query = query(value);
      if (query != null) {
        dataPoints.put(entry.getKey(), query);
      }
    }
    return dataPoints;
  }

  private HashMap<VolatilityPoint, Double> buildVolValues(Map<VolatilityPoint, ValueSnapshot> values) {
    HashMap<VolatilityPoint, Double> dataPoints = new HashMap<VolatilityPoint, Double>();
    for (Entry<VolatilityPoint, ValueSnapshot> entry : values.entrySet()) {
      ValueSnapshot value = entry.getValue();
      Double query = query(value);
      if (query != null) {
        dataPoints.put(entry.getKey(), query);
      }
    }
    return dataPoints;
  }

  private VolatilitySurfaceData<Object, Object> buildVolatilitySurfaceData(VolatilitySurfaceSnapshot volCubeSnapshot,
      VolatilitySurfaceKey marketDataKey) {

    Set<Object> xs = new HashSet<Object>();
    Set<Object> ys = new HashSet<Object>();
    Map<Pair<Object, Object>, Double> values = new HashMap<Pair<Object, Object>, Double>();
    Map<Pair<Object, Object>, ValueSnapshot> snapValues = volCubeSnapshot.getValues();
    for (Entry<Pair<Object, Object>, ValueSnapshot> entry : snapValues.entrySet()) {
      values.put(entry.getKey(), query(entry.getValue()));
      xs.add(entry.getKey().getFirst());
      ys.add(entry.getKey().getSecond());
    }

    return new VolatilitySurfaceData<Object, Object>(marketDataKey.getName(), "UNKNOWN", marketDataKey.getTarget(),
        xs.toArray(), ys.toArray(), values);
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
