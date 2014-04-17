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
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SABRConjugateGradientLeastSquareFitterTest extends LeastSquareSmileFitterTestCase {

  private static final SABRConjugateGradientLeastSquareFitter FITTER = new SABRConjugateGradientLeastSquareFitter(new SABRHaganVolatilityFunction());
  private static final double[] INITIAL_VALUES = new double[] {0.5, 1, 0.2, 0};

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSABRFormula() {
    new SABRConjugateGradientLeastSquareFitter(null);
  }

  @Test(expectedExceptions = UnsupportedOperationException.class)
  public void testNullErrors() {
    FITTER.getFitResult(null, null, null, null);
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
