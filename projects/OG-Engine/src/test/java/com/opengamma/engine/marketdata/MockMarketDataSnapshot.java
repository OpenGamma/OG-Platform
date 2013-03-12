/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import org.threeten.bp.Instant;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;

/**
 * Mock {@link MarketDataSnapshot}
 */
public class MockMarketDataSnapshot extends AbstractMarketDataSnapshot {

  private final Instant _snapshotTime = Instant.now();
  private final MockMarketDataProvider _provider;

  public MockMarketDataSnapshot(final MockMarketDataProvider provider) {
    _provider = provider;
  }

  @Override
  public UniqueId getUniqueId() {
    return UniqueId.of(MARKET_DATA_SNAPSHOT_ID_SCHEME, "MockMarketDataSnapshot:" + getSnapshotTime());
  }

  @Override
  public boolean isInitialized() {
    return true;
  }

  @Override
  public boolean isEmpty() {
    return false;
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
  public Object query(final ValueSpecification specification) {
    _provider.incrementQueryCount();
    return _provider.getValue(specification);
  }

}
