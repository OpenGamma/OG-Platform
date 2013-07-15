/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core;

import java.util.Collection;
import java.util.Map;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;

/**
 * A source of snapshot information as accessed by the main application.
 * <p>
 * This interface is read-only. Implementations must be thread-safe.
 * 
 * @param <V> the type returned by the source
 */
public interface Source<V> {

  /**
   * Gets an object by unique identifier.
   * <p>
   * 
   * @param uniqueId the unique identifier, not null
   * @return the UniqueIdentifiable object, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the object could not be found
   * @throws RuntimeException if an error occurs
   */
  V get(UniqueId uniqueId);

  /**
   * Gets an object by object identifier and version-correction.
   * <p>
   * In combination, the object identifier and version-correction exactly specify a single object at a single version-correction.
   * 
   * @param objectId the object identifier to find, not null
   * @param versionCorrection the version-correction, not null
   * @return the matched object, not null
   * @throws IllegalArgumentException if the identifier or version-correction is invalid
   * @throws DataNotFoundException if the object could not be found
   * @throws RuntimeException if an error occurs
   */
  V get(ObjectId objectId, VersionCorrection versionCorrection);

  /**
   * Gets objects by unique identifier.
   * <p>
   * A unique identifier exactly specifies a single object at a single version-correction. This bulk method is potentially a more efficient form of {@link #get(UniqueId)} for multiple lookups.
   * 
   * @param uniqueIds the unique identifiers to query, not null
   * @return the map of results, if there is no data for an identifier it will be missing from the map, not null
   */
  Map<UniqueId, V> get(Collection<UniqueId> uniqueIds);

  /**
   * Gets objects by object identifier and version-correction.
   * <p>
   * In combination, the object identifier and version-correction exactly specify a single object at a single version-correction. This bulk method is potentially a more efficient form of
   * {@link #get(ObjectId,VersionCorrection)} for multiple lookups.
   * 
   * @param objectIds the object identifiers to query, not null
   * @param versionCorrection the version-correction, not null
   * @return the map of results, if there is no data for an identifier it will be missing from the map, not null
   */
  Map<ObjectId, V> get(Collection<ObjectId> objectIds, VersionCorrection versionCorrection);

}
