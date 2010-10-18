/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.config;

import java.util.List;

import com.opengamma.config.ConfigSearchRequest;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.PublicAPI;

/**
 * A source of configuration documents as accessed by the engine.
 * <p>
 * This interface provides a simple view of configuration documents as needed by the engine.
 * This may be backed by a full-featured config master, or by a much simpler data structure.
 */
@PublicAPI
public interface ConfigSource {

  /**
   * Search for configuration documents using a request object.
   * 
   * @param <T>  the type of configuration document
   * @param clazz  the configuration document type, not null
   * @param request The request object with value for search fields, not null
   * @return all configuration document matching the request, not null
   */
  <T> List<T> search(Class<T> clazz, ConfigSearchRequest request);

  /**
   * Searches for the latest configuration document matching the specified name.
   * 
   * @param <T>  the type of configuration document
   * @param clazz  the configuration document type, not null
   * @param name  the document name to search for, not null
   * @return the latest configuration document matching the request, null if not found
   */  
  <T> T searchLatest(Class<T> clazz, String name);

  /**
   * Gets a configuration document using the unique identifier.
   * 
   * @param <T>  the type of configuration document
   * @param clazz  the configuration document type, not null
   * @param uid  the unique identifier, not null
   * @return the configuration doc, null if not found
   */
  <T> T get(Class<T> clazz, UniqueIdentifier uid);

}
