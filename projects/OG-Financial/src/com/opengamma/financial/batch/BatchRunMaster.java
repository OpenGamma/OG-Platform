/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import java.util.Set;

import javax.time.calendar.OffsetTime;

import com.opengamma.engine.view.calc.DependencyGraphExecutorFactory;

/**
 * A master for storing batch job runs.
 * <p>
 * This extends {@code BatchMaster} to provide tools to store batches.
 */
public interface BatchRunMaster extends BatchMaster {

  /**
   * Starts the storage of a batch job run.
   * <p>
   * This creates all static data structures, such as compute host, OpenGamma
   * version and risk run. This method must be called before any risk can be
   * written for the batch in question.
   * 
   * @param batch  the batch job which is starting, not null
   */
  void startBatch(BatchJobRun batch);

  /**
   * Ends the storage of a batch job run.
   * <p>
   * This marks the batch as complete.
   * 
   * @param batch  the batch job which has finished, not null
   */
  void endBatch(BatchJobRun batch);

  //-------------------------------------------------------------------------
  /**
   * Creates a LiveData snapshot in the database. 
   * If the snapshot already exists, does nothing.
   * 
   * @param snapshotId The date and time of the snapshot, not null
   */
  void createLiveDataSnapshot(SnapshotId snapshotId);

  /**
   * Fixes the time of a LiveData snapshot in the database.
   * For example, the head trader may set the time of LDN_CLOSE every day.
   * One day he may set it at 17:31, the next at 17:44. 
   * 
   * @param snapshotId The date and time of the snapshot, not null
   * @param fix The time to which the observation time was fixed, not null
   */
  void fixLiveDataSnapshotTime(SnapshotId snapshotId, OffsetTime fix);

  /**
   * Adds market data fixings to an existing LiveData snapshot. The
   * snapshot must already exist.
   * 
   * @param snapshotId The date and time of the snapshot, not null
   * @param values The fixings, not null
   */
  void addValuesToSnapshot(SnapshotId snapshotId, Set<LiveDataValue> values);

  /**
   * Gets all market data fixings associated with an existing snapshot.
   * 
   * @param snapshotId The date and time of the snapshot, not null
   * @return The fixings associated with this snapshot, not null
   * @throws IllegalArgumentException If a snapshot with the given
   * ID does not exist
   */
  Set<LiveDataValue> getSnapshotValues(SnapshotId snapshotId);

  /**
   * Gets a factory for executing dependency graphs and
   * writing risk values into the database.
   * <p>
   * The risk is written into the database incrementally,
   * as the dependency graph is being executed. This
   * is useful if you do not already have the results in
   * memory.
   * 
   * @param batch The batch job for which results will be written
   * @return A factory used to execute the batch dependency graph
   * and write results into the database.
   */
  DependencyGraphExecutorFactory<?> createDependencyGraphExecutorFactory(BatchJobRun batch);

}
