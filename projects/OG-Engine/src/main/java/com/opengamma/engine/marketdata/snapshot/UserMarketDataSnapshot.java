/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.snapshot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.time.Instant;

import com.opengamma.DataNotFoundException;
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
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.marketdata.AbstractMarketDataSnapshot;
import com.opengamma.engine.marketdata.MarketDataUtils;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

// REVIEW jonathan 2011-06-29 -- The user market data provider classes, including this, no longer need to be in the
// engine and they simply introduce dependencies on the MarketDataSnapshotSource and specific StructuredMarketDataKeys.
// They are a perfect example of adding a custom market data source and should be moved elsewhere.
/**
 * Represents a market data snapshot from a {@link MarketDataSnapshotSource}.
 */
public class UserMarketDataSnapshot extends AbstractMarketDataSnapshot implements StructuredMarketDataKey.Visitor<Object> {

  private static final String INSTRUMENT_TYPE_PROPERTY = "InstrumentType";
  private static final String SURFACE_QUOTE_TYPE_PROPERTY = "SurfaceQuoteType";
  private static final String SURFACE_QUOTE_UNITS_PROPERTY = "SurfaceUnits";
  private static final Map<String, StructuredMarketDataKeyFactory> s_structuredKeyFactories = new HashMap<String, StructuredMarketDataKeyFactory>();

  private final MarketDataSnapshotSource _snapshotSource;
  private final UniqueId _snapshotId;
  private StructuredMarketDataSnapshot _snapshot;

  /**
   * Factory for {@link StructuredMarketDataKey} instances.
   */
  private abstract static class StructuredMarketDataKeyFactory {

    /**
     * Gets the {@link StructuredMarketDataKey} and {@link ValueSpecification} corresponding to a value requirement.
     * 
     * @param valueRequirement the value requirement, not null
     * @param snapshot the market data snapshot object, not null
     * @return the structured market data key, null if the value requirement does not correspond to a key
     */
    public abstract Pair<? extends StructuredMarketDataKey, ValueSpecification> fromRequirement(ValueRequirement valueRequirement, UserMarketDataSnapshot snapshot);

