/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.batch;

import com.opengamma.batch.domain.MarketData;
import com.opengamma.batch.domain.MarketDataValue;
import com.opengamma.batch.domain.RiskRun;
import com.opengamma.engine.view.CycleInfo;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.tuple.Pair;

import java.util.List;
import java.util.Map;
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
   * @param cycleInfo  cycle information of the batch, not null
   * @param batchParameters batch parameters                  
   * @param runCreationMode the mode of risk run cration
   * @param snapshotMode the mode defining if missing market data should be written or expected to exist upfront
   * @return started risk run                    
   */
  RiskRun startRiskRun(CycleInfo cycleInfo, Map<String, String> batchParameters, RunCreationMode runCreationMode, SnapshotMode snapshotMode);

  /**
   * Ends the batch.
   * <p>
   * This marks the batch as complete.
   *
   * @param batchUniqueId the uid of the batch, not null
   */
  void endRiskRun(ObjectId batchUniqueId);

  /**
   * Adds calculation resuts to batch database
   * @param riskRunId the uid of the running batch, not null
   * @param result the result of computation of the batch
   */
  void addJobResults(ObjectId riskRunId, ViewComputationResultModel result);

  //-------------------------------------------------------------------------

  /**
   * Creates a market data in the database. 
   * If the market data already exists, does nothing.
   *
   * @param marketDataUid The base unique id of market data, not null
   * @return the id of creted market data.
   */
  MarketData createMarketData(UniqueId marketDataUid);

  /**
   * Adds market data fixings to an existing market data. 
   * The market data must already exist.
   *
   * @param marketDataId Id of the market data, not null
   * @param values The fixings, not null
   */
  void addValuesToMarketData(ObjectId marketDataId, Set<MarketDataValue> values);

  /**
   * Search batch data snapshots.
   *
   * @param pagingRequest the paging request, limiting number of market data returned
   * @return requested market data without actual values, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  Pair<List<MarketData>, Paging> getMarketData(PagingRequest pagingRequest);

  /**
   * Search market data by id.
   *
   * @param filter the filter, limiting number of values returned
   * @param marketDataId the id of the market data to get               
   * @return requested market data, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  MarketData getMarketDataById(ObjectId marketDataId);

  /**
   * Gets market data values of given market data
   *
   * @param marketDataId the object id of the market data
   * @param pagingRequest the paging request, limiting number of market data values returned
   * @return requested market data values, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  Pair<List<MarketDataValue>, Paging> getMarketDataValues(final ObjectId marketDataId, final PagingRequest pagingRequest);

  /**
   * Delete market data by id.
   *
   * @param marketDataId the id of the market data to delete               
   * @throws IllegalArgumentException if the request is invalid
   */
  void deleteMarketData(ObjectId marketDataId);
}
