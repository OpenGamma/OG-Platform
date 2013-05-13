/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;

import com.opengamma.analytics.math.function.PiecewisePolynomialFunction1D;
import com.opengamma.analytics.math.function.PiecewisePolynomialWithSensitivityFunction1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;

/**
 * 
 */
public class PiecewiseCubicHermiteSplineInterpolatorWithSensitivityTest {

  private final static MatrixAlgebra MA = new OGMatrixAlgebra();
  private final static PiecewiseCubicHermiteSplineInterpolator PCHIP = new PiecewiseCubicHermiteSplineInterpolator();
  private final static PiecewiseCubicHermiteSplineInterpolatorWithSensitivity PCHIP_S = new PiecewiseCubicHermiteSplineInterpolatorWithSensitivity();
  private final static PiecewisePolynomialFunction1D PPVAL = new PiecewisePolynomialFunction1D();
  private final static PiecewisePolynomialWithSensitivityFunction1D PPVAL_S = new PiecewisePolynomialWithSensitivityFunction1D();
  private final static double[] X = new double[] {0, 0.5, 1.0, 2.0, 3.0, 5.0};
  private final static double[] Y = new double[] {1.2200, 0.8900, 0.7800, 1.0000, 1.5000, 1.2000};
  private final static double[] XX;
  private final static double[] YY;

  static {
    XX = new double[66];
    for (int i = 0; i < 66; i++) {
      XX[i] = -0.5 + 0.1 * i;
    }
    // number from external PCHIP
    YY = new double[] {1.66, 1.57904, 1.49192, 1.40128, 1.30976, 1.22, 1.13464, 1.05632, 0.98768, 0.93136, 0.89, 0.85744, 0.82752, 0.80288, 0.78616, 0.78, 0.78341, 0.793102222222222, 0.80827,
        0.828106666666667, 0.851805555555556, 0.87856, 0.907563333333333, 0.938008888888889, 0.96909, 1, 1.03875, 1.09111111111111, 1.15291666666666, 1.22, 1.28819444444444, 1.35333333333333,
        1.41125, 1.45777777777777, 1.48875, 1.5, 1.4999625, 1.4997, 1.4989875, 1.4976, 1.4953125, 1.4919, 1.4871375, 1.4808, 1.4726625, 1.4625, 1.4500875, 1.4352, 1.4176125, 1.3971, 1.3734375,
        1.3464, 1.3157625, 1.2813, 1.2427875, 1.2, 1.1527125, 1.1007, 1.0437375, 0.9816, 0.9140625, 0.8409, 0.7618875, 0.6768, 0.5854125, 0.4875};
  }

  @Test
  public void baseInterpolationTest() {
    final int n = XX.length;
    PiecewisePolynomialResult pp = PCHIP.interpolate(X, Y);

    for (int i = 0; i < n; i++) {
      final double y = PPVAL.evaluate(pp, XX[i]).getEntry(0);
      assertEquals(YY[i], y, 1e-14);
    }
  }

  @Test
  public void interpolationTest() {
    final int n = XX.length;
    PiecewisePolynomialResult pp = PCHIP_S.interpolate(X, Y);

    for (int i = 0; i < n; i++) {
      final double y = PPVAL_S.evaluate(pp, XX[i]).getEntry(0);
      // System.out.println(XX[i]+"\t"+y);
      assertEquals("index:" + i, YY[i], y, 1e-14);
    }
  }

  @Test(enabled=false)
  public void sensitivityTest() {
    final int n = XX.length;
    final int nData = X.length;
    PiecewisePolynomialResultsWithSensitivity pp = PCHIP_S.interpolate(X, Y);

    DoubleMatrix1D[] fdRes = fdSenseCal(XX);

    for (int i = 0; i < n; i++) {
      DoubleMatrix1D res = PPVAL_S.nodeSensitivity(pp, XX[i]);
      for (int j = 0; j < nData; j++) {
        System.out.print(res.getEntry(j) + ", ");
     //   System.out.print(fdRes[j].getEntry(i) + ", ");
      }
      System.out.println();
    }
  }

  private DoubleMatrix1D[] fdSenseCal(final double[] x) {
    final int nData = Y.length;

    final double eps = 1e-5;
    final double scale = 0.5 / eps;
    final DoubleMatrix1D[] res = new DoubleMatrix1D[nData];
    double[] temp = new double[nData];
    PiecewisePolynomialResult pp;
    for (int i = 0; i < nData; i++) {
      System.arraycopy(Y, 0, temp, 0, nData);
      temp[i] += eps;
      pp = PCHIP.interpolate(X, temp);
      final DoubleMatrix1D yUp = PPVAL.evaluate(pp, x).getRowVector(0);
      temp[i] -= 2 * eps;
      pp = PCHIP.interpolate(X, temp);
      final DoubleMatrix1D yDown = PPVAL.evaluate(pp, x).getRowVector(0);
      res[i] = (DoubleMatrix1D) MA.scale(MA.subtract(yUp, yDown), scale);
    }
    return res;
  }

}
