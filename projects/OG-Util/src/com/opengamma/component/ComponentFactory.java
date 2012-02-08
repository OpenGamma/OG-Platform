/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import java.util.LinkedHashMap;

/**
 * A factory capable of creating component(s) in the OpenGamma system.
 */
public interface ComponentFactory {

  /**
   * Invokes the factory to create the component(s).
   * <p>
   * The factory is responsible for registering the component(s) with the repository.
   * <p>
   * The remaining configuration data is a live map that is normally empty.
   * If the implementation can handle additional configuration items, it must ensure that the map
   * if empty after the completion of this method, otherwise initialization will stop.
   * This is normally accomplished using {@link java.util.Map#remove(Object)) to extract the configuration.
   * 
   * @param repo  the repository to register the component(s) with, not null
   * @param configuration  the remaining configuration data, not null
   * @throws Exception allows the implementation to throw checked exceptions
   */
  void init(ComponentRepository repo, LinkedHashMap<String, String> configuration) throws Exception;

}
