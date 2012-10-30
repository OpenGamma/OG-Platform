/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import org.testng.annotations.Test;

/**
 * 
 */
public class SmileInterpolatorSplineTest extends SmileInterpolatorTestCase {
  private static final GeneralSmileInterpolator INTERPOLATOR = new SmileInterpolatorSpline();

  @Override
  public GeneralSmileInterpolator getSmileInterpolator() {
    return INTERPOLATOR;
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInterpolator() {
    new SmileInterpolatorSpline(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadDataLeftExtrapolation() {
    INTERPOLATOR.getVolatilityFunction(STRIKES[0] - 10, STRIKES, EXPIRY, VOLS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadDataRightExtrapolation() {
    INTERPOLATOR.getVolatilityFunction(STRIKES[STRIKES.length - 1] + 10, STRIKES, EXPIRY, VOLS);
  }

}
