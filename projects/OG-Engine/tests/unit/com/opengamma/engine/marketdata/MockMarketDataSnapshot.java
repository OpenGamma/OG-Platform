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
 * Mock {@link MarketDataSnapshot}
 */
public class MockMarketDataSnapshot implements MarketDataSnapshot {

  private final Instant _snapshotTime = Instant.now();
  private final MockMarketDataProvider _provider;
  
  public MockMarketDataSnapshot(MockMarketDataProvider provider) {
    _provider = provider;
  }

  @Override
  public UniqueId getUniqueId() {
    return UniqueId.of(MARKET_DATA_SNAPSHOT_ID_SCHEME, "MockMarketDataSnapshot:"+getSnapshotTime());
  }
  
  @Override
  public Instant getSnapshotTimeIndication() {
    return _snapshotTime;
  }

  @Override
  public void init() {
  }
  
  @Override
  public void init(Set<ValueRequirement> valuesRequired, long timeout, TimeUnit unit) {
  }

  @Override
  public Instant getSnapshotTime() {
    return _snapshotTime;
  }

  @Override
  public Object query(ValueRequirement requirement) {
    _provider.incrementQueryCount();
    return _provider.getValue(requirement);
  }

}
