/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResults;
import com.opengamma.analytics.math.statistics.leastsquare.NonLinearLeastSquare;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class NelsonSiegelSvenssonBondCurveModelTest {
  private static final NonLinearLeastSquare NLLS = new NonLinearLeastSquare();
  private static final DoubleMatrix1D T = new DoubleMatrix1D(new double[] {1. / 12, 0.25, 0.5, 1, 2, 3, 5, 7, 10, 20, 30});
  private static final DoubleMatrix1D Y;
  private static final double BETA0 = 3;
  private static final double BETA1 = -2;
  private static final double BETA2 = 6;
  private static final double BETA3 = 7;
  private static final double LAMBDA1 = 2;
  private static final double LAMBDA2 = 1;
  private static final NelsonSiegelSvennsonBondCurveModel MODEL = new NelsonSiegelSvennsonBondCurveModel();
  private static final DoubleMatrix1D TREASURY_T = new DoubleMatrix1D(new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30});
  private static final DoubleMatrix1D TREASURY_20110127_Y = new DoubleMatrix1D(new double[] {0.2700, 0.5711, 1.0237, 1.5145, 1.9878, 2.4187, 2.7992, 3.1292, 3.4124, 3.6541, 3.8597, 4.0341, 4.1821,
      4.3074, 4.4134, 4.5030, 4.5786, 4.6420, 4.6952, 4.7394, 4.7760, 4.8059, 4.8301, 4.8493, 4.8642, 4.8755, 4.8835, 4.8887, 4.8915, 4.8922});
  private static final DoubleMatrix1D TREASURY_E;

  static {
    final double[] t = T.getData();
    final double[] y = new double[t.length];
    for (int i = 0; i < t.length; i++) {
      y[i] = BETA0 + BETA1 * (1 - Math.exp(-t[i] / LAMBDA1)) / (t[i] / LAMBDA1) + BETA2 * ((1 - Math.exp(-t[i] / LAMBDA1)) / (t[i] / LAMBDA1) - Math.exp(-t[i] / LAMBDA1)) + BETA3
      * ((1 - Math.exp(-t[i] / LAMBDA2)) / (t[i] / LAMBDA2) - Math.exp(-t[i] / LAMBDA2));
    }
    Y = new DoubleMatrix1D(y);
    final double[] e = new double[TREASURY_T.getNumberOfElements()];
    Arrays.fill(e, 1e-4);
    TREASURY_E = new DoubleMatrix1D(e);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTime() {
    MODEL.getParameterizedFunction().evaluate(null, new DoubleMatrix1D(new double[] {1, 2, 3, 4, 5, 6}));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullParameters() {
    MODEL.getParameterizedFunction().evaluate(3., null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongNumberOfElements() {
    MODEL.getParameterizedFunction().evaluate(3., new DoubleMatrix1D(new double[] {1, 2, 3}));
  }

  @Test
  public void testKnownParameters() {
    final LeastSquareResults result = NLLS.solve(T, Y, MODEL.getParameterizedFunction(), new DoubleMatrix1D(new double[] {2, 1, 1, 4, 6, 1 }));
    final DoubleMatrix1D fitted = result.getFitParameters();
    assertArrayEquals(fitted.getData(), new double[] {BETA0, BETA1, BETA2, LAMBDA1, BETA3, LAMBDA2}, 1e-4);
  }

  @Test
  public void testFit() {
    final LeastSquareResults result = NLLS.solve(TREASURY_T, TREASURY_20110127_Y, TREASURY_E, MODEL.getParameterizedFunction(), new DoubleMatrix1D(new double[] {2, -1, -1, 2, 2, 9 }));
    final DoubleMatrix1D fitted = result.getFitParameters();
    assertEquals(fitted.getEntry(0), 4.07923660, 1e-2);
    assertEquals(fitted.getEntry(1), -3.74204358, 1e-2);
    assertEquals(fitted.getEntry(2), -6.18790519, 1e-2);
    assertEquals(fitted.getEntry(3), 1.92088325, 1e-2);
    assertEquals(fitted.getEntry(4), 5.43483123, 1e-2);
    assertEquals(fitted.getEntry(5), 9.96780064, 1e-2);
  }

}
