/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueIdentifier;

/**
 * A general-purpose security master.
 * <p>
 * The security master provides a uniform view over a set of security definitions.
 * This interface provides methods that allow the master to be searched and updated.
 */
public interface SecurityMaster {

  /**
   * Searches for securities matching the specified search criteria.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  SecuritySearchResult search(SecuritySearchRequest request);

  /**
   * Gets a security by unique identifier.
   * <p>
   * A full security master will store detailed historic information on securities,
   * including a full version history.
   * The version in the identifier allows access to these historic versions.
   * 
   * @param uid  the unique identifier, not null
   * @return the security document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no security with that unique identifier
   */
  SecurityDocument get(UniqueIdentifier uid);

  /**
   * Adds a security to the data store.
   * <p>
   * The specified document must contain the security.
   * 
   * @param document  the document, not null
   * @return the updated security document, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  SecurityDocument add(SecurityDocument document);

  /**
   * Updates a security in the data store.
   * <p>
   * The specified document must contain the security and the unique identifier.
   * If the identifier has a version it must be the latest version.
   * <p>
   * A full security master will store detailed historic information on securities,
   * including a full version history.
   * Older versions can be accessed using a versioned identifier or {@link #history}.
   * 
   * @param document  the document, not null
   * @return the updated security document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no security with that unique identifier
   */
  SecurityDocument update(SecurityDocument document);

  /**
   * Removes a security from the data store.
   * <p>
   * A full security master will store detailed historic information on securities.
   * Thus, a removal does not prevent retrieval or correction of an earlier version.
   * <p>
   * If the identifier has a version it must be the latest version.
   * 
   * @param uid  the security unique identifier to remove, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no security with that unique identifier
   */
  void remove(final UniqueIdentifier uid);

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

  /**
   * Corrects a security in the data store.
   * <p>
   * A full security master will store detailed historic information on securities
   * and will support correction of each security.
   * To update the security with a new version, use {@link #update}.
   * To correct a previously stored version, use this method.
   * Older versions and corrections can be accessed using a versioned identifier or {@link #history}.
   * <p>
   * The specified document must contain the security and the unique identifier.
   * The unique identifier must specify the last correction of a specific version of the security.
   * 
   * @param document  the document, not null
   * @return the updated security document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no security with that unique identifier
   */
  SecurityDocument correct(SecurityDocument document);

}
