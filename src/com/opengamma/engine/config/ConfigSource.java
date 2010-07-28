/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.config;

import java.util.List;

import com.opengamma.config.ConfigSearchRequest;
import com.opengamma.id.UniqueIdentifier;

/**
 * 
 */
public interface ConfigSource {
  
  <T> List<T> search(Class<T> clazz, ConfigSearchRequest request);
  
  <T> T get(Class<T> clazz, UniqueIdentifier identifier);
  
}
