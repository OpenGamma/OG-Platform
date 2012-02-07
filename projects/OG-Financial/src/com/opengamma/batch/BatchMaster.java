/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.batch;

import com.opengamma.DataNotFoundException;
import com.opengamma.batch.domain.RiskRun;
import com.opengamma.batch.rest.BatchRunSearchRequest;
import com.opengamma.id.ObjectId;
import com.opengamma.util.paging.Paging;
import com.opengamma.util.tuple.Pair;

import java.util.List;

/**
 * A master for storing and managing batch job runs.
 */
public interface BatchMaster extends BatchRunWriter {

  /**
   * The default scheme for unique identifiers.
   */
  public static final String BATCH_IDENTIFIER_SCHEME = "DbBat";


  /**
   * Searches for batches matching the specified search criteria.
   *
   * @param requestRun  the search requestRun, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the requestRun is invalid
   */
  Pair<List<RiskRun>, Paging> searchRiskRun(BatchRunSearchRequest requestRun);

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

}
