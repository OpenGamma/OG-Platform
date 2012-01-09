/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.batch;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueId;

/**
 * A master for storing and managing batch job runs.
 */
public interface BatchMaster extends BatchRunWriter {

  /**
   * The default scheme for unique identifiers.
   */
  public static final String IDENTIFIER_SCHEME_DEFAULT = "DbBat";


  /**
   * Searches for batches matching the specified search criteria.
   *
   * @param request  the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  BatchSearchResult search(BatchSearchRequest request);

  /**
   * Gets a batch document by unique identifier.
   * <p>
   * This returns a single batch document by unique identifier.
   * It will return all the risk data and the total count of the errors.
   * For more control, use {@link #get(BatchGetRequest)}.
   *
   * @param uniqueId  the unique identifier, not null
   * @return the document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no document with that unique identifier
   */
  BatchDocument get(UniqueId uniqueId);


  /**
   * Deletes (permanently) a batch document and all its risk from the database.
   *
   * @param uniqueId  the batch uid, not null
   * @return number of deleted rows (should be either 0 or 1)                
   * @throws DataNotFoundException if there is no document with that batch identifier
   */
  int delete(UniqueId uniqueId);

}
