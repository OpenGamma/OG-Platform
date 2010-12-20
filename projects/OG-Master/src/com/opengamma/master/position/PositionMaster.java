/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.position;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.AbstractMaster;

/**
 * A general-purpose position master.
 * <p>
 * The position master provides a uniform view over a set of position definitions.
 * This interface provides methods that allow the master to be searched and updated.
 */
public interface PositionMaster extends AbstractMaster<PositionDocument> {

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
   * @param uid  the trade unique identifier, not null
   * @return the trade, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no trade with that unique identifier
   */
  ManageableTrade getTrade(UniqueIdentifier uid);

}
