/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

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
 * Note that the overriding snapshot can provide instances of {@link OverrideOperation} instead of (or as well as) actual values for this to return. In this case the operation is applied to the
 * underlying.
 */
public class MarketDataSnapshotWithOverride extends AbstractMarketDataSnapshot {

  private final MarketDataSnapshot _underlying;
  private final MarketDataInjectorImpl.Snapshot _override;

  public MarketDataSnapshotWithOverride(final MarketDataSnapshot underlying, final MarketDataInjectorImpl.Snapshot override) {
    _underlying = underlying;
    _override = override;
  }

  @Override
  public UniqueId getUniqueId() {
    assertInitialized();
    if (getOverride() == null) {
      // NOTE jonathan 2013-03-08 -- important for batch to have access to the real underlying snapshot ID when possible
      return getUnderlying().getUniqueId();
    }
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
    if (getOverride() != null) {
      getOverride().init();
    }
  }

  @Override
  public void init(final Set<ValueSpecification> values, final long timeout, final TimeUnit unit) {
    getUnderlying().init(values, timeout, unit);
    if (getOverride() != null) {
      getOverride().init();
    }
  }

  @Override
  public boolean isInitialized() {
    return getUnderlying().isInitialized();
  }

  @Override
  public boolean isEmpty() {
    assertInitialized();
    return getUnderlying().isEmpty() && (getOverride() == null);
  }

  @Override
  public Instant getSnapshotTime() {
    return getUnderlying().getSnapshotTime();
  }

  @Override
  public Object query(final ValueSpecification value) {
    if (getOverride() != null) {
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
      }
    }
    return getUnderlying().query(value);
  }

  @Override
  public Map<ValueSpecification, Object> query(final Set<ValueSpecification> values) {
    if (getOverride() != null) {
      final Set<ValueSpecification> unresolved = new HashSet<ValueSpecification>(values);
      final Map<ValueSpecification, Object> result = Maps.newHashMapWithExpectedSize(values.size());
      final Map<ValueSpecification, OverrideOperation> overrideOperations = Maps.newHashMapWithExpectedSize(values.size());
      for (ValueSpecification value : values) {
        Object response = getOverride().query(value);
        if (response == null) {
          continue;
        } else if (response instanceof OverrideOperation) {
          overrideOperations.put(value, (OverrideOperation) response);
        } else {
          result.put(value, response);
          unresolved.remove(value);
        }
      }
      if (!unresolved.isEmpty()) {
        final Map<ValueSpecification, Object> response = getUnderlying().query(unresolved);
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
    } else {
      return getUnderlying().query(values);
    }
  }

  private ValueRequirement getOverrideValueRequirement(final ValueSpecification subscription) {
    // TODO: Converting a value specification to a requirement like this is probably going to be wrong
    return new ValueRequirement(subscription.getValueName(), subscription.getTargetSpecification(), subscription.getProperties());
  }

  //-------------------------------------------------------------------------
  private MarketDataSnapshot getUnderlying() {
    return _underlying;
  }

  private MarketDataInjectorImpl.Snapshot getOverride() {
    return _override;
  }

}
