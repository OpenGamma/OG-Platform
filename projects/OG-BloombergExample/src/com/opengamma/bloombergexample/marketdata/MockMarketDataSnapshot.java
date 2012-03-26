/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.bloombergexample.marketdata;

import javax.time.Instant;

import com.opengamma.engine.marketdata.AbstractMarketDataSnapshot;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.UniqueId;

/**
 * Mock market data snapshot implementation to allow the example server to run.
 * <p>
 * For fully-supported implementations supporting major data vendors like Bloomberg and Thomson-Reuters, please
 * contact sales@opengamma.com
 */
public class MockMarketDataSnapshot extends AbstractMarketDataSnapshot {


  @Override
  public UniqueId getUniqueId() {
    return UniqueId.of(MARKET_DATA_SNAPSHOT_ID_SCHEME, "MockMarketDataSnapshot:"+getSnapshotTime());
  }

  @Override
  public Instant getSnapshotTimeIndication() {
    MockMarketDataProviderFactoryBean.printWarning();
    return Instant.now();
  }

  @Override
  public Instant getSnapshotTime() {
    MockMarketDataProviderFactoryBean.printWarning();
    return Instant.now();
  }

  @Override
  public Object query(ValueRequirement requirement) {
    MockMarketDataProviderFactoryBean.printWarning();
    return null;
  }

}
