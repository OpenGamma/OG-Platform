/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.exchange.master;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueIdentifier;

/**
 * A general-purpose exchange master.
 * <p>
 * The exchange master provides a uniform view over a set of exchange definitions.
 * This interface provides methods that allow the master to be searched and updated.
 */
public interface ExchangeMaster {

  /**
   * Searches for exchanges matching the specified search criteria.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  ExchangeSearchResult searchExchanges(ExchangeSearchRequest request);

  /**
   * Gets a exchange by unique identifier.
   * <p>
   * A full exchange master will store detailed historic information on exchanges,
   * including a full version history.
   * The version in the identifier allows access to these historic versions.
   * 
   * @param uid  the unique identifier, not null
   * @return the exchange document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no exchange with that unique identifier
   */
  ExchangeDocument getExchange(UniqueIdentifier uid);

  /**
   * Adds an exchange to the data store.
   * <p>
   * The specified document must contain the exchange.
   * 
   * @param document  the document, not null
   * @return the updated exchange document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no parent node with the specified identifier
   */
  ExchangeDocument addExchange(ExchangeDocument document);

  /**
   * Updates an exchange in the data store.
   * <p>
   * The specified document must contain the exchange and the unique identifier.
   * If the identifier has a version it must be the latest version.
   * <p>
   * A full exchange master will store detailed historic information on exchanges,
   * including a full version history.
   * Older versions can be accessed using a versioned identifier or {@link #searchHistoricExchange}.
   * 
   * @param document  the document, not null
   * @return the updated exchange document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no exchange with that unique identifier
   */
  ExchangeDocument updateExchange(ExchangeDocument document);

  /**
   * Removes an exchange from the data store.
   * <p>
   * A full exchange master will store detailed historic information on exchanges.
   * Thus, a removal does not prevent retrieval or correction of an earlier version.
   * <p>
   * If the identifier has a version it must be the latest version.
   * 
   * @param uid  the exchange unique identifier to remove, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no exchange with that unique identifier
   */
  void removeExchange(final UniqueIdentifier uid);

  /**
   * Searches for exchanges matching the specified search criteria.
   * <p>
   * The request must contain an object identifier that must not have a version.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  ExchangeSearchHistoricResult searchHistoricExchange(ExchangeSearchHistoricRequest request);

  /**
   * Corrects an exchange in the data store.
   * <p>
   * A full exchange master will store detailed historic information on exchanges
   * and will support correction of each exchange.
   * To update the exchange with a new version, use {@link #updateExchange}.
   * To correct a previously stored version, use this method.
   * Older versions and corrections can be accessed using a versioned identifier or {@link #searchHistoricExchange}.
   * <p>
   * The specified document must contain the exchange and the unique identifier.
   * The unique identifier must specify the last correction of a specific version of the exchange.
   * 
   * @param document  the document, not null
   * @return the updated exchange document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no exchange with that unique identifier
   */
  ExchangeDocument correctExchange(ExchangeDocument document);

}
