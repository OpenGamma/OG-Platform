/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.time.Instant;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.UniqueId;

/**
 * Combines an underlying {@link MarketDataSnapshot} with one designed to provide overrides for certain requirements.
 * <p>
 * Note that the overriding snapshot can provide instances of {@link OverrideOperation} instead of (or as well as)
 * actual values for this to return. In this case the operation is applied to the underlying.
 */
public class MarketDataSnapshotWithOverride implements MarketDataSnapshot {

  private final MarketDataSnapshot _underlying;
  private final MarketDataSnapshot _override;
  
  public MarketDataSnapshotWithOverride(MarketDataSnapshot underlying, MarketDataSnapshot override) {
    _underlying = underlying;
    _override = override;
  }

  @Override
  public UniqueId getUniqueId() {
    return UniqueId.of(MARKET_DATA_SNAPSHOT_ID_SCHEME, "MarketDataSnapshotWithOverride:"+getSnapshotTime());
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
  
  //-------------------------------------------------------------------------
  private MarketDataSnapshot getUnderlying() {
    return _underlying;
  }
  
  private MarketDataSnapshot getOverride() {
    return _override;
  }

}
