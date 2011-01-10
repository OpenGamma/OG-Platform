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
public class GaussLegendreOrthogonalPolynomialGeneratingFunctionTest extends OrthogonalPolynomialGeneratingFunctionTestCase {
  private static final double[] X2 = new double[] {-Math.sqrt(3) / 3., Math.sqrt(3) / 3.};
  private static final double[] W2 = new double[] {1, 1};
  private static final double[] X3 = new double[] {-Math.sqrt(15) / 5., 0, Math.sqrt(15) / 5.};
  private static final double[] W3 = new double[] {5. / 9, 8. / 9, 5. / 9};
  private static final double[] X4 =
      new double[] {-Math.sqrt(525 + 70 * Math.sqrt(30)) / 35., -Math.sqrt(525 - 70 * Math.sqrt(30)) / 35., Math.sqrt(525 - 70 * Math.sqrt(30)) / 35.,
          Math.sqrt(525 + 70 * Math.sqrt(30)) / 35.};
  private static final double[] W4 =
      new double[] {(18 - Math.sqrt(30)) / 36., (18 + Math.sqrt(30)) / 36., (18 + Math.sqrt(30)) / 36., (18 - Math.sqrt(30)) / 36.};
  private static final double[] X5 =
      new double[] {-Math.sqrt(245 + 14 * Math.sqrt(70)) / 21., -Math.sqrt(245 - 14 * Math.sqrt(70)) / 21., 0, Math.sqrt(245 - 14 * Math.sqrt(70)) / 21.,
          Math.sqrt(245 + 14 * Math.sqrt(70)) / 21.};
  private static final double[] W5 =
      new double[] {(322 - 13 * Math.sqrt(70)) / 900., (322 + 13 * Math.sqrt(70)) / 900., 128. / 225, (322 + 13 * Math.sqrt(70)) / 900.,
          (322 - 13 * Math.sqrt(70)) / 900.};
  private static final GeneratingFunction<Double, GaussianQuadratureFunction> F = new GaussLegendreOrthogonalPolynomialGeneratingFunction();
  private static final Double[] PARAMS = new Double[] {-1., 1.};

  @Test
  public void test() {
    testInputs(F, PARAMS);
    testResults(F.generate(2, PARAMS), X2, W2);
    testResults(F.generate(3, PARAMS), X3, W3);
    testResults(F.generate(4, PARAMS), X4, W4);
    testResults(F.generate(5, PARAMS), X5, W5);
  }
}
