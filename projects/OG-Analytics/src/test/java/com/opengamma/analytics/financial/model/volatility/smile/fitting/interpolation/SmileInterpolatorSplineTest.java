/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
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
    INTERPOLATOR.getVolatilityFunction(getStrikes()[0] - 10, getStrikes(), getExpiry(), getVols());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadDataRightExtrapolation() {
    INTERPOLATOR.getVolatilityFunction(getStrikes()[getStrikes().length - 1] + 10, getStrikes(), getExpiry(), getVols());
  }

}
