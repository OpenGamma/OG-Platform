/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.config;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueIdentifier;

/**
 * General Configuration Document Repository
 * 
 * @param <T> Type of Document 
 *
 * 
 */
public interface ConfigMaster<T> {
  
  /**
   * Searches for config document matching the specified search criteria.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   */
  ConfigSearchResult<T> search(ConfigSearchRequest request);
  
  /**
   * Gets a config doc by unique identifier.
   *  
   * @param uid  the unique identifier, not null
   * @return the config document, not null
   * @throws IllegalArgumentException if the identifier is not from this config master
   * @throws DataNotFoundException if there is no config doc with that unique identifier
   */
  ConfigDocument<T> get(UniqueIdentifier uid);

  /**
   * Adds a config doc to the data store.
   * <p>
   * The specified document must contain the config document.
   * It must not contain the object identifier
   * 
   * @param document  the document, not null
   * @return the updated config document, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  ConfigDocument<T> add(ConfigDocument<T> document);

  /**
   * Updates a config doc in the data store.
   * <p>
   * The specified document must contain the config doc and the unique identifier.
   * If the identifier has a version it must be the latest version.
   * 
   * @param document  the document, not null
   * @return the updated config doc document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no security with that unique identifier
   */
  ConfigDocument<T> update(ConfigDocument<T> document);

  /**
   * Removes a config doc from the data store.
   * 
   * @param uid  the config unique identifier to remove, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no security with that unique identifier
   */
  void remove(final UniqueIdentifier uid);

  //-------------------------------------------------------------------------
  /**
   * Searches for config docs matching the specified search criteria.
   * <p>
   * The request must contain an object identifier that must not have a version.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   */
  ConfigSearchHistoricResult<T> searchHistoric(ConfigSearchHistoricRequest request);
  
  /**
   * @return all names for config documents
   */
//  Set<String> getNames();
  
}
