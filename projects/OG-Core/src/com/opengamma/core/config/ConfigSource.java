/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.config;

import javax.time.Instant;

import com.opengamma.id.UniqueId;
import com.opengamma.util.PublicSPI;

/**
 * A source of configuration elements as accessed by the main application.
 * <p>
 * This interface provides a simple view of configuration as used by most parts of the application.
 * This may be backed by a full-featured config master, or by a much simpler data structure.
 */
@PublicSPI
public interface ConfigSource {

  /**
   * Gets a configuration element using the unique identifier.
   * 
   * @param <T>  the type of configuration element
   * @param clazz  the configuration element type, not null
   * @param uniqueId  the unique identifier, not null
   * @return the configuration element, null if not found
   */
  <T> T get(Class<T> clazz, UniqueId uniqueId);

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
   */  
  <T> T getByName(Class<T> clazz, String name, Instant versionAsOf);

}
