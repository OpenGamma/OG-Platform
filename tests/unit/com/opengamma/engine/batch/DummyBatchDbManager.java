/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.batch;

import java.util.Collections;
import java.util.Set;

import javax.time.calendar.OffsetTime;

import org.fudgemsg.FudgeMsg;

import com.opengamma.engine.batch.db.BatchDbRiskContext;
import com.opengamma.engine.view.ViewComputationResultModel;

/**
 * 
 */
public class DummyBatchDbManager implements BatchDbManager {

  @Override
  public void startBatch(BatchJob batch) {
  }

  @Override
  public void endBatch(BatchJob batch) {
  }

  @Override
  public BatchDbRiskContext createLocalContext(BatchJob batch) {
    return null;
  }

  @Override
  public FudgeMsg createFudgeContext(BatchJob batch, String remoteComputeNodeOid, int remoteComputeNodeVersion) {
    return null;
  }

  @Override
  public BatchDbRiskContext deserializeFudgeContext(FudgeMsg msg) {
    return null;
  }

  @Override
  public void write(BatchDbRiskContext dbContext, ViewComputationResultModel result) {
  }

  @Override
  public void createLiveDataSnapshot(SnapshotId snapshotId) {
  }

  @Override
  public void fixLiveDataSnapshotTime(SnapshotId snapshotId, OffsetTime fix) {
  }

  @Override
  public void markLiveDataSnapshotComplete(SnapshotId snapshotId) {
  }

  @Override
  public void addValuesToSnapshot(SnapshotId snapshotId, Set<LiveDataValue> values) {
  }

  @Override
  public Set<LiveDataValue> getSnapshotValues(SnapshotId snapshotId) {
    return Collections.emptySet();
  }
  
}
