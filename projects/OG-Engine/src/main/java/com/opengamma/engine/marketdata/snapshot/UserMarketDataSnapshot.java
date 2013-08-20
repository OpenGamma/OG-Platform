/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.snapshot;

import static java.lang.String.format;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

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

  private static final Map<String, StructuredMarketDataHandler> s_structuredDataHandlers =
      ImmutableMap.of(ValueRequirementNames.YIELD_CURVE_MARKET_DATA, new YieldCurveDataHandler(),
                      ValueRequirementNames.CURVE_MARKET_DATA, new CurveDataHandler(),
                      ValueRequirementNames.VOLATILITY_SURFACE_DATA, new SurfaceDataHandler(),
                      ValueRequirementNames.VOLATILITY_CUBE_MARKET_DATA, new CubeDataHandler());

  private InMemoryLKVMarketDataProvider _unstructured;
  private final StructuredMarketDataSnapshot _snapshot;

  public UserMarketDataSnapshot(StructuredMarketDataSnapshot snapshot) {
    ArgumentChecker.notNull(snapshot, "snapshot");
    _snapshot = snapshot;
  }

  private static Object query(ValueSnapshot valueSnapshot) {
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

  private static Double queryDouble(ValueSnapshot valueSnapshot) {
    Object objResult = query(valueSnapshot);
    if (objResult == null //original query() would return null for Doubles so do same here
        || objResult instanceof Double) {
      return (Double) objResult;
    } else {
      throw new OpenGammaRuntimeException(format(
          "Double was expected in snapshot but Object instance of type %s found instead.",
          objResult.getClass()));
    }
  }

  private static SnapshotDataBundle createSnapshotDataBundle(UnstructuredMarketDataSnapshot values) {
    SnapshotDataBundle ret = new SnapshotDataBundle();
    for (ExternalIdBundle target : values.getTargets()) {
      Double value = queryDouble(values.getValue(target, MarketDataRequirementNames.MARKET_VALUE));
      ret.setDataPoint(target, value);
    }
    return ret;
  }

  private static SnapshotDataBundle convertYieldCurveMarketData(YieldCurveSnapshot yieldCurveSnapshot) {
    return createSnapshotDataBundle(yieldCurveSnapshot.getValues());
  }

  private static SnapshotDataBundle convertCurveMarketData(CurveSnapshot curveSnapshot) {
    return createSnapshotDataBundle(curveSnapshot.getValues());
  }

  private static VolatilitySurfaceData<Object, Object> createVolatilitySurfaceData(VolatilitySurfaceSnapshot volCubeSnapshot,
                                                                                   VolatilitySurfaceKey marketDataKey) {
    Set<Object> xs = Sets.newHashSet();
    Set<Object> ys = Sets.newHashSet();
    Map<Pair<Object, Object>, Double> values = Maps.newHashMap();
    Map<Pair<Object, Object>, ValueSnapshot> snapValues = volCubeSnapshot.getValues();
    for (Entry<Pair<Object, Object>, ValueSnapshot> entry : snapValues.entrySet()) {
      values.put(entry.getKey(), queryDouble(entry.getValue()));
      xs.add(entry.getKey().getFirst());
      ys.add(entry.getKey().getSecond());
    }
    return new VolatilitySurfaceData<>(marketDataKey.getName(), "UNKNOWN", marketDataKey.getTarget(),
                                       xs.toArray(), ys.toArray(), values);
  }

  private static VolatilityCubeData convertVolatilityCubeMarketData(VolatilityCubeSnapshot volCubeSnapshot) {
    Map<VolatilityPoint, ValueSnapshot> values = volCubeSnapshot.getValues();
    Map<VolatilityPoint, Double> dataPoints = buildVolValues(values);
    Map<Pair<Tenor, Tenor>, Double> strikes = buildVolStrikes(volCubeSnapshot.getStrikes());
    SnapshotDataBundle otherData = createSnapshotDataBundle(volCubeSnapshot.getOtherValues());
    VolatilityCubeData ret = new VolatilityCubeData();
    ret.setDataPoints(dataPoints);
    ret.setOtherData(otherData);
    ret.setATMStrikes(strikes);
    return ret;
  }

  private static Map<VolatilityPoint, Double> buildVolValues(Map<VolatilityPoint, ValueSnapshot> values) {
    Map<VolatilityPoint, Double> dataPoints = Maps.newHashMap();
    for (Entry<VolatilityPoint, ValueSnapshot> entry : values.entrySet()) {
      ValueSnapshot value = entry.getValue();
      Double query = queryDouble(value);
      if (query != null) {
        dataPoints.put(entry.getKey(), query);
      }
    }
    return dataPoints;
  }

  private static Map<Pair<Tenor, Tenor>, Double> buildVolStrikes(Map<Pair<Tenor, Tenor>, ValueSnapshot> strikes) {
    Map<Pair<Tenor, Tenor>, Double> dataPoints = Maps.newHashMap();
    for (Entry<Pair<Tenor, Tenor>, ValueSnapshot> entry : strikes.entrySet()) {
      ValueSnapshot value = entry.getValue();
      Double query = queryDouble(value);
      if (query != null) {
        dataPoints.put(entry.getKey(), query);
      }
    }
    return dataPoints;
  }

  // AbstractMarketDataSnapshot

  @Override
  public UniqueId getUniqueId() {
    return _snapshot.getUniqueId();
  }

  @Override
  public Instant getSnapshotTimeIndication() {
    return getSnapshotTime();
  }

  @Override
  public synchronized void init() {
    if (!isInitialized()) {
      _unstructured = new InMemoryLKVMarketDataProvider();
      UnstructuredMarketDataSnapshot globalValues = _snapshot.getGlobalValues();
      if (globalValues != null) {
        for (ExternalIdBundle target : globalValues.getTargets()) {
          ComputationTargetReference targetRef = new ComputationTargetRequirement(ComputationTargetType.PRIMITIVE, target);
          for (Map.Entry<String, ValueSnapshot> valuePair : globalValues.getTargetValues(target).entrySet()) {
            _unstructured.addValue(new ValueRequirement(valuePair.getKey(), targetRef), query(valuePair.getValue()));
          }
        }
      }
    }
  }

  @Override
  public void init(Set<ValueSpecification> valuesRequired, long timeout, TimeUnit unit) {
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
    Map<YieldCurveKey, YieldCurveSnapshot> yieldCurves = _snapshot.getYieldCurves();
    if (yieldCurves != null) {
      for (YieldCurveSnapshot yieldCurveSnapshot : yieldCurves.values()) {
        if (latestTimestamp == null || latestTimestamp.isBefore(yieldCurveSnapshot.getValuationTime())) {
          latestTimestamp = yieldCurveSnapshot.getValuationTime();
        }
      }
    }
    Map<CurveKey, CurveSnapshot> curves = _snapshot.getCurves();
    if (curves != null) {
      for (CurveSnapshot curveSnapshot : curves.values()) {
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
  public Object query(ValueSpecification valueSpecification) {
    StructuredMarketDataHandler handler = s_structuredDataHandlers.get(valueSpecification.getValueName());
    if (handler == null) {
      return _unstructured.getCurrentValue(valueSpecification);
    } else {
      return handler.query(valueSpecification, _snapshot);
    }
  }

  // MarketDataProvider

  public MarketDataAvailabilityProvider getAvailabilityProvider() {
    assertInitialized();
    final MarketDataAvailabilityProvider unstructured = _unstructured.getAvailabilityProvider(MarketData.live());
    return new MarketDataAvailabilityProvider() {

      @Override
      public ValueSpecification getAvailability(ComputationTargetSpecification targetSpec,
                                                Object target,
                                                ValueRequirement desiredValue) {
        StructuredMarketDataHandler handler = s_structuredDataHandlers.get(desiredValue.getValueName());
        if (handler == null) {
          return unstructured.getAvailability(targetSpec, target, desiredValue);
        } else {
          return handler.resolve(targetSpec, target, desiredValue, _snapshot);
        }
      }

      @Override
      public MarketDataAvailabilityFilter getAvailabilityFilter() {
        return new ProviderMarketDataAvailabilityFilter(this);
      }

      @Override
      public Serializable getAvailabilityHintKey() {
        ArrayList<Serializable> key = Lists.newArrayList();
        key.add(getClass().getName());
        key.add(getUniqueId());
        return key;
      }

    };
  }

  /**
   * Handler for a type of structured market data. Converts the data stored in the database into an object that
   * can be consumed by the engine.
   */
  private abstract static class StructuredMarketDataHandler {

    protected ValueProperties.Builder createValueProperties() {
      return ValueProperties.with(ValuePropertyNames.FUNCTION, "StructuredMarketData");
    }

    /**
     * @param snapshot A snapshot of market data
     * @return Whether the snapshot contains data that this handler can convert.
     */
    protected abstract boolean isValidSnapshot(StructuredMarketDataSnapshot snapshot);

    protected abstract boolean isValidTarget(Object target);

    protected abstract ValueProperties resolve(Object target,
                                               ValueProperties constraints,
                                               StructuredMarketDataSnapshot snapshot);

    protected ValueProperties resolve(Object target,
                                      ValueRequirement desiredValue,
                                      StructuredMarketDataSnapshot snapshot) {
      return resolve(target, desiredValue.getConstraints(), snapshot);
    }

    public ValueSpecification resolve(ComputationTargetSpecification targetSpec,
                                      Object target,
                                      ValueRequirement desiredValue,
                                      StructuredMarketDataSnapshot snapshot) {
      if (isValidSnapshot(snapshot) && isValidTarget(target)) {
        ValueProperties properties = resolve(target, desiredValue, snapshot);
        if (properties != null) {
          if (desiredValue.getConstraints().isSatisfiedBy(properties)) {
            if (targetSpec == null) {
              targetSpec = DefaultMarketDataAvailabilityProvider.createPrimitiveComputationTargetSpecification(target);
            }
            return new ValueSpecification(desiredValue.getValueName(),
                                          targetSpec,
                                          properties.compose(desiredValue.getConstraints()));
          }
        }
      }
      return null;
    }

    protected abstract Object query(UniqueId target, ValueProperties properties, StructuredMarketDataSnapshot snapshot);

    public Object query(ValueSpecification valueSpecification, StructuredMarketDataSnapshot snapshot) {
      return query(valueSpecification.getTargetSpecification().getUniqueId(),
                   valueSpecification.getProperties(),
                   snapshot);
    }
  }

  /**
   * Converts a {@link VolatilitySurfaceSnapshot} into {@link VolatilitySurfaceData}.
   */
  private static class SurfaceDataHandler extends StructuredMarketDataHandler {

    @Override
    protected boolean isValidTarget(Object target) {
      return target instanceof UniqueIdentifiable;
    }

    @Override
    protected boolean isValidSnapshot(StructuredMarketDataSnapshot snapshot) {
      return (snapshot.getVolatilitySurfaces() != null) && !snapshot.getVolatilitySurfaces().isEmpty();
    }

    @Override
    protected ValueProperties resolve(Object targetObject,
                                      ValueProperties constraints,
                                      StructuredMarketDataSnapshot snapshot) {
      UniqueId target = ((UniqueIdentifiable) targetObject).getUniqueId();
      Set<String> names = constraints.getValues(ValuePropertyNames.SURFACE);
      Set<String> instrumentTypes = constraints.getValues(INSTRUMENT_TYPE_PROPERTY);
      Set<String> quoteTypes = constraints.getValues(SURFACE_QUOTE_TYPE_PROPERTY);
      Set<String> quoteUnits = constraints.getValues(SURFACE_QUOTE_UNITS_PROPERTY);
      for (VolatilitySurfaceKey surface : snapshot.getVolatilitySurfaces().keySet()) {
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
                                            surface.getName()).with(INSTRUMENT_TYPE_PROPERTY,
                                                                    surface.getInstrumentType()).with(
            SURFACE_QUOTE_TYPE_PROPERTY,
            surface.getQuoteType())
            .with(SURFACE_QUOTE_UNITS_PROPERTY, surface.getQuoteUnits()).get();
      }
      return null;
    }

    @Override
    protected VolatilitySurfaceData<Object, Object> query(UniqueId target,
                                                          ValueProperties properties,
                                                          StructuredMarketDataSnapshot snapshot) {
      String name = properties.getValues(ValuePropertyNames.SURFACE).iterator().next();
      String instrumentType = properties.getValues(INSTRUMENT_TYPE_PROPERTY).iterator().next();
      String quoteType = properties.getValues(SURFACE_QUOTE_TYPE_PROPERTY).iterator().next();
      String quoteUnits = properties.getValues(SURFACE_QUOTE_UNITS_PROPERTY).iterator().next();
      if (snapshot.getVolatilitySurfaces() != null) {
        VolatilitySurfaceKey key = new VolatilitySurfaceKey(target, name, instrumentType, quoteType, quoteUnits);
        VolatilitySurfaceSnapshot data = snapshot.getVolatilitySurfaces().get(key);
        if (data != null) {
          return createVolatilitySurfaceData(data, key);
        }
      }
      return null;
    }
  }

  /**
   * Converts a {@link VolatilityCubeSnapshot} into {@link VolatilityCubeData}.
   */
  private static class CubeDataHandler extends StructuredMarketDataHandler {

    @Override
    protected boolean isValidTarget(Object target) {
      return target instanceof Currency;
    }

    @Override
    protected boolean isValidSnapshot(StructuredMarketDataSnapshot snapshot) {
      return (snapshot.getVolatilityCubes() != null) && !snapshot.getVolatilityCubes().isEmpty();
    }

    @Override
    protected ValueProperties resolve(Object target,
                                      ValueProperties constraints,
                                      StructuredMarketDataSnapshot snapshot) {
      ValueProperties.Builder properties = null;
      for (VolatilityCubeKey cube : snapshot.getVolatilityCubes().keySet()) {
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
    protected VolatilityCubeData query(UniqueId target, ValueProperties properties, StructuredMarketDataSnapshot snapshot) {
      String name = properties.getValues(ValuePropertyNames.CUBE).iterator().next();
      if (snapshot.getVolatilityCubes() != null) {
        VolatilityCubeKey key = new VolatilityCubeKey(Currency.of(target.getValue()), name);
        VolatilityCubeSnapshot data = snapshot.getVolatilityCubes().get(key);
        if (data != null) {
          return convertVolatilityCubeMarketData(data);
        }
      }
      return null;
    }
  }

  /**
   * Converts a {@link YieldCurveSnapshot} into a {@link SnapshotDataBundle}.
   */
  private static class YieldCurveDataHandler extends StructuredMarketDataHandler {

    @Override
    protected boolean isValidTarget(Object target) {
      return target instanceof Currency;
    }

    @Override
    protected boolean isValidSnapshot(StructuredMarketDataSnapshot snapshot) {
      return (snapshot.getYieldCurves() != null) && !snapshot.getYieldCurves().isEmpty();
    }

    @Override
    protected ValueProperties resolve(Object target,
                                      ValueProperties constraints,
                                      StructuredMarketDataSnapshot snapshot) {
      ValueProperties.Builder properties = null;
      for (YieldCurveKey curve : snapshot.getYieldCurves().keySet()) {
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
    protected SnapshotDataBundle query(UniqueId target, ValueProperties properties, StructuredMarketDataSnapshot snapshot) {
      String name = properties.getValues(ValuePropertyNames.CURVE).iterator().next();
      if (snapshot.getYieldCurves() != null) {
        YieldCurveKey key = new YieldCurveKey(Currency.of(target.getValue()), name);
        YieldCurveSnapshot data = snapshot.getYieldCurves().get(key);
        if (data != null) {
          return convertYieldCurveMarketData(data);
        }
      }
      return null;
    }
  }

  /**
   * Converts a {@link CurveSnapshot} into a {@link SnapshotDataBundle}.
   */
  private static class CurveDataHandler extends StructuredMarketDataHandler {

    @Override
    protected boolean isValidTarget(Object target) {
      // TODO this *has* to be wrong. presumably this is never used?
      return target == null;
    }

    @Override
    protected boolean isValidSnapshot(StructuredMarketDataSnapshot snapshot) {
      return (snapshot.getCurves() != null) && !snapshot.getCurves().isEmpty();
    }

    @Override
    protected ValueProperties resolve(Object target,
                                      ValueProperties constraints,
                                      StructuredMarketDataSnapshot snapshot) {
      ValueProperties.Builder properties = null;
      for (CurveKey curve : snapshot.getCurves().keySet()) {
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
    protected SnapshotDataBundle query(UniqueId target, ValueProperties properties, StructuredMarketDataSnapshot snapshot) {
      String name = properties.getValues(ValuePropertyNames.CURVE).iterator().next();
      if (snapshot.getCurves() != null) {
        CurveKey key = new CurveKey(name);
        CurveSnapshot data = snapshot.getCurves().get(key);
        if (data != null) {
          return convertCurveMarketData(data);
        }
      }
      return null;
    }
  }
}
