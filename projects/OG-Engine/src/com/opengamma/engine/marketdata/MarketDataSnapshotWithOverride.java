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

import javax.time.Instant;

import com.google.common.collect.Maps;
import com.opengamma.engine.value.ValueRequirement;

/**
 * Combines an underlying {@link MarketDataSnapshot} with one designed to provide overrides for certain requirements.
 * <p>
 * Note that the overriding snapshot can provide instances of {@link OverrideOperation} instead of (or as well as)
 * actual values for this to return. In this case the operation is applied to the underlying.
 */
public class MarketDataSnapshotWithOverride extends AbstractMarketDataSnapshot {

  private final MarketDataSnapshot _underlying;
  private final MarketDataSnapshot _override;
  
  public MarketDataSnapshotWithOverride(MarketDataSnapshot underlying, MarketDataSnapshot override) {
    _underlying = underlying;
    _override = override;
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
  public void init(Set<ValueRequirement> valuesRequired, long timeout, TimeUnit unit) {
    getUnderlying().init(valuesRequired, timeout, unit);
    getOverride().init();
  }

  @Override
  public Instant getSnapshotTime() {
    return getUnderlying().getSnapshotTime();
  }

  @Override
  public Object query(ValueRequirement requirement) {
    Object result = getOverride().query(requirement);
    if (result != null) {
      if (result instanceof OverrideOperation) {
        final OverrideOperation operation = (OverrideOperation) result;
        result = getUnderlying().query(requirement);
        if (result != null) {
          return operation.apply(requirement, result);
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
  public Map<ValueRequirement, Object> query(final Set<ValueRequirement> requirements) {
    final Set<ValueRequirement> unqueried = new HashSet<ValueRequirement>(requirements);
    final Map<ValueRequirement, Object> result = Maps.newHashMapWithExpectedSize(requirements.size());
    Map<ValueRequirement, Object> response = getOverride().query(unqueried);
    final Map<ValueRequirement, OverrideOperation> overrideOperations;
    if (response != null) {
      overrideOperations = Maps.newHashMapWithExpectedSize(response.size());
      for (Map.Entry<ValueRequirement, Object> overrideEntry : response.entrySet()) {
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
        for (Map.Entry<ValueRequirement, Object> underlyingEntry : response.entrySet()) {
          final OverrideOperation overrideOperation = overrideOperations.get(underlyingEntry.getKey());
          if (overrideOperation != null) {
            result.put(underlyingEntry.getKey(), overrideOperation.apply(underlyingEntry.getKey(), underlyingEntry.getValue()));
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
