/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueIdentifier;

/**
 * A general-purpose configuration master.
 * <p>
 * The configuration master provides a uniform view over storage of configuration elements.
 * This interface provides methods that allow the master to be searched and updated.
 * <p>
 * Many different kinds of configuration element may be stored using this interface.
 * Each element type will be stored using a different instance where the generic
 * parameter represents the type of the element.
 * 
 * @param <T>  the configuration element type
 */
public interface ConfigTypeMaster<T> {

  /**
   * Searches for configuration documents matching the specified search criteria.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  ConfigSearchResult<T> search(ConfigSearchRequest request);

  /**
   * Gets a configuration document by unique identifier.
   * <p>
   * A full configuration master will store detailed historic information, including a full version history.
   * The version in the identifier allows access to these historic versions.
   * 
   * @param uid  the unique identifier, not null
   * @return the configuration document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no configuration document with that unique identifier
   */
  ConfigDocument<T> get(UniqueIdentifier uid);

  /**
   * Adds a configuration document to the data store.
   * <p>
   * The specified document must contain the configuration element.
   * 
   * @param document  the document, not null
   * @return the updated configuration document, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  ConfigDocument<T> add(ConfigDocument<T> document);

  /**
   * Updates a configuration document in the data store.
   * <p>
   * The specified document must contain the element and the unique identifier.
   * If the identifier has a version it must be the latest version.
   * <p>
   * A full configuration master will store detailed historic information, including a full version history.
   * Older versions can be accessed using a versioned identifier or {@link #history}.
   * 
   * @param document  the document, not null
   * @return the updated document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no configuration document with that unique identifier
   */
  ConfigDocument<T> update(ConfigDocument<T> document);

  /**
   * Removes a configuration document from the data store.
   * <p>
   * A full configuration master will store detailed historic information.
   * Thus, a removal does not prevent retrieval of an earlier version.
   * <p>
   * If the identifier has a version it must be the latest version.
   * 
   * @param uid  the unique identifier to remove, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no configuration document with that unique identifier
   */
  void remove(final UniqueIdentifier uid);

  /**
   * Queries the history of a single piece of configuration.
   * <p>
   * The request must contain an object identifier to identify the configuration.
   * 
   * @param request  the history request, not null
   * @return the configuration history, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  ConfigHistoryResult<T> history(ConfigHistoryRequest request);

}
