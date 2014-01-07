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
public class NelsonSiegelBondCurveModelTest {
  private static final NelsonSiegelBondCurveModel MODEL = new NelsonSiegelBondCurveModel();
  private static final NonLinearLeastSquare NLLS = new NonLinearLeastSquare();
  private static final DoubleMatrix1D T = new DoubleMatrix1D(new double[] {1. / 12, 0.25, 0.5, 1, 2, 3, 5, 7, 10, 20, 30 });
  private static final DoubleMatrix1D Y;
  private static final double BETA0 = 3;
  private static final double BETA1 = -2;
  private static final double BETA2 = 6;
  private static final double LAMBDA = 2;
  private static final DoubleMatrix1D TREASURY_T = new DoubleMatrix1D(new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 });
  //private static final DoubleMatrix1D TREASURY_19740830_Y = new DoubleMatrix1D(new double[] {9.4411, 8.7922, 8.4988, 8.3483, 8.2579, 8.1975, 8.1545, 8.1221, 8.0970, 8.0769, 8.0605, 8.0467, 8.0351,
  //    8.0252, 8.0166});
  private static final DoubleMatrix1D TREASURY_19740102_Y = new DoubleMatrix1D(new double[] {7.2912, 6.8593, 6.7166, 6.6840, 6.6901, 6.7077, 6.7266, 6.7438, 6.7584, 6.7707, 6.7810, 6.7897, 6.7971,
    6.8035, 6.8090 });
  private static final DoubleMatrix1D TREASURY_E;

  static {
    final double[] t = T.getData();
    final double[] y = new double[t.length];
    for (int i = 0; i < t.length; i++) {
      y[i] = BETA0 + BETA1 * (1 - Math.exp(-t[i] / LAMBDA)) / (t[i] / LAMBDA) + BETA2 * ((1 - Math.exp(-t[i] / LAMBDA)) / (t[i] / LAMBDA) - Math.exp(-t[i] / LAMBDA));
    }
    Y = new DoubleMatrix1D(y);
    final double[] e = new double[TREASURY_T.getNumberOfElements()];
    Arrays.fill(e, 1e-4);
    TREASURY_E = new DoubleMatrix1D(e);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullTime() {
    MODEL.getParameterizedFunction().evaluate(null, new DoubleMatrix1D(new double[] {1, 2, 3, 4 }));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullParameters() {
    MODEL.getParameterizedFunction().evaluate(3., null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongNumberOfElements() {
    MODEL.getParameterizedFunction().evaluate(3., new DoubleMatrix1D(new double[] {1, 2, 3 }));
  }

  @Test
  public void testKnownParameters() {
    final LeastSquareResults result = NLLS.solve(T, Y, MODEL.getParameterizedFunction(), new DoubleMatrix1D(new double[] {2, -1, 1, 4 }));
    final DoubleMatrix1D fitted = result.getFitParameters();
    assertArrayEquals(fitted.getData(), new double[] {BETA0, BETA1, BETA2, LAMBDA }, 1e-6);
  }

  @Test
  public void testFit() {
    final LeastSquareResults result = NLLS.solve(TREASURY_T, TREASURY_19740102_Y, TREASURY_E, MODEL.getParameterizedFunction(), new DoubleMatrix1D(new double[] {1, 1, 1, 1 }));
    final DoubleMatrix1D fitted = result.getFitParameters();
    assertEquals(fitted.getEntry(0), 6.88649008, 1e-3);
    assertEquals(fitted.getEntry(1), 1.56992400, 1e-3);
    assertEquals(fitted.getEntry(2), -2.58262072, 1e-3);
    assertEquals(fitted.getEntry(3), 1.14781832, 1e-3);
    //    result = NLLS.solve(TREASURY_T, TREASURY_19740830_Y, TREASURY_E, MODEL.getParameterizedFunction(), new DoubleMatrix1D(new double[] {1, 1, 1, 1}));
    //    fitted = result.getParameters();
    //    assertEquals(fitted.getEntry(0), 7.89593811, 1e-3);
    //    assertEquals(fitted.getEntry(1), 0.61383027, 1e-3);
    //    assertEquals(fitted.getEntry(2), 5.35682485, 1e-3);
    //    assertEquals(fitted.getEntry(3), 0.30308753, 1e-3);
  }

}
