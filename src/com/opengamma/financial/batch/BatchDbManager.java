/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import java.util.Set;

import javax.time.calendar.OffsetTime;

import org.fudgemsg.FudgeMsg;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.financial.batch.db.BatchDbRiskContext;

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
   * Gets a context for writing risk values into the database
   * from the current VM.
   * 
   * @param batch The batch job which must already have been started
   * (see {@link #startBatch}). Not null.
   * @return A context for use in method {@link #write}. Not null.
   */
  BatchDbRiskContext createLocalContext(BatchJob batch);
  
  /**
   * Gets a context for writing risk values into the database
   * from a remote VM. Called in the Engine Master Process VM.
   * 
   * @param batch The batch job which must already have been started
   * (see {@link #startBatch}). Not null.
   * @param remoteComputeNodeOid Reference to OpenGamma Configuration Database
   * @param remoteComputeNodeVersion Reference to OpenGamma Configuration Database
   * @return A context for use in method {@link #write}. Not null.
   */
  FudgeMsg createFudgeContext(BatchJob batch, String remoteComputeNodeOid, int remoteComputeNodeVersion);
  
  /**
   * Deserializes a context sent from the Engine Master Process VM.
   * This enables the node on the grid to write risk.
   *  
   * @param msg Message created by {@link #createFudgeContext}
   * @return A context for use in method {@link #write}. Not null.
   */
  BatchDbRiskContext deserializeFudgeContext(FudgeMsg msg); 

  /**
   * Writes a set of risk values into the database.
   * 
   * @param dbContext Context information enabling the writing of risk, not null
   * @param result A set of risk values, along with metadata, not null
   */
  void write(BatchDbRiskContext dbContext, ViewComputationResultModel result);
  
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
  
}
