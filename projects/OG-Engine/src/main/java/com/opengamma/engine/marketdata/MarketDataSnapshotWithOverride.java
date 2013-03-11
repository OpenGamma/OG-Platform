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
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
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
    assertInitialized();
    if (_override.isEmpty()) {
      // NOTE jonathan 2013-03-08 -- important for batch to have access to the real underlying snapshot ID when possible
      return _underlying.getUniqueId();
    }
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
  public void init(final Set<ValueRequirement> valuesRequired, final long timeout, final TimeUnit unit) {
    getUnderlying().init(valuesRequired, timeout, unit);
    getOverride().init();
  }
  
  @Override
  public boolean isInitialized() {
    return getUnderlying().isInitialized() && getOverride().isInitialized();
  }
  
  @Override
  public boolean isEmpty() {
    assertInitialized();
    return getUnderlying().isEmpty() && getOverride().isEmpty();
  }

  @Override
  public Instant getSnapshotTime() {
    return getUnderlying().getSnapshotTime();
  }

  @Override
  public ComputedValue query(final ValueRequirement requirement) {
    ComputedValue result = getOverride().query(requirement);
    if (result != null) {
      if (result.getValue() instanceof OverrideOperation) {
        final OverrideOperation operation = (OverrideOperation) result.getValue();
        result = getUnderlying().query(requirement);
        if (result != null) {
          return new ComputedValue(result.getSpecification(), operation.apply(requirement, result.getValue()));
        } else {
          return null;
        }
      } else {
        return result;
      }
    } else {
      return getUnderlying().query(requirement);
    }
  }

  @Override
  public Map<ValueRequirement, ComputedValue> query(final Set<ValueRequirement> requirements) {
    final Set<ValueRequirement> unqueried = new HashSet<ValueRequirement>(requirements);
    final Map<ValueRequirement, ComputedValue> result = Maps.newHashMapWithExpectedSize(requirements.size());
    Map<ValueRequirement, ComputedValue> response = getOverride().query(unqueried);
    final Map<ValueRequirement, OverrideOperation> overrideOperations;
    if (response != null) {
      overrideOperations = Maps.newHashMapWithExpectedSize(response.size());
      for (final Map.Entry<ValueRequirement, ComputedValue> overrideEntry : response.entrySet()) {
        if (overrideEntry.getValue().getValue() instanceof OverrideOperation) {
          overrideOperations.put(overrideEntry.getKey(), (OverrideOperation) overrideEntry.getValue().getValue());
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
        for (final Map.Entry<ValueRequirement, ComputedValue> underlyingEntry : response.entrySet()) {
          final OverrideOperation overrideOperation = overrideOperations.get(underlyingEntry.getKey());
          if (overrideOperation != null) {
            result.put(underlyingEntry.getKey(),
                new ComputedValue(underlyingEntry.getValue().getSpecification(), overrideOperation.apply(underlyingEntry.getKey(), underlyingEntry.getValue().getValue())));
          } else {
            result.put(underlyingEntry.getKey(), underlyingEntry.getValue());
          }
        }
      }
    }
    return result;
  }

  //-------------------------------------------------------------------------
  private MarketDataSnapshot getUnderlying() {
    return _underlying;
  }

  private MarketDataSnapshot getOverride() {
    return _override;
  }

}
