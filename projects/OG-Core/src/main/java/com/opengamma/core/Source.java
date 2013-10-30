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
   * This retrieves the object stored using the unique identifier.
   * If not found, an exception is thrown.
   * 
   * @param uniqueId  the unique identifier to search for, not null
   * @return the matched object, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the object could not be found
   * @throws RuntimeException if an error occurs
   */
  V get(UniqueId uniqueId);

  /**
   * Gets an object by object identifier and version-correction.
   * <p>
   * This retrieves the object stored using the object identifier at the instant
   * specified by the version-correction. If not found, an exception is thrown.
   * In combination, the object identifier and version-correction are equivalent to 
   * a unique identifier.
   * 
   * @param objectId  the object identifier to search for, not null
   * @param versionCorrection  the version-correction, not null
   * @return the matched object, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the object could not be found
   * @throws RuntimeException if an error occurs
   */
  V get(ObjectId objectId, VersionCorrection versionCorrection);

  //-------------------------------------------------------------------------
  /**
   * Bulk gets objects by unique identifier.
   * <p>
   * This retrieves a set of objects stored using the unique identifiers.
   * If not found, the unique identifier will be missing from the result map.
   * <p>
   * This bulk method is equivalent to {@link #get(UniqueId)}
   * for multiple lookups and potentially more efficient.
   * 
   * @param uniqueIds  the unique identifiers to search for, not null
   * @return the map of results, if there is no data for an identifier it will be missing from the map, not null
   * @throws IllegalArgumentException if an identifier is invalid
   * @throws RuntimeException if an error occurs
   */
  Map<UniqueId, V> get(Collection<UniqueId> uniqueIds);

  /**
   * Bulk gets objects by object identifier and version-correction.
   * <p>
   * This retrieves a set of objects stored using the object identifiers at the instant
   * specified by the version-correction.
   * If not found, the object identifier will be missing from the result map.
   * In combination, the object identifier and version-correction are equivalent to 
   * a unique identifier.
   * <p>
   * This bulk method is equivalent to {@link #get(ObjectId, VersionCorrection)}
   * for multiple lookups and potentially more efficient.
   * 
   * @param objectIds  the object identifiers to search for, not null
   * @param versionCorrection  the version-correction, not null
   * @return the map of results, if there is no data for an identifier it will be missing from the map, not null
   * @throws IllegalArgumentException if an identifier is invalid
   * @throws RuntimeException if an error occurs
   */
  Map<ObjectId, V> get(Collection<ObjectId> objectIds, VersionCorrection versionCorrection);

}
