/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.config;

import java.util.Collection;

import javax.time.Instant;

import com.opengamma.DataNotFoundException;
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
public interface ConfigSource {

  /**
   * Gets a configuration element by unique identifier.
   * <p>
   * A unique identifier exactly specifies a single configuration at a single version-correction.
   * 
   * @param <T>  the type of configuration element
   * @param clazz  the configuration element type, not null
   * @param uniqueId  the unique identifier to find, not null
   * @return the matched configuration, not null
   * @throws IllegalArgumentException if the identifier is invalid
   * @throws DataNotFoundException if the configuration could not be found
   * @throws RuntimeException if an error occurs
   */
  <T> T getConfig(Class<T> clazz, UniqueId uniqueId);

  /**
   * Gets a configuration element by object identifier and version-correction.
   * <p>
   * In combination, the object identifier and version-correction exactly specify
   * a single configuration at a single version-correction.
   * 
   * @param <T>  the type of configuration element
   * @param clazz  the configuration element type, not null
   * @param objectId  the object identifier to find, not null
   * @param versionCorrection  the version-correction, not null
   * @return the matched configuration, not null
   * @throws IllegalArgumentException if the identifier or version-correction is invalid
   * @throws DataNotFoundException if the configuration could not be found
   * @throws RuntimeException if an error occurs
   */
  <T> T getConfig(Class<T> clazz, ObjectId objectId, VersionCorrection versionCorrection);

  /**
   * Gets a configuration element by name and version-correction.
   * <p>
   * Each configuration element has a name and this method allows lookup by name.
   * A name lookup does not guarantee to match a single configuration element but it normally will.
   * This method returns all configurations that may match for {@link ConfigResolver} to choose from.
   * 
   * @param <T>  the type of configuration element
   * @param clazz  the configuration element type, not null
   * @param configName  the configuration name, not null
   * @param versionCorrection  the version-correction, not null
   * @return all configuration elements matching the name, empty if no matches, not null
   * @throws IllegalArgumentException if the name or version-correction is invalid
   * @throws RuntimeException if an error occurs
   */
  <T> Collection<? extends T> getConfigs(Class<T> clazz, String configName, VersionCorrection versionCorrection);

  //-------------------------------------------------------------------------
  // TODO: remove below here
  /**
   * Searches for the latest version of a configuration element matching the specified name.
   * <p>
   * This will always return the latest version, ignoring any other version constraints
   * of the implementation.
   * 
   * @param <T>  the type of configuration element
   * @param clazz  the configuration element type, not null
   * @param name  the element name to search for, wildcards allowed, not null
   * @return the latest configuration element matching the request, null if not found
   * @throws RuntimeException if an error occurs
   */
  <T> T getLatestByName(Class<T> clazz, String name);

  /**
   * Searches for a configuration element matching the specified name.
   * <p>
   * This will always return the version requested, ignoring any other version constraints
   * of the implementation.
   * 
   * @param <T>  the type of configuration element
   * @param clazz  the configuration element type, not null
   * @param name  the element name to search for, wildcards allowed, not null
   * @param versionAsOf  the version to fetch, null means latest
   * @return the versioned configuration element matching the request, null if not found
   * @throws RuntimeException if an error occurs
   */
  <T> T getByName(Class<T> clazz, String name, Instant versionAsOf);

}
