/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.engine.function.config;

import com.opengamma.util.PublicSPI;

/**
 * Provides a repository configuration for bootstrapping a function repository.
 * Possible implementations may:
 * <ul>
 * <li>Retrieve function information from a configuration database
 * <li>Generate function information dynamically (e.g. by scanning for annotations)
 * <li>Download the configuration from another node
 * </ul>
 */
@PublicSPI
public interface RepositoryConfigurationSource {

  /**
   * Returns a repository configuration.
   * 
   * @return the configuration, not null
   */
  RepositoryConfiguration getRepositoryConfiguration();

}
