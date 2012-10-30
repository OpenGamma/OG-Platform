/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security;

import com.opengamma.master.AbstractChangeProvidingMaster;
import com.opengamma.util.PublicSPI;

/**
 * A general-purpose security master.
 * <p>
 * The security master provides a uniform view over a set of security definitions.
 * This interface provides methods that allow the master to be searched and updated.
 */
@PublicSPI
public interface SecurityMaster extends AbstractChangeProvidingMaster<SecurityDocument> {

  /**
   * Queries the meta-data about the master.
   * <p>
   * This can return information that is useful for drop-down lists.
   *
   * @param request  the search request, not null
   * @return the requested meta-data, not null
   */
  SecurityMetaDataResult metaData(SecurityMetaDataRequest request);

  /**
   * Searches for securities matching the specified search criteria.
   *
   * @param request  the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  SecuritySearchResult search(SecuritySearchRequest request);

  /**
   * Queries the history of a single security.
   * <p>
   * The request must contain an object identifier to identify the security.
   *
   * @param request  the history request, not null
   * @return the security history, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  SecurityHistoryResult history(SecurityHistoryRequest request);

}
