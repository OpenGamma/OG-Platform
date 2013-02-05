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

import org.threeten.bp.Instant;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.MarketDataValueSpecification;
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
import com.opengamma.core.marketdatasnapshot.impl.ManageableMarketDataSnapshot;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.AbstractMarketDataSnapshot;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalId;
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
public class UserMarketDataSnapshot extends AbstractMarketDataSnapshot implements StructuredMarketDataKey.Visitor<Object>, MarketDataAvailabilityProvider {

  private static final String INSTRUMENT_TYPE_PROPERTY = "InstrumentType";
  private static final String SURFACE_QUOTE_TYPE_PROPERTY = "SurfaceQuoteType";
  private static final String SURFACE_QUOTE_UNITS_PROPERTY = "SurfaceUnits";

  private static final Map<String, StructuredMarketDataHandler> s_structuredDataHandler = new HashMap<String, StructuredMarketDataHandler>();

  private final MarketDataSnapshotSource _snapshotSource;
  private final UniqueId _snapshotId;
  private StructuredMarketDataSnapshot _snapshot;

  /**
   * Handler for a type of structured market data.
   */
  private abstract static class StructuredMarketDataHandler {

    protected ValueProperties.Builder createValueProperties() {
      return ValueProperties.with(ValuePropertyNames.FUNCTION, "StructuredMarketData");
    }

    protected boolean isValidSnapshot(final StructuredMarketDataSnapshot snapshot) {
      return true;
    }

    protected boolean isValidTarget(final Object target) {
      return true;
    }

    protected ValueProperties resolve(final Object target, final StructuredMarketDataSnapshot snapshot) {
      assert false;
      throw new UnsupportedOperationException();
    }

    protected ValueProperties resolve(final Object target, final ValueProperties constraints, final StructuredMarketDataSnapshot snapshot) {
      return resolve(target, snapshot);
    }

    protected ValueProperties resolve(final Object target, final ValueRequirement desiredValue, final StructuredMarketDataSnapshot snapshot) {
      return resolve(target, desiredValue.getConstraints(), snapshot);
    }

    public ValueSpecification resolve(final ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue, final StructuredMarketDataSnapshot snapshot) {
      if (isValidSnapshot(snapshot) && isValidTarget(target)) {
        final ValueProperties properties = resolve(target, desiredValue, snapshot);
        if (properties != null) {
          if (desiredValue.getConstraints().isSatisfiedBy(properties)) {
            return new ValueSpecification(desiredValue.getValueName(), targetSpec, properties.compose(desiredValue.getConstraints()));
          }
        }
      }
      return null;
    }

    protected Object query(final UniqueId target, final StructuredMarketDataSnapshot snapshot) {
      assert false;
      throw new UnsupportedOperationException();
    }

    protected Object query(final UniqueId target, final ValueProperties properties, final StructuredMarketDataSnapshot snapshot) {
      return query(target, snapshot);
    }

    protected Object query(final ComputationTargetSpecification targetSpec, final ValueProperties properties, final StructuredMarketDataSnapshot snapshot) {
      return query(targetSpec.getUniqueId(), properties, snapshot);
    }

    public Object query(final ValueSpecification valueSpecification, final StructuredMarketDataSnapshot snapshot) {
      return query(valueSpecification.getTargetSpecification(), valueSpecification.getProperties(), snapshot);
    }

  }

