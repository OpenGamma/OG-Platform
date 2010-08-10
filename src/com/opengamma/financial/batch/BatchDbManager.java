/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import java.util.Set;

import javax.time.calendar.OffsetTime;

import com.opengamma.engine.view.calcnode.ResultWriterFactory;

/**
 * All operations needed to populate the batch database.
 */
public interface BatchDbManager {

  /**
   * Creates all static data structures
   * in the batch database (compute host,
   * OpenGamma version, risk run, etc.). This method 
   * must be called before any risk can be written into 
   * the database for the batch in question.
   * 
   * @param batch The batch job which is starting, not null
   */
  void startBatch(BatchJob batch);

  /**
   * Marks the batch as complete.
   * 
   * @param batch The batch job which has finished, not null
   */
  void endBatch(BatchJob batch);
  
  /**
   * Creates a LiveData snapshot in the database. 
   * If the snapshot already exists, does nothing.
   * The LiveData snapshot will be incomplete.
   * Before it is actually used, you must add entries to it
   * and then mark it complete.
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
   * Marks a LiveData snapshot complete. This releases the snapshot
   * for use in batches.
   * 
   * @param snapshotId The date and time of the snapshot, not null
   */
  void markLiveDataSnapshotComplete(SnapshotId snapshotId);
  
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
   */
  Set<LiveDataValue> getSnapshotValues(SnapshotId snapshotId);
  
  /**
   * Gets a factory for writing risk values into the database.
   * 
   * @param batch The batch job for which results will be written
   * @return A factory used to create result writers. These
   * result writers will in turn be sent down to compute nodes
   * on the grid.
   */
  ResultWriterFactory createResultWriterFactory(BatchJob batch);
  
}
