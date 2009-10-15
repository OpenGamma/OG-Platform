/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * 
 * @author emcleod
 */
public class GaussLegendreOrthogonalPolynomialGeneratingFunctionTest {
  private static final double[] X2 = new double[] { -Math.sqrt(3) / 3., Math.sqrt(3) / 3. };
  private static final double[] W2 = new double[] { 1, 1 };
  private static final double[] X3 = new double[] { -Math.sqrt(15) / 5., 0, Math.sqrt(15) / 5. };
  private static final double[] W3 = new double[] { 5. / 9, 8. / 9, 5. / 9 };
  private static final double[] X4 = new double[] { -Math.sqrt(525 + 70 * Math.sqrt(30)) / 35., -Math.sqrt(525 - 70 * Math.sqrt(30)) / 35.,
      Math.sqrt(525 - 70 * Math.sqrt(30)) / 35., Math.sqrt(525 + 70 * Math.sqrt(30)) / 35. };
  private static final double[] W4 = new double[] { (18 - Math.sqrt(30)) / 36., (18 + Math.sqrt(30)) / 36., (18 + Math.sqrt(30)) / 36., (18 - Math.sqrt(30)) / 36. };
  private static final double[] X5 = new double[] { -Math.sqrt(245 + 14 * Math.sqrt(70)) / 21., -Math.sqrt(245 - 14 * Math.sqrt(70)) / 21., 0,
      Math.sqrt(245 - 14 * Math.sqrt(70)) / 21., Math.sqrt(245 + 14 * Math.sqrt(70)) / 21. };
  private static final double[] W5 = new double[] { (322 - 13 * Math.sqrt(70)) / 900., (322 + 13 * Math.sqrt(70)) / 900., 128. / 225, (322 + 13 * Math.sqrt(70)) / 900.,
      (322 - 13 * Math.sqrt(70)) / 900. };
  private static final GeneratingFunction<Double, GaussianQuadratureFunction> F = new GaussLegendreOrthogonalPolynomialGeneratingFunction();
  private static final Double[] PARAMS = new Double[] { -1., 1. };
  private static final double EPS = 1e-9;

  @Test
  public void test() {
    try {
      F.generate(-1, PARAMS);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      F.generate(3, (Double[]) null);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    try {
      F.generate(3, new Double[0]);
      fail();
    } catch (final IllegalArgumentException e) {
      // Expected
    }
    testResults(F.generate(2, PARAMS), X2, W2);
    testResults(F.generate(3, PARAMS), X3, W3);
    testResults(F.generate(4, PARAMS), X4, W4);
    testResults(F.generate(5, PARAMS), X5, W5);
  }

  private void testResults(final GaussianQuadratureFunction f, final double[] x, final double[] w) {
    final Double[] x1 = f.getAbscissas();
    final Double[] w1 = f.getWeights();
    for (int i = 0; i < x.length; i++) {
      assertEquals(x1[i].doubleValue(), x[i], EPS);
      assertEquals(w1[i].doubleValue(), w[i], EPS);
    }
  }
}
