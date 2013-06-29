/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility;

import com.opengamma.financial.analytics.volatility.cube.SwaptionVolatilityCubeSpecification;
import com.opengamma.id.VersionCorrection;

/**
 * A source of swaption volatility cube specifications.
 */
public interface SwaptionVolatilityCubeSpecificationSource {

  /**
   * Gets a swaption volatility cube specification for a name
   * @param name The name of the swaption volatility cube specification, not null
   * @return The specification, null if not found
   */
  SwaptionVolatilityCubeSpecification getSpecification(String name);

  /**
   * Gets a swaption volatility cube specification for a name and version
   * @param name The name of the swaption volatility cube specification, not null
   * @param versionCorrection The version correction, not null
   * @return The specification, null if not found
   */
  SwaptionVolatilityCubeSpecification getSpecification(String name, VersionCorrection versionCorrection);
}
