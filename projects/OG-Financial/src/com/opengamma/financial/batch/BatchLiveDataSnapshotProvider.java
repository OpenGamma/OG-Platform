/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.livedata.HistoricalLiveDataSnapshotProvider;
import com.opengamma.engine.livedata.InMemoryLKVSnapshotProvider;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.ArgumentChecker;

/**
 * This {@code LiveDataSnapshotProvider} is used for batch risk.
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
public class BatchLiveDataSnapshotProvider extends InMemoryLKVSnapshotProvider {

  /**
   * The run for which snapshots are being provided.
   */
  private final BatchJobRun _run;
  /**
   * The batch master.
   */
  private final BatchRunMaster _batchRunMaster;
  /**
   * The provider of historical data.
   * In practice, this is the time series database.
   */
  private final HistoricalLiveDataSnapshotProvider _snapshotProvider;

  /**
   * Creates an instance.
   * 
   * @param run  the run data, not null
   * @param batchRunMaster  the batch master, not null
   * @param snapshotProvider  the historical snapshot provider, not null
   */
  public BatchLiveDataSnapshotProvider(
      BatchJobRun run,
      BatchRunMaster batchRunMaster,
      HistoricalLiveDataSnapshotProvider snapshotProvider) {
    ArgumentChecker.notNull(run, "run");
    ArgumentChecker.notNull(batchRunMaster, "batchMaster");
    ArgumentChecker.notNull(snapshotProvider, "snapshotProvider");
    _run = run;
    _batchRunMaster = batchRunMaster;
    _snapshotProvider = snapshotProvider;
  }

  //-------------------------------------------------------------------------
  // method is synchronized for now because of the call to .addValuesToSnapshot() which
  // you don't want to be doing multiple times. Could synchronize just on snapshot+requirement 
  // combo
  @Override
  public synchronized Object querySnapshot(long snapshot, ValueRequirement requirement) {
    Object valueInBatchDb = super.querySnapshot(snapshot, requirement);
    if (valueInBatchDb != null) {
      return valueInBatchDb;
    }
    
    Object valueInTimeSeriesDb = _snapshotProvider.querySnapshot(snapshot, requirement);
    if (valueInTimeSeriesDb == null) {
      return null;
    }
    
    if (!(valueInTimeSeriesDb instanceof Double)) {
      throw new IllegalStateException("Value " + valueInTimeSeriesDb + " not a double for " + snapshot + "/" + requirement);
    }
    
    Double value = (Double) valueInTimeSeriesDb;
    LiveDataValue liveDataValue = new LiveDataValue(requirement, value);
    Set<LiveDataValue> liveDataValues = Collections.singleton(liveDataValue);
    _batchRunMaster.addValuesToSnapshot(_run.getSnapshotId(), liveDataValues);
    
    addValue(requirement, valueInTimeSeriesDb);
    
    return valueInTimeSeriesDb;
  }

  @Override
  public boolean isAvailable(ValueRequirement requirement) {
    if (super.isAvailable(requirement)) {
      return true;
    }
    return _snapshotProvider.isAvailable(requirement);
  }

}
