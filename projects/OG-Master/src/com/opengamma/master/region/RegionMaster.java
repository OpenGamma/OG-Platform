/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.master.region;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.UniqueIdentifier;

/**
 * A general-purpose region master.
 * <p>
 * The region master provides a uniform view over a set of region definitions.
 * This interface provides methods that allow the master to be searched and updated.
 */
public interface RegionMaster {

  /**
   * Searches for regions matching the specified search criteria.
   * 
   * @param request  the search request, not null
   * @return the search result, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  RegionSearchResult search(RegionSearchRequest request);

  /**
   * Gets a region by unique identifier.
   * <p>
   * A full region master will store detailed historic information on regions,
   * including a full version history.
   * The version in the identifier allows access to these historic versions.
   * 
   * @param uid  the unique identifier, not null
   * @return the region document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no region with that unique identifier
   */
  RegionDocument get(UniqueIdentifier uid);

  /**
   * Adds a region to the data store.
   * <p>
   * The specified document must contain the region.
   * 
   * @param document  the document, not null
   * @return the updated region document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no parent node with the specified identifier
   */
  RegionDocument add(RegionDocument document);

  /**
   * Updates a region in the data store.
   * <p>
   * The specified document must contain the region and the unique identifier.
   * If the identifier has a version it must be the latest version.
   * <p>
   * A full region master will store detailed historic information on regions,
   * including a full version history.
   * Older versions can be accessed using a versioned identifier or {@link #history}.
   * 
   * @param document  the document, not null
   * @return the updated region document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no region with that unique identifier
   */
  RegionDocument update(RegionDocument document);

  /**
   * Removes a region from the data store.
   * <p>
   * A full region master will store detailed historic information on regions.
   * Thus, a removal does not prevent retrieval or correction of an earlier version.
   * <p>
   * If the identifier has a version it must be the latest version.
   * 
   * @param uid  the region unique identifier to remove, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no region with that unique identifier
   */
  void remove(final UniqueIdentifier uid);

  /**
   * Queries the history of a single region.
   * <p>
   * The request must contain an object identifier to identify the region.
   * 
   * @param request  the history request, not null
   * @return the region history, not null
   * @throws IllegalArgumentException if the request is invalid
   */
  RegionHistoryResult history(RegionHistoryRequest request);

  /**
   * Corrects a region in the data store.
   * <p>
   * A full region master will store detailed historic information on regions
   * and will support correction of each region.
   * To update the region with a new version, use {@link #update}.
   * To correct a previously stored version, use this method.
   * Older versions and corrections can be accessed using a versioned identifier or {@link #history}.
   * <p>
   * The specified document must contain the region and the unique identifier.
   * The unique identifier must specify the last correction of a specific version of the region.
   * 
   * @param document  the document, not null
   * @return the updated region document, not null
   * @throws IllegalArgumentException if the request is invalid
   * @throws DataNotFoundException if there is no region with that unique identifier
   */
  RegionDocument correct(RegionDocument document);

}
