/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.convention;

import com.opengamma.master.AbstractChangeProvidingMaster;
import com.opengamma.util.PublicSPI;

/**
 * A general-purpose convention master.
 * <p>
 * The convention master provides a uniform view over a set of convention definitions.
 * This interface provides methods that allow the master to be searched and updated.
 */
@PublicSPI
public interface ConventionMaster extends AbstractChangeProvidingMaster<ConventionDocument> {

  /**
   * Queries the meta-data about the master.
   * <p>
   * This can return information that is useful for drop-down lists.
   *
   * @param request  the search request, not null
   * @return the requested meta-data, not null
   */
  ConventionMetaDataResult metaData(ConventionMetaDataRequest request);

  /**
   * Searches for conventions matching the specified search criteria.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  ConventionSearchResult search(ConventionSearchRequest request);

  /**
   * Queries the history of a single convention.
   * <p>
   * The request must contain an object identifier to identify the convention.
   * 
   * @param request  the history request, not null
   * @return the convention history, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  ConventionHistoryResult history(ConventionHistoryRequest request);

}
