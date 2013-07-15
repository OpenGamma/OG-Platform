/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueId;
import com.opengamma.master.AbstractChangeProvidingMaster;
import com.opengamma.util.PublicSPI;

/**
 * A general-purpose position master.
 * <p>
 * The position master provides a uniform view over a set of position definitions.
 * This interface provides methods that allow the master to be searched and updated.
 */
@PublicSPI
public interface PositionMaster extends AbstractChangeProvidingMaster<PositionDocument> {

  /**
   * Searches for positions matching the specified search criteria.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  PositionSearchResult search(PositionSearchRequest request);

  /**
   * Queries the history of a single position.
   * <p>
   * The request must contain an object identifier to identify the position.
   * 
   * @param request  the history request, not null
   * @return the position history, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  PositionHistoryResult history(PositionHistoryRequest request);

  /**
   * Gets a trade by unique identifier.
   * <p>
   * If the master supports history then the version in the identifier will be used
   * to return the requested historic version.
   * 
   * @param tradeId  the trade unique identifier, not null
   * @return the trade, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no trade with that unique identifier
   */
  ManageableTrade getTrade(UniqueId tradeId);

}
