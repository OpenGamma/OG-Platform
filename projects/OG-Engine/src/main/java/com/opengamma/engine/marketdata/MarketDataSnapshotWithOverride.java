/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.threeten.bp.Instant;

import com.google.common.collect.Maps;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;

/**
 * Combines an underlying {@link MarketDataSnapshot} with one designed to provide overrides for certain requirements.
 * <p>
 * Note that the overriding snapshot can provide instances of {@link OverrideOperation} instead of (or as well as)
 * actual values for this to return. In this case the operation is applied to the underlying.
 */
public class MarketDataSnapshotWithOverride extends AbstractMarketDataSnapshot {

  private final MarketDataSnapshot _underlying;
  private final MarketDataSnapshot _override;

  public MarketDataSnapshotWithOverride(final MarketDataSnapshot underlying, final MarketDataSnapshot override) {
    _underlying = underlying;
    _override = override;
  }

  @Override
  public UniqueId getUniqueId() {
    // REVIEW 2013-02-04 Andrew -- This is not a good unique id, it should be allocated by whatever persists or creates these snapshots
    return UniqueId.of(MARKET_DATA_SNAPSHOT_ID_SCHEME, "MarketDataSnapshotWithOverride:" + getSnapshotTime());
  }

  @Override
  public Instant getSnapshotTimeIndication() {
    return getUnderlying().getSnapshotTimeIndication();
  }

  @Override
  public void init() {
    getUnderlying().init();
    getOverride().init();
  }

  @Override
  public void init(final Set<ValueSpecification> values, final long timeout, final TimeUnit unit) {
    getUnderlying().init(values, timeout, unit);
    getOverride().init();
  }

  @Override
  public Instant getSnapshotTime() {
    return getUnderlying().getSnapshotTime();
  }

  @Override
  public Object query(final ValueSpecification value) {
    Object result = getOverride().query(value);
    if (result != null) {
      if (result instanceof OverrideOperation) {
        final OverrideOperation operation = (OverrideOperation) result;
        result = getUnderlying().query(value);
        if (result != null) {
          return operation.apply(getOverrideValueRequirement(value), result);
        } else {
          return null;
        }
      } else {
        return result;
      }
    } else {
      return getUnderlying().query(value);
    }
  }

  @Override
  public Map<ValueSpecification, Object> query(final Set<ValueSpecification> values) {
    final Set<ValueSpecification> unqueried = new HashSet<ValueSpecification>(values);
    final Map<ValueSpecification, Object> result = Maps.newHashMapWithExpectedSize(values.size());
    Map<ValueSpecification, Object> response = getOverride().query(unqueried);
    final Map<ValueSpecification, OverrideOperation> overrideOperations;
    if (response != null) {
      overrideOperations = Maps.newHashMapWithExpectedSize(response.size());
      for (final Map.Entry<ValueSpecification, Object> overrideEntry : response.entrySet()) {
        if (overrideEntry.getValue() instanceof OverrideOperation) {
          overrideOperations.put(overrideEntry.getKey(), (OverrideOperation) overrideEntry.getValue());
        } else {
          result.put(overrideEntry.getKey(), overrideEntry.getValue());
          unqueried.remove(overrideEntry.getKey());
        }
      }
    } else {
      overrideOperations = Collections.emptyMap();
    }
    if (!unqueried.isEmpty()) {
      response = getUnderlying().query(unqueried);
      if (response != null) {
        for (final Map.Entry<ValueSpecification, Object> underlyingEntry : response.entrySet()) {
          final OverrideOperation overrideOperation = overrideOperations.get(underlyingEntry.getKey());
          if (overrideOperation != null) {
            result.put(underlyingEntry.getKey(), overrideOperation.apply(getOverrideValueRequirement(underlyingEntry.getKey()), underlyingEntry.getValue()));
          } else {
            result.put(underlyingEntry.getKey(), underlyingEntry.getValue());
          }
        }
      }
    }
    return result;
  }

  private ValueRequirement getOverrideValueRequirement(final ValueSpecification subscription) {
    // TODO: Converting a value specification to a requirement like this is probably going to be wrong
    return new ValueRequirement(subscription.getValueName(), subscription.getTargetSpecification(), subscription.getProperties());
  }

  //-------------------------------------------------------------------------
  private MarketDataSnapshot getUnderlying() {
    return _underlying;
  }

  private MarketDataSnapshot getOverride() {
    return _override;
  }

}
