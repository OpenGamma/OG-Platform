/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.batch.db;

import java.util.Set;

import javax.time.Instant;
import javax.time.calendar.LocalDate;

import com.opengamma.engine.batch.BatchJob;
import com.opengamma.engine.view.ViewComputationResultModel;

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
   * Writes a set of risk values into the database.
   * 
   * @param batch The batch as part of which the risk values was calculated, not null
   * @param result A set of risk values, along with metadata, not null
   */
  void write(BatchJob batch, ViewComputationResultModel result);
  
  /**
   * Creates a LiveData snapshot in the database. 
   * If the snapshot already exists, does nothing.
   * The LiveData snapshot will be incomplete.
   * Before it is actually used, you must add entries to it
   * and then mark it complete.
   * 
   * @param observationDate The date of the snapshot, not null
   * @param observationTime The time of the snapshot (e.g., LDN_CLOSE), not null
   */
  void createLiveDataSnapshot(LocalDate observationDate, String observationTime);
  
  /**
   * Fixes the time of a LiveData snapshot in the database.
   * For example, the head trader may set the time of LDN_CLOSE every day.
   * One day he may set it at 17:31, the next at 17:44. 
   * 
   * @param observationDate The date of the snapshot, not null
   * @param observationTime The time of the snapshot (e.g., LDN_CLOSE), not null
   * @param fix The instant to which the observation time was fixed, not null
   */
  void fixLiveDataSnapshotTime(LocalDate observationDate, String observationTime, Instant fix);
  
  /**
   * Marks a LiveData snapshot complete. This releases the snapshot
   * for use in batches.
   * 
   * @param observationDate The date of the snapshot, not null
   * @param observationTime The time of the snapshot (e.g., LDN_CLOSE), not null
   */
  void markLiveDataSnapshotComplete(LocalDate observationDate, String observationTime);
  
  /**
   * Adds market data fixings to an existing LiveData snapshot. The
   * snapshot must already exist.
   * 
   * @param observationDate The date of the snapshot, not null
   * @param observationTime The time of the snapshot (e.g., LDN_CLOSE), not null
   * @param values The fixings, not null
   */
  void addValuesToSnapshot(LocalDate observationDate, 
      String observationTime,
      Set<LiveDataValue> values);
  
}
