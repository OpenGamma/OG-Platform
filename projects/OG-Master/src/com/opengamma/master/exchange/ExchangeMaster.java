/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.exchange;

import com.opengamma.master.AbstractMaster;

/**
 * A general-purpose exchange master.
 * <p>
 * The exchange master provides a uniform view over a set of exchange definitions.
 * This interface provides methods that allow the master to be searched and updated.
 */
public interface ExchangeMaster extends AbstractMaster<ExchangeDocument> {

  /**
   * Searches for exchanges matching the specified search criteria.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  ExchangeSearchResult search(ExchangeSearchRequest request);

  /**
   * Queries the history of a single exchange.
   * <p>
   * The request must contain an object identifier to identify the exchange.
   * 
   * @param request  the history request, not null
   * @return the exchange history, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  ExchangeHistoryResult history(ExchangeHistoryRequest request);

}
