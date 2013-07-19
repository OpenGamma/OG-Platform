/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.exposure;

import com.opengamma.id.VersionCorrection;

/**
 * Source for {@link ExposureFunctions}s.
 */
public interface ExposureFunctionsSource {

  /**
   * Gets the latest version of the named exposure functions.
   * @param name The name of the exposure functions, not null
   * @return The exposure functions or null if not found
   */
  ExposureFunctions getExposureFunctions(String name);

  /**
   * Gets the named exposure functions that matches the version correction.
   * @param name The name of the exposure functions, not null
   * @param versionCorrection The version correction, not null
   * @return The exposure functions or null if not found
   */
  ExposureFunctions getExposureFunctions(String name, VersionCorrection versionCorrection);
}
