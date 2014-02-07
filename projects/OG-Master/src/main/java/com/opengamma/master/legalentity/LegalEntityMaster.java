/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.legalentity;

import com.opengamma.master.AbstractChangeProvidingMaster;
import com.opengamma.util.PublicSPI;

/**
 * A general-purpose legal entity master.
 * <p/>
 * The legal entity master provides a uniform view over a set of legal entity definitions.
 * This interface provides methods that allow the master to be searched and updated.
 */
@PublicSPI
public interface LegalEntityMaster extends AbstractChangeProvidingMaster<LegalEntityDocument> {

  /**
   * Queries the meta-data about the master.
   * <p/>
   * This can return information that is useful for drop-down lists.
   *
   * @param request the search request, not null
   * @return the requested meta-data, not null
   */
  LegalEntityMetaDataResult metaData(LegalEntityMetaDataRequest request);

  /**
   * Searches for legal entities matching the specified search criteria.
   *
   * @param request the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  LegalEntitySearchResult search(LegalEntitySearchRequest request);

  /**
   * Queries the history of a single legal entity.
   * <p/>
   * The request must contain an object identifier to identify the legal entity.
   *
   * @param request the history request, not null
   * @return the legal entities history, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  LegalEntityHistoryResult history(LegalEntityHistoryRequest request);

}
