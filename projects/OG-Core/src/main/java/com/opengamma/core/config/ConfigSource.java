/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.config;

import java.util.Collection;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.Source;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.PublicSPI;

/**
 * A source of configuration elements as accessed by the main application.
 * <p>
 * This interface provides a simple view of configuration as used by most parts of the application.
 * This may be backed by a full-featured config master, or by a much simpler data structure.
 * <p>
 * This interface is read-only.
 * Implementations must be thread-safe.
 */
@PublicSPI
public interface ConfigSource extends Source<ConfigItem<?>>, ChangeProvider {

  /**
   * Gets a configuration element by unique identifier.
   * <p>
   * A unique identifier exactly specifies a single configuration at a single version-correction.
   *
   * @param uniqueId  the unique identifier to find, not null
   * @return the matched configuration, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the configuration could not be found
   * @throws RuntimeException if an error occurs
   */
  @Override
  ConfigItem<?> get(UniqueId uniqueId);

  /**
   * Gets a configuration element by object identifier and version-correction.
   * <p>
   * In combination, the object identifier and version-correction exactly specify
   * a single configuration at a single version-correction.
   *
   * @param objectId  the object identifier to find, not null
   * @param versionCorrection  the version-correction, not null
   * @return the matched configuration, not null
   * @throws IllegalArgumentException if the identifier or version-correction is invalid
   * @throws DataNotFoundException if the configuration could not be found
   * @throws RuntimeException if an error occurs
   */
  @Override
  ConfigItem<?> get(ObjectId objectId, VersionCorrection versionCorrection);

  /**
   * Gets configuration elements by name and version-correction.
   * <p>
   * Each configuration element has a name and this method allows lookup by name.
   * A name lookup does not guarantee to match a single configuration element but it normally will.
   * 
   * @param <R> the type of configuration element
   * @param clazz the configuration element type, not null
   * @param configName the configuration name, not null
   * @param versionCorrection the version-correction, not null
   * @return the elements matching the name, empty if no matches, not null
   * @throws IllegalArgumentException if the name or version-correction is invalid
   * @throws RuntimeException if an error occurs
   */
  <R> Collection<ConfigItem<R>> get(Class<R> clazz, String configName, VersionCorrection versionCorrection);

  //-------------------------------------------------------------------------
  // TODO: remove below here
  /**
   * Searches for all configuration elements.
   * <p>
   * This will always return the version requested, ignoring any other version constraints
   * of the implementation.
   *
   * @param <R>  the type of configuration element
   * @param clazz  the configuration element type, not null
   * @param versionCorrection the version to fetch, null means latest
   * @return the versioned configuration elements, null if not found
   * @throws RuntimeException if an error occurs
   */
  <R> Collection<ConfigItem<R>> getAll(Class<R> clazz, VersionCorrection versionCorrection);

  /**
   * Gets a configuration element by unique identifier.
   * <p>
   * A unique identifier exactly specifies a single configuration at a single version-correction.
   *
   * @param <R>  the type of configuration element
   * @param clazz  the configuration element type, not null
   * @param uniqueId  the unique identifier to find, not null
   * @return the matched configuration, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the configuration could not be found
   * @throws RuntimeException if an error occurs
   */
  <R> R getConfig(Class<R> clazz, UniqueId uniqueId);

  /**
   * Gets a configuration element by object identifier and version-correction.
   * <p>
   * In combination, the object identifier and version-correction exactly specify
   * a single configuration at a single version-correction.
   *
   * @param <R>  the type of configuration element
   * @param clazz  the configuration element type, not null
   * @param objectId  the object identifier to find, not null
   * @param versionCorrection  the version-correction, not null
   * @return the matched configuration, not null
   * @throws IllegalArgumentException if the identifier or version-correction is invalid
   * @throws DataNotFoundException if the configuration could not be found
   * @throws RuntimeException if an error occurs
   */
  <R> R getConfig(Class<R> clazz, ObjectId objectId, VersionCorrection versionCorrection);

  /**
   * Gets a single best-fit configuration element by name.
   * <p>
   * A name lookup does not guarantee to match a single configuration element but it normally will. In the case where it does not an implementation will need some mechanism to decide what the best-fit
   * match is.
   *
   * @param <R> the type of configuration element
   * @param clazz the configuration element type, not null
   * @param configName the configuration name, not null
   * @param versionCorrection the version-correction, not null
   * @return the configuration element matching the name, null if not found
   * @throws IllegalArgumentException if the name or version-correction is invalid
   * @throws RuntimeException if an error occurs
   */
  <R> R getSingle(Class<R> clazz, String configName, VersionCorrection versionCorrection);

  /**
   * Searches for the latest version of a configuration element matching the specified name.
   * <p>
   * This will always return the latest version, ignoring any other version constraints
   * of the implementation.
   *
   * @param <R>  the type of configuration element
   * @param clazz  the configuration element type, not null
   * @param name  the element name to search for, wildcards allowed, not null
   * @return the latest configuration element matching the request, null if not found
   * @throws RuntimeException if an error occurs
   */
  <R> R getLatestByName(Class<R> clazz, String name);

}
