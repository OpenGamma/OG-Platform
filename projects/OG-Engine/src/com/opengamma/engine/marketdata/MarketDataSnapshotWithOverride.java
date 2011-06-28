/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.time.Instant;

import com.opengamma.core.marketdatasnapshot.StructuredMarketDataKey;
import com.opengamma.engine.value.ValueRequirement;

/**
 * Combines an underlying {@link MarketDataSnapshot} with one designed to provide overrides for certain requirements.
 */
public class MarketDataSnapshotWithOverride implements MarketDataSnapshot {

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
  public boolean hasStructuredData() {
    return getUnderlying().hasStructuredData() || getOverride().hasStructuredData();
  }

  @Override
  public Object query(ValueRequirement requirement) {
    Object result = getOverride().query(requirement);
    if (result != null) {
      return result;
    }
    return getUnderlying().query(requirement);
  }

  @Override
  public Object query(StructuredMarketDataKey marketDataKey) {
    Object result = getOverride().query(marketDataKey);
    if (result != null) {
      return result;
    }
    return getUnderlying().query(marketDataKey);
  }
  
  //-------------------------------------------------------------------------
  private MarketDataSnapshot getUnderlying() {
    return _underlying;
  }
  
  private MarketDataSnapshot getOverride() {
    return _override;
  }

}
