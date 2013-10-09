/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.historical;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.threeten.bp.Duration;
import org.threeten.bp.Instant;
import org.threeten.bp.temporal.ChronoUnit;
import org.threeten.bp.temporal.TemporalUnit;

import com.google.common.collect.Maps;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.OpenGammaClock;

/**
 * Snapshot composed of 3 underlying snapshots. This snapshot's values are derived by finding the difference between
 * values in the first two snapshots and applying it to the value from the third snapshot. The change applied to the
 * base value can be the proportional or absolute difference between the two other values.
 */
public class HistoricalShockMarketDataSnapshot implements MarketDataSnapshot {

  /**
   * The type of transformation to apply to the base value.
   */
  public enum ShockType {

    /**
     * The shocked value is the base value * value2 / value1.
     */
    PROPORTIONAL {
      @Override
      public Double shock(Double value1, Double value2, Double baseValue) {
        return baseValue * value2 / value1;
      }
    },
    /**
     * The shocked value is the base value + value2 - value1.
     */
    ABSOLUTE {
      @Override
      public Double shock(Double value1, Double value2, Double baseValue) {
        return baseValue + value2 - value1;
      }
    };

    /**
     *
     * @param value1 The first historical value
     * @param value2 The second historical value
     * @param baseValue The base value
     * @return The base value shocked by the difference between the two historical values
     */
    public abstract Double shock(Double value1, Double value2, Double baseValue);
  }

  private final ShockType _shockType;
  private final MarketDataSnapshot _historicalSnapshot1;
  private final MarketDataSnapshot _historicalSnapshot2;
  private final MarketDataSnapshot _baseSnapshot;

  public HistoricalShockMarketDataSnapshot(ShockType shockType,
                                           MarketDataSnapshot historicalSnapshot1,
                                           MarketDataSnapshot historicalSnapshot2,
                                           MarketDataSnapshot baseSnapshot) {
    ArgumentChecker.notNull(historicalSnapshot1, "historicalSnapshot1");
    ArgumentChecker.notNull(historicalSnapshot2, "historicalSnapshot2");
    ArgumentChecker.notNull(baseSnapshot, "baseSnapshot");
    _shockType = shockType;
    _historicalSnapshot1 = historicalSnapshot1;
    _historicalSnapshot2 = historicalSnapshot2;
    _baseSnapshot = baseSnapshot;
  }

  @Override
  public UniqueId getUniqueId() {
    // TODO this is nasty, see PLAT-4292
    return UniqueId.of(MARKET_DATA_SNAPSHOT_ID_SCHEME, "HistoricalShockMarketDataSnapshot:" + getSnapshotTime());
  }

  @Override
  public Instant getSnapshotTimeIndication() {
    return _baseSnapshot.getSnapshotTimeIndication();
  }

  @Override
  public void init() {
    _historicalSnapshot1.init();
    _historicalSnapshot2.init();
    _baseSnapshot.init();
  }

  @Override
  public void init(Set<ValueSpecification> values, long timeout, TimeUnit ucUnit) {
    Instant start = OpenGammaClock.getInstance().instant();
    TemporalUnit unit = convertUnit(ucUnit);
    Duration remaining = Duration.of(timeout, unit);

    _historicalSnapshot1.init(values, timeout, ucUnit);
    Instant after1 = OpenGammaClock.getInstance().instant();
    Duration duration1 = Duration.between(start, after1);
    remaining = remaining.minus(duration1);
    if (remaining.isNegative()) {
      return;
    }
    _historicalSnapshot2.init(values, remaining.get(unit), ucUnit);
    Instant after2 = OpenGammaClock.getInstance().instant();
    Duration duration2 = Duration.between(after1, after2);
    remaining = remaining.minus(duration2);
    if (remaining.isNegative()) {
      return;
    }
    _baseSnapshot.init(values, remaining.get(unit), ucUnit);
  }

  @Override
  public boolean isInitialized() {
    return _baseSnapshot.isInitialized();
  }

  @Override
  public boolean isEmpty() {
    return _historicalSnapshot1.isEmpty() || _historicalSnapshot2.isEmpty() || _baseSnapshot.isEmpty();
  }

  @Override
  public Instant getSnapshotTime() {
    return _baseSnapshot.getSnapshotTime();
  }

  @Override
  public Object query(ValueSpecification specification) {
    Object value1 = _historicalSnapshot1.query(specification);
    Object value2 = _historicalSnapshot2.query(specification);
    Object baseValue = _baseSnapshot.query(specification);
    if (!(value1 instanceof Double) || !(value2 instanceof Double) || !(baseValue instanceof Double)) {
      return baseValue;
    }
    return _shockType.shock((Double) value1, (Double) value2, (Double) baseValue);
  }

  @Override
  public Map<ValueSpecification, Object> query(Set<ValueSpecification> specifications) {
    Map<ValueSpecification, Object> values = Maps.newHashMapWithExpectedSize(specifications.size());
    for (ValueSpecification specification : specifications) {
      values.put(specification, query(specification));
    }
    return values;
  }

  private static TemporalUnit convertUnit(TimeUnit unit) {
    switch (unit) {
      case NANOSECONDS:
        return ChronoUnit.NANOS;
      case MICROSECONDS:
        return ChronoUnit.MICROS;
      case MILLISECONDS:
        return ChronoUnit.MILLIS;
      case SECONDS:
        return ChronoUnit.SECONDS;
      case MINUTES:
        return ChronoUnit.MINUTES;
      case HOURS:
        return ChronoUnit.HOURS;
      case DAYS:
        return ChronoUnit.DAYS;
      default:
        throw new IllegalArgumentException("Unexpected unit " + unit);
    }
  }
}
