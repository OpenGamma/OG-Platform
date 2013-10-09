/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.snapshot;

import static java.lang.String.format;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.threeten.bp.Instant;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.marketdatasnapshot.CurveKey;
import com.opengamma.core.marketdatasnapshot.CurveSnapshot;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
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
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.AbstractMarketDataSnapshot;
import com.opengamma.engine.marketdata.InMemoryLKVMarketDataProvider;
import com.opengamma.engine.marketdata.availability.DefaultMarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityFilter;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.availability.ProviderMarketDataAvailabilityFilter;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetRequirement;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;
;

// REVIEW jonathan 2011-06-29 -- The user market data provider classes, including this, no longer need to be in the
// engine and they simply introduce dependencies on the MarketDataSnapshotSource and specific StructuredMarketDataKeys.
// They are a perfect example of adding a custom market data source and should be moved elsewhere.
/**
 * Represents a market data snapshot from a {@link com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource}.
 */
public class UserMarketDataSnapshot extends AbstractMarketDataSnapshot {

  private static final String INSTRUMENT_TYPE_PROPERTY = "InstrumentType";
  private static final String SURFACE_QUOTE_TYPE_PROPERTY = "SurfaceQuoteType";
  private static final String SURFACE_QUOTE_UNITS_PROPERTY = "SurfaceUnits";

  private static final Map<String, StructuredMarketDataHandler> s_structuredDataHandler = new HashMap<String, StructuredMarketDataHandler>();