    protected Currency getCurrencyTarget(ValueRequirement valueRequirement) {
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
      public Pair<? extends StructuredMarketDataKey, ValueSpecification> fromRequirement(ValueRequirement valueRequirement, UserMarketDataSnapshot snapshot) {
        final Currency target = getCurrencyTarget(valueRequirement);
        if (target == null) {
          return null;
        }
        final ValueProperties constraints = valueRequirement.getConstraints();
        final Set<String> names = constraints.getValues(ValuePropertyNames.CURVE);
        YieldCurveKey key = null;
        if ((names != null) && (names.size() == 1)) {
          key = new YieldCurveKey(target, names.iterator().next());
        } else {
          if (snapshot.getSnapshot().getYieldCurves() != null) {
            for (YieldCurveKey curve : snapshot.getSnapshot().getYieldCurves().keySet()) {
              if (!target.equals(curve.getCurrency())) {
                continue;
              }
              if ((names != null) && !names.isEmpty() && !names.contains(curve.getName())) {
                continue;
              }
              key = curve;
              break;
            }
          }
          if (key == null) {
            return null;
          }
        }
        final ValueProperties properties = ValueProperties.with(ValuePropertyNames.CURVE, key.getName()).get();
        if (!constraints.isSatisfiedBy(properties)) {
          return null;
        }
        return Pair.of(key, MarketDataUtils.createMarketDataValue(ValueRequirementNames.YIELD_CURVE_MARKET_DATA, valueRequirement.getTargetSpecification(), properties));
      }

    });
    registerStructuredKeyFactory(ValueRequirementNames.VOLATILITY_SURFACE_DATA, new StructuredMarketDataKeyFactory() {

      @Override
      public Pair<? extends StructuredMarketDataKey, ValueSpecification> fromRequirement(ValueRequirement valueRequirement, UserMarketDataSnapshot snapshot) {
        final UniqueIdentifiable target = getTarget(valueRequirement);
        if (target == null) {
          return null;
        }
        final ValueProperties constraints = valueRequirement.getConstraints();
        final Set<String> names = constraints.getValues(ValuePropertyNames.SURFACE);
        final Set<String> instrumentTypes = constraints.getValues(INSTRUMENT_TYPE_PROPERTY);
        final Set<String> quoteTypes = constraints.getValues(SURFACE_QUOTE_TYPE_PROPERTY);
        final Set<String> quoteUnits = constraints.getValues(SURFACE_QUOTE_UNITS_PROPERTY);
        VolatilitySurfaceKey key = null;
        if ((names != null) && (instrumentTypes != null) && (quoteTypes != null) && (quoteUnits != null) && (names.size() == 1) && (instrumentTypes.size() == 1) && (quoteTypes.size() == 1) &&
            (quoteUnits.size() == 1)) {
          key = new VolatilitySurfaceKey(target, names.iterator().next(), instrumentTypes.iterator().next(), quoteTypes.iterator().next(), quoteUnits.iterator().next());
        } else {
          if (snapshot.getSnapshot().getVolatilitySurfaces() != null) {
            for (VolatilitySurfaceKey surface : snapshot.getSnapshot().getVolatilitySurfaces().keySet()) {
              if (!target.equals(surface.getTarget())) {
                continue;
              }
              if ((names != null) && !names.isEmpty() && !names.contains(surface.getName())) {
                continue;
              }
              if ((instrumentTypes != null) && !instrumentTypes.isEmpty() && !instrumentTypes.contains(surface.getInstrumentType())) {
                continue;
              }
              if ((quoteTypes != null) && !quoteTypes.isEmpty() && !quoteTypes.contains(surface.getQuoteType())) {
                continue;
              }
              if ((quoteUnits != null) && !quoteUnits.isEmpty() && !quoteUnits.contains(surface.getQuoteUnits())) {
                continue;
              }
              key = surface;
              break;
            }
          }
          if (key == null) {
            return null;
          }
        }
        final ValueProperties properties = ValueProperties.with(ValuePropertyNames.SURFACE,
            key.getName()).with(INSTRUMENT_TYPE_PROPERTY, key.getInstrumentType()).with(SURFACE_QUOTE_TYPE_PROPERTY, key.getQuoteType())
            .with(SURFACE_QUOTE_UNITS_PROPERTY, key.getQuoteUnits()).get();
        if (!constraints.isSatisfiedBy(properties)) {
          return null;
        }
        return Pair.of(key, MarketDataUtils.createMarketDataValue(ValueRequirementNames.VOLATILITY_SURFACE_DATA, valueRequirement.getTargetSpecification(), properties));
      }

    });
    registerStructuredKeyFactory(ValueRequirementNames.VOLATILITY_CUBE_MARKET_DATA, new StructuredMarketDataKeyFactory() {

      @Override
      public Pair<? extends StructuredMarketDataKey, ValueSpecification> fromRequirement(ValueRequirement valueRequirement, UserMarketDataSnapshot snapshot) {
        final Currency target = getCurrencyTarget(valueRequirement);
        if (target == null) {
          return null;
        }
        final ValueProperties constraints = valueRequirement.getConstraints();
        final Set<String> names = constraints.getValues(ValuePropertyNames.CUBE);
        VolatilityCubeKey key = null;
        if ((names != null) && (names.size() == 1)) {
          key = new VolatilityCubeKey(target, names.iterator().next());
        } else {
          if (snapshot.getSnapshot().getVolatilityCubes() != null) {
            for (VolatilityCubeKey cube : snapshot.getSnapshot().getVolatilityCubes().keySet()) {
              if (!target.equals(cube.getCurrency())) {
                continue;
              }
              if ((names != null) && !names.isEmpty() && !names.contains(cube.getName())) {
                continue;
              }
              key = cube;
              break;
            }
          }
          if (key == null) {
            return null;
          }
        }
        final ValueProperties properties = ValueProperties.with(ValuePropertyNames.CUBE, key.getName()).get();
        if (!constraints.isSatisfiedBy(properties)) {
          return null;
        }
        return Pair.of(key, MarketDataUtils.createMarketDataValue(ValueRequirementNames.VOLATILITY_CUBE_MARKET_DATA, valueRequirement.getTargetSpecification(), properties));
      }

    });
  }

  public UserMarketDataSnapshot(MarketDataSnapshotSource snapshotSource, UniqueId snapshotId) {
    _snapshotSource = snapshotSource;
    _snapshotId = snapshotId;
  }

  //-------------------------------------------------------------------------

  @Override
  public UniqueId getUniqueId() {
    return UniqueId.of(MARKET_DATA_SNAPSHOT_ID_SCHEME, "UserMarketDataSnapshot:" + getSnapshotTime());
  }

  @Override
  public Instant getSnapshotTimeIndication() {
    init();
    return getSnapshotTime();
  }

  @Override
  public void init() {
    try {
      _snapshot = getSnapshotSource().get(getSnapshotId());
    } catch (DataNotFoundException ex) {
      _snapshot = null;
    }
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
  public ComputedValue query(ValueRequirement requirement) {
    final StructuredMarketDataKeyFactory factory = s_structuredKeyFactories.get(requirement.getValueName());
    if (factory == null) {
      return null;
    }
    final Pair<? extends StructuredMarketDataKey, ValueSpecification> key = factory.fromRequirement(requirement, this);
    if (key != null) {
      final Object value = key.getFirst().accept(this);
      if (value == null) {
        return null;
      } else {
        return new ComputedValue(key.getSecond(), value);
      }
    } else {
      return queryUnstructured(requirement);
    }
  }

  //-------------------------------------------------------------------------
  private static void registerStructuredKeyFactory(String valueRequirementName, StructuredMarketDataKeyFactory factory) {
    s_structuredKeyFactories.put(valueRequirementName, factory);
  }

  private ComputedValue queryUnstructured(ValueRequirement requirement) {
    UnstructuredMarketDataSnapshot globalValues = getSnapshot().getGlobalValues();
    if (globalValues == null) {
      return null;
    }
    MarketDataValueSpecification marketDataValueSpecification = new MarketDataValueSpecification(
        getTargetType(requirement), requirement.getTargetSpecification().getUniqueId());

    Map<String, ValueSnapshot> map = globalValues.getValues().get(marketDataValueSpecification);
    if (map == null) {
      return null;
    }
    ValueSnapshot valueSnapshot = map.get(requirement.getValueName());
    return new ComputedValue(MarketDataUtils.createMarketDataValue(requirement), query(valueSnapshot));
  }

  @Override
  public Object visitYieldCurveKey(final YieldCurveKey marketDataKey) {
    YieldCurveSnapshot yieldCurveSnapshot = getYieldCurveSnapshot(marketDataKey);
    if (yieldCurveSnapshot == null) {
      return null;
    }
    return buildSnapshot(yieldCurveSnapshot);
  }

  @Override
  public Object visitVolatilitySurfaceKey(final VolatilitySurfaceKey marketDataKey) {
    final VolatilitySurfaceSnapshot volSurfaceSnapshot = getVolSurfaceSnapshot(marketDataKey);
    if (volSurfaceSnapshot == null) {
      return null;
    }
    return buildVolatilitySurfaceData(volSurfaceSnapshot, marketDataKey);
  }

  @Override
  public Object visitVolatilityCubeKey(final VolatilityCubeKey marketDataKey) {
    VolatilityCubeSnapshot volCubeSnapshot = getVolCubeSnapshot(marketDataKey);
    if (volCubeSnapshot == null) {
      return null;
    }
    return buildVolatilityCubeData(volCubeSnapshot);
  }

  private YieldCurveSnapshot getYieldCurveSnapshot(YieldCurveKey yieldcurveKey) {
    if (getSnapshot().getYieldCurves() == null) {
      return null;
    }
    return getSnapshot().getYieldCurves().get(yieldcurveKey);
  }

  private VolatilityCubeSnapshot getVolCubeSnapshot(VolatilityCubeKey volCubeKey) {
    if (getSnapshot().getVolatilityCubes() == null) {
      return null;
    }
    return getSnapshot().getVolatilityCubes().get(volCubeKey);
  }

  private VolatilitySurfaceSnapshot getVolSurfaceSnapshot(VolatilitySurfaceKey volSurfaceKey) {
    if (getSnapshot().getVolatilitySurfaces() == null) {
      return null;
    }
    return getSnapshot().getVolatilitySurfaces().get(volSurfaceKey);
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

  private VolatilitySurfaceData<Object, Object> buildVolatilitySurfaceData(VolatilitySurfaceSnapshot volCubeSnapshot, VolatilitySurfaceKey marketDataKey) {
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
