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
  void startBatch(BatchJobRun batch);

  /**
   * Marks the batch as complete.
   * 
   * @param batch The batch job which has finished, not null
   */
  void endBatch(BatchJobRun batch);
  
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
   * 
   * @param batch The batch job for which results will be written
   * @return A factory used to execute the batch dependency graph
   * and write results into the database.
   */
  DependencyGraphExecutorFactory<?> createDependencyGraphExecutorFactory(BatchJobRun batch);
  
  /**
   * Gets the results of a batch from the batch DB.
   * <p>
   * Risk failures are not included in the result. 
   * <p>
   * This method should not be called while the batch is still
   * in progress. If this is done, the results may be incomplete
   * and you may encounter database locking issues. 
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  BatchDataSearchResult getResults(BatchDataSearchRequest request);
  
  /**
   * Searches for batches matching the specified search criteria.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  BatchSearchResult search(BatchSearchRequest request);
  
}
