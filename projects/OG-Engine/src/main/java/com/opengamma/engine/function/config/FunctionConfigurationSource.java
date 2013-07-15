/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.engine.function.config;

import com.opengamma.util.PublicSPI;

/**
 * Provides a function configuration for bootstrapping a function repository. Possible implementations may (but are not limited to):
 * <ul>
 * <li>Retrieve function information from a configuration database;
 * <li>Generate function information dynamically (e.g. by scanning for annotations);
 * <li>Download the configuration from another node;
 * <li>Collate the information from other {@code FunctionConfigurationSource} instances; or
 * <li>Construct the repository from static data.
 * </ul>
 */
@PublicSPI
public interface FunctionConfigurationSource {

  /**
   * Returns a function configuration bundle.
   *
   * @return the configuration, not null
   */
  FunctionConfigurationBundle getFunctionConfiguration();

}