  static {
    registerStructuredMarketDataHandler(ValueRequirementNames.YIELD_CURVE_MARKET_DATA, new StructuredMarketDataHandler() {

      @Override
      protected boolean isValidTarget(final Object target) {
        return target instanceof Currency;
      }

      @Override
      protected boolean isValidSnapshot(final StructuredMarketDataSnapshot snapshot) {
        return (snapshot.getYieldCurves() != null) && !snapshot.getYieldCurves().isEmpty();
      }

      @Override
      protected ValueProperties resolve(final Object target, final StructuredMarketDataSnapshot snapshot) {
        ValueProperties.Builder properties = null;
        for (final YieldCurveKey curve : snapshot.getYieldCurves().keySet()) {
          if (target.equals(curve.getCurrency())) {
            if (properties == null) {
              properties = createValueProperties();
            }
            properties.with(ValuePropertyNames.CURVE, curve.getName());
          }
        }
        if (properties != null) {
          return properties.get();
        } else {
          return null;
        }
      }

      @Override
      protected Object query(final UniqueId target, final ValueProperties properties, final StructuredMarketDataSnapshot snapshot) {
        final String name = properties.getValues(ValuePropertyNames.CURVE).iterator().next();
        return snapshot.getYieldCurves().get(new YieldCurveKey(Currency.of(target.getValue()), name));
      }

    });
    registerStructuredMarketDataHandler(ValueRequirementNames.VOLATILITY_SURFACE_DATA, new StructuredMarketDataHandler() {

      @Override
      protected boolean isValidTarget(final Object target) {
        return target instanceof UniqueIdentifiable;
      }

      @Override
      protected boolean isValidSnapshot(final StructuredMarketDataSnapshot snapshot) {
        return (snapshot.getVolatilitySurfaces() != null) && !snapshot.getVolatilitySurfaces().isEmpty();
      }

      @Override
      protected ValueProperties resolve(final Object targetObject, final ValueProperties constraints, final StructuredMarketDataSnapshot snapshot) {
        final UniqueId target = ((UniqueIdentifiable) targetObject).getUniqueId();
        final Set<String> names = constraints.getValues(ValuePropertyNames.SURFACE);
        final Set<String> instrumentTypes = constraints.getValues(INSTRUMENT_TYPE_PROPERTY);
        final Set<String> quoteTypes = constraints.getValues(SURFACE_QUOTE_TYPE_PROPERTY);
        final Set<String> quoteUnits = constraints.getValues(SURFACE_QUOTE_UNITS_PROPERTY);
        for (final VolatilitySurfaceKey surface : snapshot.getVolatilitySurfaces().keySet()) {
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
          return createValueProperties().with(ValuePropertyNames.SURFACE,
              surface.getName()).with(INSTRUMENT_TYPE_PROPERTY, surface.getInstrumentType()).with(SURFACE_QUOTE_TYPE_PROPERTY, surface.getQuoteType())
              .with(SURFACE_QUOTE_UNITS_PROPERTY, surface.getQuoteUnits()).get();
        }
        return null;
      }

      @Override
      protected Object query(final UniqueId target, final ValueProperties properties, final StructuredMarketDataSnapshot snapshot) {
        final String name = properties.getValues(ValuePropertyNames.SURFACE).iterator().next();
        final String instrumentType = properties.getValues(INSTRUMENT_TYPE_PROPERTY).iterator().next();
        final String quoteType = properties.getValues(SURFACE_QUOTE_TYPE_PROPERTY).iterator().next();
        final String quoteUnits = properties.getValues(SURFACE_QUOTE_UNITS_PROPERTY).iterator().next();
        return snapshot.getVolatilitySurfaces().get(new VolatilitySurfaceKey(target, name, instrumentType, quoteType, quoteUnits));
      }

    });
    registerStructuredMarketDataHandler(ValueRequirementNames.VOLATILITY_CUBE_MARKET_DATA, new StructuredMarketDataHandler() {

      @Override
      protected boolean isValidTarget(final Object target) {
        return target instanceof Currency;
      }

      @Override
      protected boolean isValidSnapshot(final StructuredMarketDataSnapshot snapshot) {
        return (snapshot.getVolatilityCubes() != null) && !snapshot.getVolatilityCubes().isEmpty();
      }

      @Override
      protected ValueProperties resolve(final Object target, final StructuredMarketDataSnapshot snapshot) {
        ValueProperties.Builder properties = null;
        for (final VolatilityCubeKey cube : snapshot.getVolatilityCubes().keySet()) {
          if (target.equals(cube.getCurrency())) {
            if (properties == null) {
              properties = createValueProperties();
            }
            properties.with(ValuePropertyNames.CUBE, cube.getName());
          }
        }
        if (properties != null) {
          return properties.get();
        } else {
          return null;
        }
      }

      @Override
      protected Object query(final UniqueId target, final ValueProperties properties, final StructuredMarketDataSnapshot snapshot) {
        final String name = properties.getValues(ValuePropertyNames.CUBE).iterator().next();
        return snapshot.getVolatilityCubes().get(new VolatilityCubeKey(Currency.of(target.getValue()), name));
      }

    });
  }

