/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import java.util.Collections;
import java.util.Set;

import javax.time.calendar.OffsetTime;

import com.opengamma.engine.view.calcnode.ResultWriterFactory;

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
