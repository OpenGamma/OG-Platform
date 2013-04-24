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
public interface SourceWithExternalBundle<V extends UniqueIdentifiable & ExternalBundleIdentifiable> extends Source<V>, ChangeProvider {

  /**
   * Gets all objects at the given version-correction that match the specified external identifier bundle.
   * <p>
   * A bundle represents the set of external identifiers which in theory map to a single object.
   * Unfortunately, not all external identifiers uniquely identify a single version of a single object.
   * The default behavior in standard implementations should be to return any
   * element with <strong>any</strong> external identifier that matches <strong>any</strong>
   * identifier in the bundle. While specific implementations may modify this behavior,
   * this should be explicitly documented to avoid confusion. 
   *
   * @param bundle  the bundle keys to match, not null
   * @param versionCorrection  the version-correction, not null
   * @return all objects matching the bundle, empty if no matches, not null
   * @throws IllegalArgumentException if the identifier bundle is invalid
   * @throws RuntimeException if an error occurs
   */
  Collection<V> get(ExternalIdBundle bundle, VersionCorrection versionCorrection);

  /**
   * Bulk operation form of {@link #get(ExternalIdBundle, VersionCorrection)}.
   * 
   * @param bundles the identifiers to search for, not null
   * @param versionCorrection the version-correction to search at, not null
   * @return the map of results, not null
   */
  Map<ExternalIdBundle, Collection<V>> getAll(Collection<ExternalIdBundle> bundles, VersionCorrection versionCorrection);

  //-------------------------------------------------------------------------
  // TODO: remove below here

  /**
   * Gets all objects at the latest version-correction that match the specified bundle of keys.
   * <p>
   * The identifier bundle represents those keys associated with a single object. In an ideal world, all the identifiers in a bundle would refer to the same object. However, since each identifier is
   * not completely unique, multiple may match. To further complicate matters, some identifiers are more unique than others.
   * <p>
   * The simplest implementation of this method will return a object if it matches one of the keys. A more advanced implementation will choose using some form of priority order which key or keys from
   * the bundle to search for.
   * 
   * @param bundle the bundle keys to match, not null
   * @return all objects matching the specified key, empty if no matches, not null
   * @throws IllegalArgumentException if the identifier bundle is invalid (e.g. empty)
   * @throws RuntimeException if an error occurs
   */
  Collection<V> get(ExternalIdBundle bundle);

  /**
   * Gets the single best-fit object at the latest version-correction that matches the specified bundle of keys.
   * <p>
   * The identifier bundle represents those keys associated with a single object. In an ideal world, all the identifiers in a bundle would refer to the same object. However, since each identifier is
   * not completely unique, multiple may match. To further complicate matters, some identifiers are more unique than others.
   * <p>
   * An implementation will need some mechanism to decide what the best-fit match is.
   * 
   * @param bundle the bundle keys to match, not null
   * @return the single object matching the bundle of keys, null if not found
   * @throws IllegalArgumentException if the identifier bundle is invalid (e.g. empty)
   * @throws RuntimeException if an error occurs
   */
  V getSingle(ExternalIdBundle bundle);

  /**
   * Gets the single best-fit object at the given version-correction that matches the specified bundle of keys.
   * <p>
   * The identifier bundle represents those keys associated with a single object. In an ideal world, all the identifiers in a bundle would refer to the same object. However, since each identifier is
   * not completely unique, multiple may match. To further complicate matters, some identifiers are more unique than others.
   * <p>
   * An implementation will need some mechanism to decide what the best-fit match is.
   * 
   * @param bundle the bundle keys to match, not null
   * @param versionCorrection the version-correction, not null
   * @return the single object matching the bundle of keys, null if not found
   * @throws IllegalArgumentException if the identifier bundle is invalid (e.g. empty)
   * @throws RuntimeException if an error occurs
   */
  V getSingle(ExternalIdBundle bundle, VersionCorrection versionCorrection);

  /**
   * Bulk operation form of {@link #getSingle(ExternalIdBundle, VersionCorrection)}.
   * 
   * @param bundles the identifiers to search for, not null
   * @param versionCorrection the version-correction to search at, not null
   * @return the map of results, not null
   */
  Map<ExternalIdBundle, V> getSingle(Collection<ExternalIdBundle> bundles, VersionCorrection versionCorrection);

}
