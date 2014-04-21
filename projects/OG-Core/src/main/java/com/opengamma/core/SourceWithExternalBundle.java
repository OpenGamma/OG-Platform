/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core;

import java.util.Collection;
import java.util.Map;

import com.opengamma.core.change.ChangeProvider;
import com.opengamma.id.ExternalBundleIdentifiable;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.VersionCorrection;

/**
 * A source of with additional methods working with external bundle ids.
 * <p>
 * This interface is read-only. Implementations must be thread-safe.
 * 
 * @param <V> the type returned by the source
 */
public interface SourceWithExternalBundle<V extends UniqueIdentifiable & ExternalBundleIdentifiable>
    extends Source<V>, ChangeProvider {

  /**
   * Gets objects by external identifier bundle and version-correction.
   * <p>
   * A bundle represents the set of external identifiers which in theory map to a single object.
   * Unfortunately, not all external identifiers uniquely identify a single version of a single object.
   * The default behavior in standard implementations should be to return any
   * element with <strong>any</strong> external identifier that matches <strong>any</strong>
   * identifier in the bundle. While specific implementations may modify this behavior,
   * this should be explicitly documented to avoid confusion. 
   *
   * @param bundle  the external identifier bundle to search for, not null
   * @param versionCorrection  the version-correction, not null
   * @return all objects matching the bundle, empty if no matches, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws RuntimeException if an error occurs
   */
  Collection<V> get(ExternalIdBundle bundle, VersionCorrection versionCorrection);

  /**
   * Bulk gets objects by external identifier bundle and version-correction.
   * <p>
   * This retrieves a set of objects stored using the object identifiers at the instant
   * specified by the version-correction.
   * If not found, the external identifier will be missing from the result map.
   * <p>
   * This bulk method is equivalent to {@link #get(ExternalIdBundle, VersionCorrection)}
   * for multiple lookups and potentially more efficient.
   * 
   * @param bundles  the set of bundles to search for, not null
   * @param versionCorrection the version-correction to search at, not null
   * @return the map of results, not null
   * @throws IllegalArgumentException if an identifier is invalid
   * @throws RuntimeException if an error occurs
   */
  Map<ExternalIdBundle, Collection<V>> getAll(Collection<ExternalIdBundle> bundles, VersionCorrection versionCorrection);

  //-------------------------------------------------------------------------
  // TODO: remove below here

  /**
   * Gets objects by external identifier bundle at the latest version-correction.
   * <p>
   * A bundle represents the set of external identifiers which in theory map to a single object.
   * Unfortunately, not all external identifiers uniquely identify a single version of a single object.
   * The default behavior in standard implementations should be to return any
   * element with <strong>any</strong> external identifier that matches <strong>any</strong>
   * identifier in the bundle. While specific implementations may modify this behavior,
   * this should be explicitly documented to avoid confusion. 
   *
   * @param bundle  the external identifier bundle to search for, not null
   * @return all objects matching the bundle, empty if no matches, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws RuntimeException if an error occurs
   */
  Collection<V> get(ExternalIdBundle bundle);

  /**
   * Gets an object by external identifier bundle at the latest version-correction.
   * <p>
   * This retrieves the object stored using the external identifier at the latest
   * version-correction. If not found, an exception is thrown.
   * <p>
   * The identifier bundle represents those keys associated with a single object.
   * In an ideal world, all the identifiers in a bundle would refer to the same object.
   * However, since each identifier is not completely unique, multiple may match.
   * To further complicate matters, some identifiers are more unique than others.
   * The best-match mechanism is implementation specific.
   * 
   * @param bundle  the external identifier bundle to search for, not null
   * @return the matched object, null if not found
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws RuntimeException if an error occurs
   */
  V getSingle(ExternalIdBundle bundle);

  /**
   * Gets an object by external identifier bundle and version-correction.
   * <p>
   * This retrieves the object stored using the external identifier at the instant
   * specified by the version-correction. If not found, an exception is thrown.
   * <p>
   * The identifier bundle represents those keys associated with a single object.
   * In an ideal world, all the identifiers in a bundle would refer to the same object.
   * However, since each identifier is not completely unique, multiple may match.
   * To further complicate matters, some identifiers are more unique than others.
   * The best-match mechanism is implementation specific.
   * 
   * @param bundle  the external identifier bundle to search for, not null
   * @param versionCorrection  the version-correction, not null
   * @return the matched object, null if not found
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws RuntimeException if an error occurs
   */
  V getSingle(ExternalIdBundle bundle, VersionCorrection versionCorrection);

  /**
   * Bulk gets objects by external identifier and version-correction.
   * <p>
   * This retrieves a set of objects stored using the external identifiers at the instant
   * specified by the version-correction.
   * If not found, the external identifier will be missing from the result map.
   * <p>
   * This bulk method is equivalent to {@link #getSingle(ExternalIdBundle, VersionCorrection)}
   * for multiple lookups and potentially more efficient.
   * 
   * @param bundles  the external identifier bundles to search for, not null
   * @param versionCorrection  the version-correction, not null
   * @return the map of results, if there is no data for an identifier it will be missing from the map, not null
   * @throws IllegalArgumentException if an identifier is invalid
   * @throws RuntimeException if an error occurs
   */
  Map<ExternalIdBundle, V> getSingle(Collection<ExternalIdBundle> bundles, VersionCorrection versionCorrection);

}
