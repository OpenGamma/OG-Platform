/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static com.opengamma.analytics.math.matrix.MatrixAlgebraFactory.OG_ALGEBRA;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.PiecewisePolynomialFunction1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class MeshgridInterpolator2DTest {

  private static final double EPS = 1e-12;

  /**
   * 
   */
  @Test
  public void linearTest() {
    double[] x0Values = new double[] {1., 2., 3., 4. };
    double[] x1Values = new double[] {-1., 0., 1., 2., 3. };
    final int n0Data = x0Values.length;
    final int n1Data = x1Values.length;
    double[][] yValues = new double[n0Data][n1Data];

    for (int i = 0; i < n0Data; ++i) {
      for (int j = 0; j < n1Data; ++j) {
        yValues[i][j] = (x0Values[i] + 2.) * (x1Values[j] + 5.);
      }
    }

    PiecewisePolynomialInterpolator method = new CubicSplineInterpolator();
    MeshgridInterpolator2D interp = new MeshgridInterpolator2D(new PiecewisePolynomialInterpolator[] {method, method });

    final int n0Keys = 51;
    final int n1Keys = 61;
    double[] x0Keys = new double[n0Keys];
    double[] x1Keys = new double[n1Keys];
    for (int i = 0; i < n0Keys; ++i) {
      x0Keys[i] = 0. + 5. * i / (n0Keys - 1);
    }
    for (int i = 0; i < n1Keys; ++i) {
      x1Keys[i] = -2. + 6. * i / (n1Keys - 1);
    }

    double[][] resValues = interp.interpolate(x0Values, x1Values, yValues, x0Keys, x1Keys).getData();
    double[][] resX0 = interp.differentiateX0(x0Values, x1Values, yValues, x0Keys, x1Keys).getData();
    double[][] resX1 = interp.differentiateX1(x0Values, x1Values, yValues, x0Keys, x1Keys).getData();
    double[][] resX0Twice = interp.differentiateX0Twice(x0Values, x1Values, yValues, x0Keys, x1Keys).getData();
    double[][] resX1Twice = interp.differentiateX1Twice(x0Values, x1Values, yValues, x0Keys, x1Keys).getData();

    for (int i = 0; i < n0Keys; ++i) {
      for (int j = 0; j < n1Keys; ++j) {
        final double expVal = (x0Keys[i] + 2.) * (x1Keys[j] + 5.);
        final double ref = Math.abs(expVal) == 0. ? 1. : Math.abs(expVal);
        assertEquals(resValues[i][j], expVal, ref * EPS);
      }
    }
    for (int i = 0; i < n0Keys; ++i) {
      for (int j = 0; j < n1Keys; ++j) {
        final double expVal = (x1Keys[j] + 5.);
        final double ref = Math.abs(expVal) == 0. ? 1. : Math.abs(expVal);
        assertEquals(resX0[i][j], expVal, ref * EPS);
      }
    }
    for (int i = 0; i < n0Keys; ++i) {
      for (int j = 0; j < n1Keys; ++j) {
        final double expVal = (x0Keys[i] + 2.);
        final double ref = Math.abs(expVal) == 0. ? 1. : Math.abs(expVal);
        assertEquals(resX1[i][j], expVal, ref * EPS);
      }
    }
    for (int i = 0; i < n0Keys; ++i) {
      for (int j = 0; j < n1Keys; ++j) {
        assertEquals(resX0Twice[i][j], 0., EPS);
      }
    }
    for (int i = 0; i < n0Keys; ++i) {
      for (int j = 0; j < n1Keys; ++j) {
        assertEquals(resX1Twice[i][j], 0., EPS);
      }
    }
    {
      final double expVal = (x0Keys[1] + 2.) * (x1Keys[2] + 5.);
      final double ref = Math.abs(expVal) == 0. ? 1. : Math.abs(expVal);
      assertEquals(interp.interpolate(x0Values, x1Values, yValues, x0Keys[1], x1Keys[2]), expVal, ref * EPS);
    }
    {
      final double expVal = (x0Keys[23] + 2.) * (x1Keys[20] + 5.);
      final double ref = Math.abs(expVal) == 0. ? 1. : Math.abs(expVal);
      assertEquals(interp.interpolate(x0Values, x1Values, yValues, x0Keys[23], x1Keys[20]), expVal, ref * EPS);
    }
  }

  /**
   * 
   */
  @Test
  public void generalTest() {
    double[] x0Values = new double[] {1., 2., 3., 4. };
    double[] x1Values = new double[] {-1., 0., 1., 2., 3., 5. };
    final int n0Data = x0Values.length;
    final int n1Data = x1Values.length;
    double[][] yValues = new double[n0Data][n1Data];

    for (int i = 0; i < n0Data; ++i) {
      for (int j = 0; j < n1Data; ++j) {
        yValues[i][j] = (x0Values[i] + 1.) * (x0Values[i] + 5.) * (x0Values[i] + 2.) * (x1Values[j] + 4.) * (x1Values[j] + 3.) * (x1Values[j] + 1.);
      }
    }

    PiecewisePolynomialInterpolator method = new NaturalSplineInterpolator();
    MeshgridInterpolator2D interp = new MeshgridInterpolator2D(method);
    PiecewisePolynomialFunction1D func = new PiecewisePolynomialFunction1D();

    final int n0Keys = 10 * n0Data + 1;
    final int n1Keys = 10 * n1Data + 1;
    final double eps = 1.e-6;
    double[] x0Keys = new double[n0Keys];
    double[] x1Keys = new double[n1Keys];
    double[] x0KeysUp = new double[n0Keys];
    double[] x1KeysUp = new double[n1Keys];
    double[] x0KeysDown = new double[n0Keys];
    double[] x1KeysDown = new double[n1Keys];
    for (int i = 0; i < n0Keys; ++i) {
      x0Keys[i] = x0Values[0] + (x0Values[n0Data - 1] - x0Values[0]) / (n0Keys - 1) * i;
      x0KeysUp[i] = x0Keys[i] == 0. ? eps : x0Keys[i] * (1. + eps);
      x0KeysDown[i] = x0Keys[i] == 0. ? -eps : x0Keys[i] * (1. - eps);
    }
    for (int i = 0; i < n1Keys; ++i) {
      x1Keys[i] = x1Values[0] + (x1Values[n1Data - 1] - x1Values[0]) / (n1Keys - 1) * i;
      x1KeysUp[i] = x1Keys[i] == 0. ? eps : x1Keys[i] * (1. + eps);
      x1KeysDown[i] = x1Keys[i] == 0. ? -eps : x1Keys[i] * (1. - eps);
    }

    final double[][] knotsRes = interp.interpolate(x0Values, x1Values, yValues, x0Values, x1Values).getData();

    final double[][] res = interp.interpolate(x0Values, x1Values, yValues, x0Keys, x1Keys).getData();
    final double[][] resX0 = interp.differentiateX0(x0Values, x1Values, yValues, x0Keys, x1Keys).getData();
    final double[][] resX1 = interp.differentiateX1(x0Values, x1Values, yValues, x0Keys, x1Keys).getData();
    final double[][] resX0Twice = interp.differentiateX0Twice(x0Values, x1Values, yValues, x0Keys, x1Keys).getData();
    final double[][] resX1Twice = interp.differentiateX1Twice(x0Values, x1Values, yValues, x0Keys, x1Keys).getData();

    final double[][] resTran = OG_ALGEBRA.getTranspose(interp.interpolate(x1Values, x0Values, OG_ALGEBRA.getTranspose(new DoubleMatrix2D(yValues)).getData(), x1Keys, x0Keys)).getData();
    final double[][] resX0Tran = OG_ALGEBRA.getTranspose(interp.differentiateX1(x1Values, x0Values, OG_ALGEBRA.getTranspose(new DoubleMatrix2D(yValues)).getData(), x1Keys, x0Keys)).getData();
    final double[][] resX1Tran = OG_ALGEBRA.getTranspose(interp.differentiateX0(x1Values, x0Values, OG_ALGEBRA.getTranspose(new DoubleMatrix2D(yValues)).getData(), x1Keys, x0Keys)).getData();
    final double[][] resX0TwiceTran = OG_ALGEBRA.getTranspose(interp.differentiateX1Twice(x1Values, x0Values, OG_ALGEBRA.getTranspose(new DoubleMatrix2D(yValues)).getData(), x1Keys, x0Keys))
        .getData();
    final double[][] resX1TwiceTran = OG_ALGEBRA.getTranspose(interp.differentiateX0Twice(x1Values, x0Values, OG_ALGEBRA.getTranspose(new DoubleMatrix2D(yValues)).getData(), x1Keys, x0Keys))
        .getData();

    for (int i = 0; i < n0Data; ++i) {
      for (int j = 0; j < n1Data; ++j) {
        double ref = Math.abs(yValues[i][j]) < 0.1 * EPS ? 0.1 : Math.abs(yValues[i][j]);
        assertEquals(knotsRes[i][j], yValues[i][j], EPS * ref);
      }
    }
    {
      double ref = Math.abs(yValues[1][2]) < 0.1 * EPS ? 0.1 : Math.abs(yValues[1][2]);
      assertEquals(interp.interpolate(x0Values, x1Values, yValues, x0Values[1], x1Values[2]), yValues[1][2], EPS * ref);
    }
    for (int i = 0; i < n0Keys; ++i) {
      for (int j = 0; j < n1Keys; ++j) {
        double ref = Math.abs(res[i][j]) < 0.1 * EPS ? 0.1 : Math.abs(res[i][j]);
        assertEquals(res[i][j], resTran[i][j], EPS * ref);
        ref = Math.abs(resX0[i][j]) < 0.1 * EPS ? 0.1 : Math.abs(resX0[i][j]);
        assertEquals(resX0[i][j], resX0Tran[i][j], EPS * ref);
        ref = Math.abs(resX1[i][j]) < 0.1 * EPS ? 0.1 : Math.abs(resX1[i][j]);
        assertEquals(resX1[i][j], resX1Tran[i][j], EPS * ref);
        ref = Math.abs(resX0Twice[i][j]) < 0.1 * EPS ? 0.1 : Math.abs(resX0Twice[i][j]);
        assertEquals(resX0Twice[i][j], resX0TwiceTran[i][j], EPS * ref);
        ref = Math.abs(resX1Twice[i][j]) < 0.1 * EPS ? 0.1 : Math.abs(resX1Twice[i][j]);
        assertEquals(resX1Twice[i][j], resX1TwiceTran[i][j], EPS * ref);
      }
    }
    {
      final double val = func.differentiate(method.interpolate(x1Values, yValues[1]), x1Keys[2]).getData()[0];
      final double resVal = interp.differentiateX1(x0Values, x1Values, yValues, x0Values[1], x1Keys[2]);
      final double ref = Math.abs(val) < 0.1 * EPS ? 0.1 : Math.abs(val);
      assertEquals(resVal, val, EPS * ref);
    }
    {
      final double val = func.differentiate(method.interpolate(x0Values, OG_ALGEBRA.getTranspose(new DoubleMatrix2D(yValues)).getData()[1]), x0Keys[2]).getData()[0];
      final double resVal = interp.differentiateX0(x0Values, x1Values, yValues, x0Keys[2], x1Values[1]);
      final double ref = Math.abs(val) < 0.1 * EPS ? 0.1 : Math.abs(val);
      assertEquals(resVal, val, EPS * ref);
    }
    {
      final double val = func.differentiateTwice(method.interpolate(x1Values, yValues[1]), x1Keys[2]).getData()[0];
      final double resVal = interp.differentiateX1Twice(x0Values, x1Values, yValues, x0Values[1], x1Keys[2]);
      final double ref = Math.abs(val) < 0.1 * EPS ? 0.1 : Math.abs(val);
      assertEquals(resVal, val, EPS * ref);
    }
    {
      final double val = func.differentiateTwice(method.interpolate(x0Values, OG_ALGEBRA.getTranspose(new DoubleMatrix2D(yValues)).getData()[1]), x0Keys[2]).getData()[0];
      final double resVal = interp.differentiateX0Twice(x0Values, x1Values, yValues, x0Keys[2], x1Values[1]);
      final double ref = Math.abs(val) < 0.1 * EPS ? 0.1 : Math.abs(val);
      assertEquals(resVal, val, EPS * ref);
    }

  }

  /**
   * 
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void notTwoMethodsTest() {
    double[] x0Values = new double[] {0., 1., 2., 3. };
    double[] x1Values = new double[] {0., 1., 2. };
    double[][] yValues = new double[][] { {1., 2., 4. }, {-1., 2., -4. }, {2., 3., 4. }, {5., 2., 1. } };

    MeshgridInterpolator2D interp = new MeshgridInterpolator2D(new PiecewisePolynomialInterpolator[] {new CubicSplineInterpolator() });
    interp.interpolate(x0Values, x1Values, yValues, x0Values, x1Values);
  }
}
