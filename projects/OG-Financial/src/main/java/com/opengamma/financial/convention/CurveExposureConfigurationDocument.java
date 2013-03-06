/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

/**
 * Document to hold curve exposure configuration information and related meta-data.
 */
public class CurveExposureConfigurationDocument {
  private final String _name;
  private final CurveExposureConfiguration _configurationSet;

  /**
   *
   */
  public CurveExposureConfigurationDocument(final CurveExposureConfiguration configurationSet) {
    _configurationSet = configurationSet;
    _name = configurationSet.getConfigurationName();
  }

  public String getName() {
    return _name;
  }

  public CurveExposureConfiguration getConfigurationSet() {
    return _configurationSet;
  }

  public CurveExposureConfiguration getValue() {
    return _configurationSet;
  }
}
