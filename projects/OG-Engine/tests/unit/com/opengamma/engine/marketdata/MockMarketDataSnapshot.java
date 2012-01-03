/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import javax.time.Instant;

import com.opengamma.engine.value.ValueRequirement;

/**
 * Mock {@link MarketDataSnapshot}
 */
public class MockMarketDataSnapshot extends AbstractMarketDataSnapshot {

  private final Instant _snapshotTime = Instant.now();
  private final MockMarketDataProvider _provider;
  
  public MockMarketDataSnapshot(MockMarketDataProvider provider) {
    _provider = provider;
  }
  
  @Override
  public Instant getSnapshotTimeIndication() {
    return _snapshotTime;
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
