/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.component;

/**
 * A principal component of the OpenGamma system.
 */
public interface Component {

  /**
   * Starts the component using the configuration.
   * <p>
   * The started component must be registered with the repository.
   * 
   * @param repo  the repository to register the component with, not null
   * @param config  the configuration to use, not null
   */
  void start(ComponentRepository repo, ComponentConfig config);

}