  private InMemoryLKVMarketDataProvider _unstructured;
  private final StructuredMarketDataSnapshot _snapshot;
  

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
      return resolve(target, constraints, snapshot);
    }

    protected ValueProperties resolve(final Object target, final ValueRequirement desiredValue, final StructuredMarketDataSnapshot snapshot) {
      return resolve(target, desiredValue.getConstraints(), snapshot);
    }

    public ValueSpecification resolve(ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue, final StructuredMarketDataSnapshot snapshot) {
      if (isValidSnapshot(snapshot) && isValidTarget(target)) {
        final ValueProperties properties = resolve(target, desiredValue, snapshot);
        if (properties != null) {
          if (desiredValue.getConstraints().isSatisfiedBy(properties)) {
            if (targetSpec == null) {
              targetSpec = DefaultMarketDataAvailabilityProvider.createPrimitiveComputationTargetSpecification(target);
            }
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
      protected ValueProperties resolve(final Object target, final ValueProperties constraints, final StructuredMarketDataSnapshot snapshot) {
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
        if (snapshot.getYieldCurves() != null) {
          final YieldCurveSnapshot data = snapshot.getYieldCurves().get(new YieldCurveKey(Currency.of(target.getValue()), name));
          if (data != null) {
            return convertYieldCurveMarketData(data);
          }
        }
        return null;
      }

    });
    registerStructuredMarketDataHandler(ValueRequirementNames.CURVE_MARKET_DATA, new StructuredMarketDataHandler() {

      @Override
      protected boolean isValidTarget(final Object target) {
        return target == null;
      }

      @Override
      protected boolean isValidSnapshot(final StructuredMarketDataSnapshot snapshot) {
        return (snapshot.getCurves() != null) && !snapshot.getCurves().isEmpty();
      }

      @Override
      protected ValueProperties resolve(final Object target, final ValueProperties constraints, final StructuredMarketDataSnapshot snapshot) {
        ValueProperties.Builder properties = null;
        for (final CurveKey curve : snapshot.getCurves().keySet()) {
          if (curve.getName().equals(Iterables.getOnlyElement(constraints.getValues(ValuePropertyNames.CURVE)))) {
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
        if (snapshot.getCurves() != null) {
          final CurveSnapshot data = snapshot.getCurves().get(new CurveKey(name));
          if (data != null) {
            return convertCurveMarketData(data);
          }
        }
        return null;
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
        if (snapshot.getVolatilitySurfaces() != null) {
          final VolatilitySurfaceKey key = new VolatilitySurfaceKey(target, name, instrumentType, quoteType, quoteUnits);
          final VolatilitySurfaceSnapshot data = snapshot.getVolatilitySurfaces().get(key);
          if (data != null) {
            return createVolatilitySurfaceData(data, key);
          }
        }
        return null;
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
      protected ValueProperties resolve(final Object target, final ValueProperties constraints, final StructuredMarketDataSnapshot snapshot) {
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
        if (snapshot.getVolatilityCubes() != null) {
          final VolatilityCubeSnapshot data = snapshot.getVolatilityCubes().get(new VolatilityCubeKey(Currency.of(target.getValue()), name));
          if (data != null) {
            return convertVolatilityCubeMarketData(data);
          }
        }
        return null;
      }

    });
  }

  public UserMarketDataSnapshot(final StructuredMarketDataSnapshot snapshot) {
    _snapshot = snapshot;
  }

  private static void registerStructuredMarketDataHandler(final String valueRequirementName, final StructuredMarketDataHandler handler) {
    s_structuredDataHandler.put(valueRequirementName, handler);
  }

  private StructuredMarketDataSnapshot getSnapshot() {
    return _snapshot;
  }

  private static Object query(final ValueSnapshot valueSnapshot) {
    if (valueSnapshot == null) {
      return null;
    }
    // TODO: If there is a use case to run a snapshot with the original values then we might want a mode to use the original values
    // instead of the overrides. The alternative is to create a new snapshot programmatically (or revert to an earlier version) which
    // does not have the override values.
    if (valueSnapshot.getOverrideValue() != null) {
      return valueSnapshot.getOverrideValue();
    } else {
      return valueSnapshot.getMarketValue();
    }
  }
  
  private static Double queryDouble(final ValueSnapshot valueSnapshot) {
    Object objResult = query(valueSnapshot);
    if (objResult == null //original query() would return null for Doubles so do same here
        || objResult instanceof Double) {
      return (Double) objResult;
    } else {
      throw new OpenGammaRuntimeException(format("Double was expected in snapshot but Object instance of type %s found instead.", objResult.getClass()));
    }
  }

  private static SnapshotDataBundle createSnapshotDataBundle(final UnstructuredMarketDataSnapshot values) {
    final SnapshotDataBundle ret = new SnapshotDataBundle();
    for (final ExternalIdBundle target : values.getTargets()) {
      final Double value = queryDouble(values.getValue(target, MarketDataRequirementNames.MARKET_VALUE));
      ret.setDataPoint(target, value);
    }
    return ret;
  }

  private static SnapshotDataBundle convertYieldCurveMarketData(final YieldCurveSnapshot yieldCurveSnapshot) {
    return createSnapshotDataBundle(yieldCurveSnapshot.getValues());
  }

  private static SnapshotDataBundle convertCurveMarketData(final CurveSnapshot curveSnapshot) {
    return createSnapshotDataBundle(curveSnapshot.getValues());
  }

  private static VolatilitySurfaceData<Object, Object> createVolatilitySurfaceData(final VolatilitySurfaceSnapshot volCubeSnapshot, final VolatilitySurfaceKey marketDataKey) {
    final Set<Object> xs = new HashSet<Object>();
    final Set<Object> ys = new HashSet<Object>();
    final Map<Pair<Object, Object>, Double> values = new HashMap<Pair<Object, Object>, Double>();
    final Map<Pair<Object, Object>, ValueSnapshot> snapValues = volCubeSnapshot.getValues();
    for (final Entry<Pair<Object, Object>, ValueSnapshot> entry : snapValues.entrySet()) {
      values.put(entry.getKey(), queryDouble(entry.getValue()));
      xs.add(entry.getKey().getFirst());
      ys.add(entry.getKey().getSecond());
    }
    return new VolatilitySurfaceData<Object, Object>(marketDataKey.getName(), "UNKNOWN", marketDataKey.getTarget(),
        xs.toArray(), ys.toArray(), values);
  }

  private static VolatilityCubeData convertVolatilityCubeMarketData(final VolatilityCubeSnapshot volCubeSnapshot) {
    final Map<VolatilityPoint, ValueSnapshot> values = volCubeSnapshot.getValues();
    final HashMap<VolatilityPoint, Double> dataPoints = buildVolValues(values);
    final HashMap<Pair<Tenor, Tenor>, Double> strikes = buildVolStrikes(volCubeSnapshot.getStrikes());
    final SnapshotDataBundle otherData = createSnapshotDataBundle(volCubeSnapshot.getOtherValues());
    final VolatilityCubeData ret = new VolatilityCubeData();
    ret.setDataPoints(dataPoints);
    ret.setOtherData(otherData);
    ret.setATMStrikes(strikes);
    return ret;
  }

  private static HashMap<VolatilityPoint, Double> buildVolValues(final Map<VolatilityPoint, ValueSnapshot> values) {
    final HashMap<VolatilityPoint, Double> dataPoints = new HashMap<VolatilityPoint, Double>();
    for (final Entry<VolatilityPoint, ValueSnapshot> entry : values.entrySet()) {
      final ValueSnapshot value = entry.getValue();
      final Double query = queryDouble(value);
      if (query != null) {
        dataPoints.put(entry.getKey(), query);
      }
    }
    return dataPoints;
  }

  private static HashMap<Pair<Tenor, Tenor>, Double> buildVolStrikes(final Map<Pair<Tenor, Tenor>, ValueSnapshot> strikes) {
    final HashMap<Pair<Tenor, Tenor>, Double> dataPoints = new HashMap<Pair<Tenor, Tenor>, Double>();
    for (final Entry<Pair<Tenor, Tenor>, ValueSnapshot> entry : strikes.entrySet()) {
      final ValueSnapshot value = entry.getValue();
      final Double query = queryDouble(value);
      if (query != null) {
        dataPoints.put(entry.getKey(), query);
      }
    }
    return dataPoints;
  }

  // AbstractMarketDataSnapshot

  @Override
  public UniqueId getUniqueId() {
    return getSnapshot().getUniqueId();
  }

  @Override
  public Instant getSnapshotTimeIndication() {
    return getSnapshotTime();
  }

  @Override
  public synchronized void init() {
    if (!isInitialized()) {
      _unstructured = new InMemoryLKVMarketDataProvider();
      final UnstructuredMarketDataSnapshot globalValues = _snapshot.getGlobalValues();
      if (globalValues != null) {
        for (final ExternalIdBundle target : globalValues.getTargets()) {
          final ComputationTargetReference targetRef = new ComputationTargetRequirement(ComputationTargetType.PRIMITIVE, target);
          for (final Map.Entry<String, ValueSnapshot> valuePair : globalValues.getTargetValues(target).entrySet()) {
            ValueRequirement valueRequirement = new ValueRequirement(valuePair.getKey(), targetRef);
            _unstructured.addValue(valueRequirement, query(valuePair.getValue()));
          }
        }
      }
    }
  }

  @Override
  public void init(final Set<ValueSpecification> valuesRequired, final long timeout, final TimeUnit unit) {
    init();
  }

  @Override
  public synchronized boolean isInitialized() {
    return _unstructured != null;
  }

  @Override
  public boolean isEmpty() {
    return false;
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
    final Map<CurveKey, CurveSnapshot> curves = getSnapshot().getCurves();
    if (curves != null) {
      for (final CurveSnapshot curveSnapshot : curves.values()) {
        if (latestTimestamp == null || latestTimestamp.isBefore(curveSnapshot.getValuationTime())) {
          latestTimestamp = curveSnapshot.getValuationTime();
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
      return _unstructured.getCurrentValue(valueSpecification);
    } else {
      return handler.query(valueSpecification, getSnapshot());
    }
  }

  // MarketDataProvider

  public MarketDataAvailabilityProvider getAvailabilityProvider() {
    assertInitialized();
    final MarketDataAvailabilityProvider unstructured = _unstructured.getAvailabilityProvider(MarketData.live());
    return new MarketDataAvailabilityProvider() {

      @Override
      public ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue) {
        final StructuredMarketDataHandler handler = s_structuredDataHandler.get(desiredValue.getValueName());
        if (handler == null) {
          return unstructured.getAvailability(targetSpec, target, desiredValue);
        } else {
          return handler.resolve(targetSpec, target, desiredValue, getSnapshot());
        }
      }


      @Override
      public MarketDataAvailabilityFilter getAvailabilityFilter() {
        return new ProviderMarketDataAvailabilityFilter(this);
      }

      @Override
      public Serializable getAvailabilityHintKey() {
        final ArrayList<Serializable> key = new ArrayList<Serializable>();
        key.add(getClass().getName());
        key.add(getUniqueId());
        return key;
      }

    };
  }

}
