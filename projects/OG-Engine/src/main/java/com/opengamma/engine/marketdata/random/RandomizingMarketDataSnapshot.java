/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.random;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.threeten.bp.Instant;

import com.google.common.collect.Maps;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.spec.RandomizingMarketDataSpecification;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.OpenGammaClock;

/**
 *
 */
public class RandomizingMarketDataSnapshot implements MarketDataSnapshot {

  private final MarketDataSnapshot _underlying;
  private final ConcurrentMap<ValueSpecification, Double> _values = Maps.newConcurrentMap();
  private final RandomizingMarketDataSpecification _spec;
  private final Set<ValueSpecification> _specsToRandomize;

  private volatile Instant _snapshotTime;

  /* package */ RandomizingMarketDataSnapshot(MarketDataSnapshot underlying,
                                              RandomizingMarketDataSpecification spec,
                                              Set<ValueSpecification> specsToRandomize) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(spec, "spec");
    ArgumentChecker.notNull(specsToRandomize, "specsToRandomize");
    _specsToRandomize = specsToRandomize;
    _spec = spec;
    _underlying = underlying;
  }

  @Override
  public UniqueId getUniqueId() {
    // TODO this is nasty, see PLAT-4292
    return UniqueId.of(MARKET_DATA_SNAPSHOT_ID_SCHEME, "RandomizingMarketDataSnapshot:" + getSnapshotTime());
  }

  @Override
  public Instant getSnapshotTimeIndication() {
    return _snapshotTime;
  }

  @Override
  public void init() {
    _underlying.init();
    _snapshotTime = OpenGammaClock.getInstance().instant();
  }

  @Override
  public void init(Set<ValueSpecification> values, long timeout, TimeUnit unit) {
    _underlying.init(values, timeout, unit);
    _snapshotTime = OpenGammaClock.getInstance().instant();
  }

  @Override
  public boolean isInitialized() {
    return _underlying.isInitialized();
  }

  @Override
  public boolean isEmpty() {
    return _underlying.isEmpty();
  }

  @Override
  public Instant getSnapshotTime() {
    return _snapshotTime;
  }

  @Override
  public Object query(ValueSpecification specification) {
    Double cachedValue = _values.get(specification);
    if (cachedValue != null) {
      return cachedValue;
    }
    Object underlyingValue = _underlying.query(specification);
    if (underlyingValue instanceof Double) {
      double value;
      if (_specsToRandomize.contains(specification)) {
        value = randomize((Double) underlyingValue);
      } else {
        value = (double) underlyingValue;
      }
      return _values.putIfAbsent(specification, randomize(value));
    } else {
      return underlyingValue;
    }
  }

  @Override
  public Map<ValueSpecification, Object> query(Set<ValueSpecification> specifications) {
    Map<ValueSpecification, Object> underlyingValues = _underlying.query(specifications);
    Map<ValueSpecification, Object> values = Maps.newHashMap();
    for (Map.Entry<ValueSpecification, Object> entry : underlyingValues.entrySet()) {
      ValueSpecification specification = entry.getKey();
      Object underlyingValue = entry.getValue();
      Double cachedValue = _values.get(specification);
      if (cachedValue != null) {
        values.put(specification, cachedValue);
      } else {
        if (underlyingValue instanceof Double) {
          double value;
          if (_specsToRandomize.contains(specification)) {
            value = randomize((Double) underlyingValue);
          } else {
            value = (double) underlyingValue;
          }
          values.put(specification, _values.putIfAbsent(specification, randomize(value)));
        } else {
          values.put(specification, underlyingValue);
        }
      }

    }
    return values;
  }

  private Double randomize(Double value) {
    double signum = (Math.random() < 0.5) ? -1 : 1;
    return value * (1 + signum * Math.random() * (double) _spec.getMaxPercentageChange() / 100d);
  }

  /* package */ Set<ValueSpecification> getSpecifications() {
    return _values.keySet();
  }
}
