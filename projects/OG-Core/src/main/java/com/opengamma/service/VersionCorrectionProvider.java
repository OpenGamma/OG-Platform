/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.service;

import com.opengamma.id.VersionCorrection;

/**
 * Provides version-corrections to the {@code ServiceContext}.
 */
public interface VersionCorrectionProvider {

  /**
   * Gets the version-correction applicable to the portfolio.
   * 
   * @return the version-correction, not null
   */
  VersionCorrection getPortfolioVersionCorrection();

  /**
   * Gets the version-correction applicable to the configuration.
   * 
   * @return the version-correction, not null
   */
  VersionCorrection getConfigVersionCorrection();

}
