/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import org.junit.Test;

/**
 * 
 */
public class GaussHermiteOrthogonalPolynomialGeneratingFunctionTest extends OrthogonalPolynomialGeneratingFunctionTestCase {
  private static final double SQRT_PI = Math.sqrt(Math.PI);
  private static final double DENOM1 = 4 * (3 - Math.sqrt(6));
  private static final double DENOM2 = 4 * (3 + Math.sqrt(6));
  private static final double[] X2 = new double[] {-Math.sqrt(2) / 2., Math.sqrt(2) / 2.};
  private static final double[] W2 = new double[] {SQRT_PI / 2., SQRT_PI / 2.};
  private static final double[] X3 = new double[] {-Math.sqrt(6) / 2., 0, Math.sqrt(6) / 2.};
  private static final double[] W3 = new double[] {SQRT_PI / 6., 2 * SQRT_PI / 3., SQRT_PI / 6.};
  private static final double[] X4 =
      new double[] {-Math.sqrt((3 + Math.sqrt(6)) / 2.), -Math.sqrt((3 - Math.sqrt(6)) / 2.), Math.sqrt((3 - Math.sqrt(6)) / 2.),
          Math.sqrt((3 + Math.sqrt(6)) / 2.)};
  private static final double[] W4 = new double[] {SQRT_PI / DENOM2, SQRT_PI / DENOM1, SQRT_PI / DENOM1, SQRT_PI / DENOM2};
  private static final GeneratingFunction<Double, GaussianQuadratureFunction> F = new GaussHermiteOrthogonalPolynomialGeneratingFunction();
  private static final Double[] PARAMS = new Double[] {-1., 1.};

  @Test
  public void test() {
    testInputsFixedLimits(F, PARAMS);
    testResults(F.generate(2, PARAMS), X2, W2);
    testResults(F.generate(3, PARAMS), X3, W3);
    testResults(F.generate(4, PARAMS), X4, W4);
  }
}
