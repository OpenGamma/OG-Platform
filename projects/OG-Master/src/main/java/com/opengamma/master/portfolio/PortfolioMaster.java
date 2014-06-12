/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.portfolio;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueId;
import com.opengamma.master.AbstractChangeProvidingMaster;
import com.opengamma.util.PublicSPI;

/**
 * A general-purpose portfolio master.
 * <p>
 * The portfolio master provides a uniform view over a set of portfolio definitions.
 * This interface provides methods that allow the master to be searched and updated.
 */
@PublicSPI
public interface PortfolioMaster extends AbstractChangeProvidingMaster<PortfolioDocument> {

  /**
   * Searches for portfolios matching the specified search criteria.
   * <p>
   * The result will never contain positions, and may contain the node tree depending
   * on the depth parameter in the request.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  PortfolioSearchResult search(PortfolioSearchRequest request);

  /**
   * Queries the history of a single portfolio.
   * <p>
   * The request must contain an object identifier to identify the portfolio.
   * 
   * @param request  the history request, not null
   * @return the portfolio history, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  PortfolioHistoryResult history(PortfolioHistoryRequest request);

  /**
   * Gets a portfolio node by unique identifier.
   * <p>
   * If the master supports history then the version in the identifier will be used
   * to return the requested historic version.
   * 
   * @param nodeId  the node unique identifier, not null
   * @return the node, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no node with that unique identifier
   */
  ManageablePortfolioNode getNode(UniqueId nodeId);

}