  public UserMarketDataSnapshot(final MarketDataSnapshotSource snapshotSource, final UniqueId snapshotId) {
    _snapshotSource = snapshotSource;
    _snapshotId = snapshotId;
  }

  private static void registerStructuredMarketDataHandler(final String valueRequirementName, final StructuredMarketDataHandler handler) {
    s_structuredDataHandler.put(valueRequirementName, handler);
  }

  @Override
  public Object visitYieldCurveKey(final YieldCurveKey marketDataKey) {
    final YieldCurveSnapshot yieldCurveSnapshot = getYieldCurveSnapshot(marketDataKey);
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
    final VolatilityCubeSnapshot volCubeSnapshot = getVolCubeSnapshot(marketDataKey);
    if (volCubeSnapshot == null) {
      return null;
    }
    return buildVolatilityCubeData(volCubeSnapshot);
  }

  private YieldCurveSnapshot getYieldCurveSnapshot(final YieldCurveKey yieldcurveKey) {
    if (getSnapshot().getYieldCurves() == null) {
      return null;
    }
    return getSnapshot().getYieldCurves().get(yieldcurveKey);
  }

  private VolatilityCubeSnapshot getVolCubeSnapshot(final VolatilityCubeKey volCubeKey) {
    if (getSnapshot().getVolatilityCubes() == null) {
      return null;
    }
    return getSnapshot().getVolatilityCubes().get(volCubeKey);
  }

