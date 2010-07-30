/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import java.util.Collections;
import java.util.Set;

import javax.time.calendar.OffsetTime;

import org.fudgemsg.FudgeMsg;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.calcnode.ResultWriterFactory;
import com.opengamma.financial.batch.db.BatchDbRiskContext;

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

  @Override
  public ResultWriterFactory createResultWriterFactory(BatchJob batch) {
    return null;
  }
  
}
