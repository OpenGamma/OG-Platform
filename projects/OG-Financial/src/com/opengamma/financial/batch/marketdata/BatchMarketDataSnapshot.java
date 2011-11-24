/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.batch.marketdata;

import com.opengamma.engine.marketdata.InMemoryLKVMarketDataProvider;
import com.opengamma.engine.marketdata.InMemoryLKVMarketDataSnapshot;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.id.UniqueId;

import javax.time.Instant;
import java.util.Set;
import java.util.concurrent.TimeUnit;

// REVIEW jonathan 2011-06-30 -- see comment on BatchMarketDataProvider

/**
 * Implementation of {@link MarketDataSnapshot} for batch runs.
 * <p>
 * It first tries to see if the requested snapshot data is in the batch db.
 * If so, that data is returned. But if the data is not found in the batch db,
 * the provider goes to an underlying data provider (in practice, the time
 * series database), to see if the data could be found there. If so,
 * that data is copied over to the batch database and returned.
 * <p>
 * The copying of the data from the underlying data provider to the batch
 * database ensures that if the batch is restarted, it will be run
 * using the exact same market data as on the first attempt. If 
 * the data in the historical data provider is completely fixed, 
 * then this step would not strictly be necessary. 
 */
public class BatchMarketDataSnapshot implements MarketDataSnapshot {

  private final InMemoryLKVMarketDataProvider _batchDbProvider;
  private final InMemoryLKVMarketDataSnapshot _batchDbSnapshot;
  private final MarketDataSnapshot _historicalMarketDataSnapshot;
  
  public BatchMarketDataSnapshot(InMemoryLKVMarketDataProvider batchDbProvider, InMemoryLKVMarketDataSnapshot batchDbSnapshot, MarketDataSnapshot historicalMarketDataSnapshot) {
    _batchDbProvider = batchDbProvider;
    _batchDbSnapshot = batchDbSnapshot;
    _historicalMarketDataSnapshot = historicalMarketDataSnapshot;
  }

  @Override
  public UniqueId getUniqueId() {
    return UniqueId.of(MARKET_DATA_SNAPSHOT_ID_SCHEME, "BatchMarketDataSnapshot:"+getSnapshotTime());
  }
  
  @Override
  public Instant getSnapshotTimeIndication() {
    // Will never be called as time set on execution options
    return null;
  }

  @Override
  public void init() {
    _batchDbSnapshot.init();
    _historicalMarketDataSnapshot.init();
  }

  @Override
  public void init(Set<ValueRequirement> valuesRequired, long timeout, TimeUnit unit) {
    getBatchDbSnapshot().init(valuesRequired, timeout, unit);
    if (getHistoricalMarketDataSnapshot() != null) {
      getHistoricalMarketDataSnapshot().init(valuesRequired, timeout, unit);
    }
  }

  @Override
  public Instant getSnapshotTime() {
    // Will never be called as time set on execution options
    return null;
  }

  @Override
  public Object query(ValueRequirement requirement) {
    Object valueInBatchDb = getBatchDbSnapshot().query(requirement);
    if (valueInBatchDb != null) {
      return valueInBatchDb;
    }
    
    if (getHistoricalMarketDataSnapshot() == null) {
      return null;
    }
    
    Object valueInTimeSeriesDb = getHistoricalMarketDataSnapshot().query(requirement);
    if (valueInTimeSeriesDb == null) {
      return null;
    }
    
    if (!(valueInTimeSeriesDb instanceof Double)) {
      throw new IllegalStateException("Value " + valueInTimeSeriesDb + " not a double for " + requirement);
    }
    
    getBatchDbProvider().addValue(requirement, valueInTimeSeriesDb);
    
    return valueInTimeSeriesDb;
  }
  
  //-------------------------------------------------------------------------
  
  private InMemoryLKVMarketDataProvider getBatchDbProvider() {
    return _batchDbProvider;
  }
  
  public InMemoryLKVMarketDataSnapshot getBatchDbSnapshot() {
    return _batchDbSnapshot;
  }
  
  public MarketDataSnapshot getHistoricalMarketDataSnapshot() {
    return _historicalMarketDataSnapshot;
  }

}
