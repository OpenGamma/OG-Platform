/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.config;

import java.util.List;

import javax.time.Instant;

import com.opengamma.config.ConfigSearchRequest;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.PublicAPI;

/**
 * A source of configuration elements as accessed by the engine.
 * <p>
 * This interface provides a simple view of configuration elements as needed by the engine.
 * This may be backed by a full-featured config master, or by a much simpler data structure.
 */
@PublicAPI
public interface ConfigSource {

  /**
   * Search for configuration elements using a request object.
   * 
   * @param <T>  the type of configuration element
   * @param clazz  the configuration element type, not null
   * @param request The request object with value for search fields, not null
   * @return all configuration elements matching the request, not null
   */
  <T> List<T> search(Class<T> clazz, ConfigSearchRequest request);

  /**
   * Gets a configuration element using the unique identifier.
   * 
   * @param <T>  the type of configuration element
   * @param clazz  the configuration element type, not null
   * @param uid  the unique identifier, not null
   * @return the configuration element, null if not found
   */
  <T> T get(Class<T> clazz, UniqueIdentifier uid);

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
