/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;


/**
 * 
 */
public class SmileInterpolatorSABRTest extends SmileInterpolatorTestCase {

  private static final GeneralSmileInterpolator INTERPOLATOR = new SmileInterpolatorSABR();

  @Override
  public GeneralSmileInterpolator getSmileInterpolator() {
    return INTERPOLATOR;
  }

}
