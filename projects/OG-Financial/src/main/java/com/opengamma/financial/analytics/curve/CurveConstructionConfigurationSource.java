/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import com.opengamma.id.VersionCorrection;

/**
 * Source for {@link CurveConstructionConfiguration}s.
 */
public interface CurveConstructionConfigurationSource {

  /**
   * Gets the latest version of the named curve construction configuration.
   * @param name The name of the curve construction configuration, not null
   * @return The curve construction configuration or null if not found
   */
  CurveConstructionConfiguration getCurveConstructionConfiguration(String name);

  /**
   * Gets the named curve construction configuration that matches the version correction.
   * @param name The name of the curve construction configuration, not null
   * @param versionCorrection The version correction, not null
   * @return The curve construction configuration or null if not found
   */
  CurveConstructionConfiguration getCurveConstructionConfiguration(String name, VersionCorrection versionCorrection);

}
