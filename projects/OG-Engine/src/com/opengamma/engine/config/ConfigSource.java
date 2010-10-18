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
 * A source of config documents as accessed by the engine.
 * <p>
 * This interface provides a simple view of config documents as needed by the engine.
 * This may be backed by a full-featured config master, or by a much simpler data structure.
 */
@PublicAPI
public interface ConfigSource {
  /**
   * Search for config document by providing fields in ConfigSearchRequest
   * 
   * @param <T> The type of config document
   * @param clazz The config document class, not-null
   * @param name The name for the name search field, not-null
   * @return the latest config document matching the request
   */  
  <T> T searchLatest(Class<T> clazz, String name);
  
  /**
   * Search for config document by providing fields in ConfigSearchRequest
   * 
   * @param <T> The type of config document
   * @param clazz The config document class, not-null
   * @param request The request object with value for search fields, not-null
   * @return all config document matching the request
   */
  <T> List<T> search(Class<T> clazz, ConfigSearchRequest request);
  
  /**
   * Search for config document by providing uid
   * 
   * @param <T> The type of config document
   * @param clazz The config document class, not-null
   * @param uid the unique identifier, not-null 
   * @return the config doc, null if not found
   */
  <T> T get(Class<T> clazz, UniqueIdentifier uid);
  
}
