/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.id.UniqueId;

import java.util.Set;

/**
 * A master for storing batch job runs.
 * <p>
 * This extends {@code BatchMaster} to provide tools to store batches.
 */
public interface BatchRunWriter {

  /**
   * Starts the storage of a batch.
   * <p>
   * This creates all static data structures. 
   * This method must be called before any risk can be
   * written for the batch in question.
   *
   * @param batch  the batch which is starting, not null
   * @param runCreationMode the mode of risk run cration
   * @param snapshotMode the mode defining if missing market data should be written or expected to exist upfront 
   */
  void startBatch(Batch batch, RunCreationMode runCreationMode, SnapshotMode snapshotMode);

  /**
   * Ends the batch.
   * <p>
   * This marks the batch as complete.
   * 
   * @param batchUniqueId the uid of the batch, not null
   */
  void endBatch(UniqueId batchUniqueId);

  /**
   * Deletes a batch and all data retated to it.
   * @param batchUniqueId the uid of the batch, not null
   */
  void deleteBatch(UniqueId batchUniqueId);

  /**
   * Adds calculation resuts to batch database
   * @param batchUniqueId the uid of the running batch, not null
   * @param result the result of computation of the batch
   */
  void addJobResults(UniqueId batchUniqueId, ViewComputationResultModel result);

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
}