  private VolatilitySurfaceSnapshot getVolSurfaceSnapshot(final VolatilitySurfaceKey volSurfaceKey) {
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

  private Double query(final ValueSnapshot valueSnapshot) {
    if (valueSnapshot == null) {
      return null;
    }
    //TODO configure which value to use
    if (valueSnapshot.getOverrideValue() != null) {
      return valueSnapshot.getOverrideValue();
    }
    return valueSnapshot.getMarketValue();
  }

  private SnapshotDataBundle buildSnapshot(final YieldCurveSnapshot yieldCurveSnapshot) {
    final UnstructuredMarketDataSnapshot values = yieldCurveSnapshot.getValues();
    return buildBundle(values);
  }

  private SnapshotDataBundle buildBundle(final UnstructuredMarketDataSnapshot values) {
    final SnapshotDataBundle ret = new SnapshotDataBundle();
    final HashMap<ExternalId, Double> points = new HashMap<ExternalId, Double>();
    for (final Entry<MarketDataValueSpecification, Map<String, ValueSnapshot>> entry : values.getValues().entrySet()) {
      final Double value = query(entry.getValue().get(MarketDataRequirementNames.MARKET_VALUE));
      points.put(entry.getKey().getIdentifier(), value);
    }
    ret.setDataPoints(points);
    return ret;
  }

  private VolatilitySurfaceData<Object, Object> buildVolatilitySurfaceData(final VolatilitySurfaceSnapshot volCubeSnapshot, final VolatilitySurfaceKey marketDataKey) {
    final Set<Object> xs = new HashSet<Object>();
    final Set<Object> ys = new HashSet<Object>();
    final Map<Pair<Object, Object>, Double> values = new HashMap<Pair<Object, Object>, Double>();
    final Map<Pair<Object, Object>, ValueSnapshot> snapValues = volCubeSnapshot.getValues();
    for (final Entry<Pair<Object, Object>, ValueSnapshot> entry : snapValues.entrySet()) {
      values.put(entry.getKey(), query(entry.getValue()));
      xs.add(entry.getKey().getFirst());
      ys.add(entry.getKey().getSecond());
    }
    return new VolatilitySurfaceData<Object, Object>(marketDataKey.getName(), "UNKNOWN", marketDataKey.getTarget(),
        xs.toArray(), ys.toArray(), values);
  }

  private VolatilityCubeData buildVolatilityCubeData(final VolatilityCubeSnapshot volCubeSnapshot) {
    final Map<VolatilityPoint, ValueSnapshot> values = volCubeSnapshot.getValues();
    final HashMap<VolatilityPoint, Double> dataPoints = buildVolValues(values);
    final HashMap<Pair<Tenor, Tenor>, Double> strikes = buildVolStrikes(volCubeSnapshot.getStrikes());
    final SnapshotDataBundle otherData = buildBundle(volCubeSnapshot.getOtherValues());

    final VolatilityCubeData ret = new VolatilityCubeData();
    ret.setDataPoints(dataPoints);
    ret.setOtherData(otherData);
    ret.setATMStrikes(strikes);

    return ret;
  }

  private HashMap<Pair<Tenor, Tenor>, Double> buildVolStrikes(final Map<Pair<Tenor, Tenor>, ValueSnapshot> strikes) {
    final HashMap<Pair<Tenor, Tenor>, Double> dataPoints = new HashMap<Pair<Tenor, Tenor>, Double>();
    for (final Entry<Pair<Tenor, Tenor>, ValueSnapshot> entry : strikes.entrySet()) {
      final ValueSnapshot value = entry.getValue();
      final Double query = query(value);
      if (query != null) {
        dataPoints.put(entry.getKey(), query);
      }
    }
    return dataPoints;
  }

  private HashMap<VolatilityPoint, Double> buildVolValues(final Map<VolatilityPoint, ValueSnapshot> values) {
    final HashMap<VolatilityPoint, Double> dataPoints = new HashMap<VolatilityPoint, Double>();
    for (final Entry<VolatilityPoint, ValueSnapshot> entry : values.entrySet()) {
      final ValueSnapshot value = entry.getValue();
      final Double query = query(value);
      if (query != null) {
        dataPoints.put(entry.getKey(), query);
      }
    }
    return dataPoints;
  }

  protected ValueSpecification resolveUnstructured(final ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue) {
    final UnstructuredMarketDataSnapshot snapshot = getSnapshot().getGlobalValues();
    snapshot.getValues();
    throw new UnsupportedOperationException("PLAT-3044 TODO");
  }

  protected Object queryUnstructured(final ValueSpecification valueSpecification) {
    throw new UnsupportedOperationException("PLAT-3044 TODO");
  }

  // AbstractMarketDataSnapshot

  @Override
  public UniqueId getUniqueId() {
    return _snapshotId;
  }

  @Override
  public Instant getSnapshotTimeIndication() {
    init();
    return getSnapshotTime();
  }

  @Override
  public void init() {
    if (_snapshot == null) {
      try {
        _snapshot = getSnapshotSource().get(getSnapshotId());
      } catch (final DataNotFoundException ex) {
        // Can't leave as null or there will be errors about an uninitialized snapshot. Throwing an exception here might be better than assuming
        // an empty snapshot.
        _snapshot = new ManageableMarketDataSnapshot();
      }
    }
  }

  @Override
  public void init(final Set<ValueSpecification> valuesRequired, final long timeout, final TimeUnit unit) {
    init();
  }

  @Override
  public Instant getSnapshotTime() {
    // TODO [PLAT-1393] should explicitly store a snapshot time, which the user might choose to customise
    Instant latestTimestamp = null;
    final Map<YieldCurveKey, YieldCurveSnapshot> yieldCurves = getSnapshot().getYieldCurves();
    if (yieldCurves != null) {
      for (final YieldCurveSnapshot yieldCurveSnapshot : yieldCurves.values()) {
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
  public Object query(final ValueSpecification valueSpecification) {
    final StructuredMarketDataHandler handler = s_structuredDataHandler.get(valueSpecification.getValueName());
    if (handler == null) {
      return queryUnstructured(valueSpecification);
    } else {
      return handler.query(valueSpecification, getSnapshot());
    }
  }

  // MarketDataAvailabilityProvider

  @Override
  public ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue) {
    final StructuredMarketDataHandler handler = s_structuredDataHandler.get(desiredValue.getValueName());
    if (handler == null) {
      return resolveUnstructured(targetSpec, target, desiredValue);
    }
    return handler.resolve(targetSpec, target, desiredValue, getSnapshot());
  }

}
