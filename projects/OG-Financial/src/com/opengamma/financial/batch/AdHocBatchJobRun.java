/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.Instant;
import javax.time.calendar.LocalDate;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.view.calc.SingleComputationCycle;
import com.opengamma.util.VersionUtil;

/**
 * A batch run where results already exist in memory, but
 * are now to be saved in the database.
 * <p>
 * A typical scenario is that a user plays with Excel, 
 * sets up a view, prices it, and then likes the results
 * enough that they want to save them in the batch DB.
 */
public class AdHocBatchJobRun extends BatchJobRun {
  
  /**
   * The result that already exists in memory
   */
  private final SingleComputationCycle _cycle;
  
  /**
   * What snapshot to use
   */
  private SnapshotId _snapshotId;
  
  /**
   * When this batch run was created
   */
  private final Instant _creationTime;
  
  // --------------------------------------------------------------------------
  
  public AdHocBatchJobRun(
      SingleComputationCycle cycle,
      BatchId batchId) {

    super(batchId);
    
    _cycle = cycle;
    _creationTime = Instant.now();
    
    setView(cycle.getView());
  }
  
  // --------------------------------------------------------------------------
  
  @Override
  public SnapshotId getSnapshotId() {
    if (_snapshotId == null) {
      throw new IllegalStateException("Snapshot ID not set");
    }
    return _snapshotId;
  }

  public void setSnapshotId(SnapshotId snapshotId) {
    _snapshotId = snapshotId;
  }

  public void saveSnapshot(BatchDbManager dbManager) {
    dbManager.createLiveDataSnapshot(getSnapshotId());
    
    Set<LiveDataValue> values = new HashSet<LiveDataValue>();
    for (ComputedValue liveData : _cycle.getAllLiveData()) {
      values.add(new LiveDataValue(liveData));      
    }
    
    dbManager.addValuesToSnapshot(getSnapshotId(), values);
  }
  
  // --------------------------------------------------------------------------
  
  public void saveResult(BatchDbManager dbManager) {
    
    
  }
  
  // --------------------------------------------------------------------------
  

  @Override
  public String getRunReason() {
    return "Ad hoc run";
  }

  @Override
  public Instant getValuationTime() {
    return _cycle.getValuationTime();
  }

  @Override
  public RunCreationMode getRunCreationMode() {
    return RunCreationMode.ALWAYS;
  }

  @Override
  public String getOpenGammaVersion() {
    return VersionUtil.getVersion("og-financial");
  }

  @Override
  public Instant getCreationTime() {
    return _creationTime;
  }

  @Override
  public boolean isFailed() {
    // result already available -> cannot have failed
    return false;
  }

  @Override
  public Map<String, String> getJobLevelParameters() {
    return Collections.emptyMap();
  }

}
