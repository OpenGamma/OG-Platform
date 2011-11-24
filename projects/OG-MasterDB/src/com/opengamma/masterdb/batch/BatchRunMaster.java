/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.financial.batch.BatchMaster;
import com.opengamma.financial.batch.LiveDataValue;
import com.opengamma.id.UniqueId;

import java.util.Set;

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
  void startBatch(Batch batch);

  /**
   * Ends the storage of a batch job run.
   * <p>
   * This marks the batch as complete.
   * 
   * @param batch  the batch job which has finished, not null
   */
  void endBatch(Batch batch);

  //-------------------------------------------------------------------------
  /**
   * Creates a LiveData snapshot in the database. 
   * If the snapshot already exists, does nothing.
   * 
   * @param snapshotId The market data specification of the snapshot, not null
   * @return the snapshot
   */
  LiveDataSnapshot createLiveDataSnapshot(UniqueId snapshotId);

  /**
   * Craetes a version correction in the database.
   * If the version correction already exists, does nothing.
   *
   * @param versionCorrection The version correction, not null
   * @return the databse version correction
   */
  VersionCorrection createVersionCorrection(com.opengamma.id.VersionCorrection versionCorrection);

  /**
   * Fixes the time of a LiveData snapshot in the database.
   * For example, the head trader may set the time of LDN_CLOSE every day.
   * One day he may set it at 17:31, the next at 17:44. 
   * 
   * @param snapshotId The date and time of the snapshot, not null
   * @param fix The time to which the observation time was fixed, not null
   */
  //TODO enableit?
  //void fixLiveDataSnapshotTime(UniqueId marketDataSnapshotUniqueId, OffsetTime fix);

  /**
   * Adds market data fixings to an existing LiveData snapshot. The
   * snapshot must already exist.
   * 
   * @param marketDataSnapshotUniqueId Unique id of the snapshot, not null
   * @param values The fixings, not null
   */
  void addValuesToSnapshot(UniqueId marketDataSnapshotUniqueId, Set<LiveDataValue> values);

  /**
   * Gets all market data fixings associated with an existing snapshot.
   * 
   * @param snapshotUniqueId Unique id of the snapshot, not null
   * @return The fixings associated with this snapshot, not null
   * @throws IllegalArgumentException If a snapshot with the given
   * ID does not exist
   */
  Set<LiveDataValue> getSnapshotValues(UniqueId snapshotUniqueId);

  void addJobResults(Batch batch, ViewResultModel result);

  RiskRun createRiskRun(Batch batch);

  /**
   * Ends the batch.
   * @param batch the batch to end.
   */
  void end(Batch batch);
}
