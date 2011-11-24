/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.CycleInfo;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.financial.batch.BatchId;
import com.opengamma.financial.batch.RunCreationMode;
import com.opengamma.id.*;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.functional.Function1;

import javax.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.opengamma.util.functional.Functional.map;

public class Batch {

  final private BatchId _batchId;

  final private CycleInfo _cycleInfo;

  final private RunCreationMode _runCreationMode;

  private Map<String, String> _parametersMap;

  private Instant _originalCreationTime;

  private DbBatchMaster.DbHandle _dbHandle;

  private SnapshotMode _snapshotMode;



  public Batch(BatchId batchId, CycleInfo cycleInfo) {
    _batchId = batchId;
    _cycleInfo = cycleInfo;

    //TODO implement the mechanism of passing that as config option
    _snapshotMode = SnapshotMode.PREPARED;
    //TODO implement the mechanism of passing that as config option
    _runCreationMode = RunCreationMode.CREATE_NEW_OVERWRITE;

  }

  public BatchId getId() {
    return _batchId;
  }

  public CycleInfo getCycleInfo() {
    return _cycleInfo;
  }

  public RunCreationMode getRunCreationMode() {
    return _runCreationMode;
  }

  public Map<String, String> getParametersMap() {
    return _parametersMap;
  }

  public void setParametersMap(Map<String, String> parametersMap) {
    _parametersMap = parametersMap;
  }

  public Instant getOriginalCreationTime() {
    return _originalCreationTime;
  }

  public void setOriginalCreationTime(Instant originalCreationTime) {
    _originalCreationTime = originalCreationTime;
  }

  public DbBatchMaster.DbHandle getDbHandle() {
    return _dbHandle;
  }

  public void setDbHandle(DbBatchMaster.DbHandle dbHandle) {
    _dbHandle = dbHandle;
  }

  public SnapshotMode getSnapshotMode() {
    return _snapshotMode;
  }

  public void setSnapshotMode(SnapshotMode snapshotMode) {
    _snapshotMode = snapshotMode;
  }

  /**
   * Represents snapshot data mode
   */
  enum SnapshotMode {
    /**
     * The snapshot data is expected to be populated upfront batch run.
     */
    PREPARED,
    /**
     * The snapshot data is no prepared upfront the batch run, and it should be written to DB on the fly
     */
    WRITE_TROUGH
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Batch batch = (Batch) o;
    return _batchId.equals(batch._batchId);
  }

  @Override
  public int hashCode() {
    return _batchId.hashCode();
  }

  @Override
  public String toString() {
    return "Batch{" + _batchId + '}';
  }
}
