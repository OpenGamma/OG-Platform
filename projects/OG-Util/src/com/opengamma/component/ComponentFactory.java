/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

/**
 * A factory capable of creating component(s) in the OpenGamma system.
 */
public interface ComponentFactory {

  /**
   * Invokes the factory to create the component(s).
   * <p>
   * The factory is responsible for registering the component(s) with the repository.
   * 
   * @param repo  the repository to register the component(s) with, not null
   * @throws Exception allows the implementation to throw checked exceptions
   */
  void init(ComponentRepository repo) throws Exception;

}
