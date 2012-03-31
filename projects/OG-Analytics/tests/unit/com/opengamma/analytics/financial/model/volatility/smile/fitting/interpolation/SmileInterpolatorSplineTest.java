/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

/**
 * 
 */
public class SmileInterpolatorSplineTest extends SmileInterpolatorTestCase {

  private static final GeneralSmileInterpolator INTERPOLATOR = new SmileInterpolatorSpline();

  @Override
  public GeneralSmileInterpolator getSmileInterpolator() {
    return INTERPOLATOR;
  }

}
