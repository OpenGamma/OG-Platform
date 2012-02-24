/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.batch;

import com.opengamma.DataNotFoundException;
import com.opengamma.batch.domain.MarketData;
import com.opengamma.batch.domain.MarketDataValue;
import com.opengamma.batch.domain.RiskRun;
import com.opengamma.batch.rest.BatchDocument;
import com.opengamma.batch.rest.BatchGetRequest;
import com.opengamma.batch.rest.BatchRunSearchRequest;
import com.opengamma.engine.view.ViewResultEntry;
import com.opengamma.id.ObjectId;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.tuple.Pair;

import java.util.List;

/**
 * A master for storing and managing batch job runs.
 */
public interface BatchMaster {

  /**
   * The default scheme for unique identifiers.
   */
  public static final String BATCH_IDENTIFIER_SCHEME = "DbBat";


  /**
   * Searches for batches matching the specified search criteria.
   *
   * @param request  the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  Pair<List<RiskRun>, Paging> searchRiskRun(BatchRunSearchRequest request);

  /**
   * Gets a batch document by unique identifier.
   * <p>
   * This returns a single batch document by unique identifier.
   * It will return all the risk data and the total count of the errors.
   * For more control, use {@link #get(BatchGetRequest)}.
   *
   * @param batchId  the unique identifier, not null
   * @return the document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  RiskRun getRiskRun(ObjectId batchId);


  /**
   * Deletes a batch and all data related to it.
   * @param batchId the uid of the batch, not null
   */
  void deleteRiskRun(ObjectId batchId);

  /**
   * Gets paged list of computed values belonging to given batch
   * @param batchId the batch values belong to
   * @param pagingRequest the paging request
   * @return list of batch values
   */
  Pair<List<ViewResultEntry>, Paging> getBatchValues(final ObjectId batchId, final PagingRequest pagingRequest);
  
  
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
}
