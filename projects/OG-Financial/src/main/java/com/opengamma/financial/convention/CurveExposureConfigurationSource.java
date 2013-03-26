/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import com.opengamma.id.VersionCorrection;

/**
 *
 */
public interface CurveExposureConfigurationSource {

  CurveExposureConfiguration getCurveExposureConfiguration(String name);
  
  CurveExposureConfiguration getCurveExposureConfiguration(String name, VersionCorrection versionCorrection);
}
