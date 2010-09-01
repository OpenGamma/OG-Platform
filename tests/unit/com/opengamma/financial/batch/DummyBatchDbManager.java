/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import java.util.Collections;
import java.util.Set;

import javax.time.calendar.OffsetTime;

import com.opengamma.engine.view.calc.DependencyGraphExecutorFactory;
import com.opengamma.engine.view.calc.SingleNodeExecutorFactory;

/**
 * 
 */
public class DummyBatchDbManager implements BatchDbManager {

  @Override
  public void startBatch(BatchJobRun batch) {
  }

  @Override
  public void endBatch(BatchJobRun batch) {
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
  public DependencyGraphExecutorFactory<?> createDependencyGraphExecutorFactory(BatchJobRun batch) {
    return new SingleNodeExecutorFactory();
  }

}
