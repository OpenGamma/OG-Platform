/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.component;

/**
 * A factory capable of creating a component in the OpenGamma system.
 */
public interface ComponentFactory {

  /**
   * Starts the component.
   * <p>
   * The started component must be registered with the repository.
   * 
   * @param repo  the repository to register the component with, not null
   * @throws Exception allows the implementation to throw checked exceptions
   */
  void start(ComponentRepository repo) throws Exception;

}
