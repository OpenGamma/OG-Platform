/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.service;

import com.opengamma.id.VersionCorrection;

/**
 * Provides version-corrections.
 */
public interface VersionCorrectionProvider {

  VersionCorrection getPortfolioVersionCorrection();

  VersionCorrection getConfigVersionCorrection();

}
