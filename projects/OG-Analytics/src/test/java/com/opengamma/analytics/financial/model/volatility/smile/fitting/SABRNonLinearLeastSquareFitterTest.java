/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.util.test.TestGroup;

/**
 * @deprecated This class tests deprecated functionality
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class SABRNonLinearLeastSquareFitterTest extends LeastSquareSmileFitterTestCase {
  private static final SABRNonLinearLeastSquareFitter FITTER = new SABRNonLinearLeastSquareFitter(new SABRHaganVolatilityFunction());
  private static final double[] INITIAL_VALUES = new double[] {0.5, 1, 0.2, 0};

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFormula() {
    new SABRNonLinearLeastSquareFitter(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeATMVol() {
    FITTER.getFitResult(OPTIONS, FLAT_DATA, ERRORS, INITIAL_VALUES, FIXED, -0.4, true);
  }

  @Override
  protected LeastSquareSmileFitter getFitter() {
    return FITTER;
  }

  @Override
  protected double[] getInitialValues() {
    return INITIAL_VALUES;
  }

}
