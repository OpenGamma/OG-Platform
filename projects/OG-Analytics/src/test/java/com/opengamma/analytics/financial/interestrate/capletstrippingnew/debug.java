/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import static org.apache.commons.math.util.MathUtils.binomialCoefficient;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;
import org.testng.annotations.Test;

import com.opengamma.analytics.math.interpolation.PSplineFitter;
import com.opengamma.analytics.math.matrix.ColtMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.DoubleMatrixUtils;
import com.opengamma.analytics.math.matrix.Matrix;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class debug {

  MatrixAlgebra MA = new ColtMatrixAlgebra();

  @Test
  public void test() {
    final PSplineFitter fitter = new PSplineFitter();
    final DoubleMatrix2D m1 = fitter.getPenaltyMatrix(new int[] {2, 2 }, 1, 0);
    System.out.println(m1);
    final DoubleMatrix2D m2 = fitter.getPenaltyMatrix(new int[] {2, 2 }, 1, 1);
    System.out.println(m2);
    final DoubleMatrix2D m3 = fitter.getPenaltyMatrix(5, 2);
    System.out.println(m3);

    final Matrix<?> m4 = MA.kroneckerProduct(DoubleMatrixUtils.getIdentityMatrix2D(2), m3);
    System.out.println(m4);

    final Matrix<?> m5 = MA.kroneckerProduct(m3, DoubleMatrixUtils.getIdentityMatrix2D(2));
    System.out.println(m5);
  }

  @Test
  public void test2() {
    final DoubleMatrix2D m0 = getDiffMatrix(5, 0);
    System.out.println(m0);
    final DoubleMatrix2D m1 = getDiffMatrix(5, 1);
    System.out.println(m1);
    final DoubleMatrix2D m2 = getDiffMatrix(5, 2);
    System.out.println(m2);

    final DoubleMatrix2D m2b = (DoubleMatrix2D) MA.multiply(m1, m1);
    System.out.println(m2b);

    final double[] x = new double[] {1, 2, 2.1, 4, 5 };
    final DoubleMatrix2D d = getDiffMatrix(x, 2);
    System.out.println(d);

    final double[] y = new double[5];
    for (int i = 0; i < 5; i++) {
      y[i] = 1.0 - 2.0 * x[i] + x[i] * x[i];
    }

    final DoubleMatrix1D yM = new DoubleMatrix1D(y);
    System.out.println(yM);
    final DoubleMatrix1D d1 = (DoubleMatrix1D) MA.multiply(d, yM);
    System.out.println(d1);

  }

  public DoubleMatrix2D getDiffMatrix(final int m, final int k) {
    Validate.isTrue(k < m, "differce order too high");

    if (m == 0) {
      return DoubleMatrixUtils.getIdentityMatrix2D(m);
    }

    final double[][] data = new double[m][m];
    final int[] coeff = new int[k + 1];

    int sign = 1;
    for (int i = k; i >= 0; i--) {
      coeff[i] = (int) (sign * binomialCoefficient(k, i));
      sign *= -1;
    }

    for (int i = k; i < m; i++) {
      for (int j = 0; j < k + 1; j++) {
        data[i][j + i - k] = coeff[j];
      }
    }
    return new DoubleMatrix2D(data);
  }

  @Test
  public void test3() {

    final double[] x = new double[] {1, 2, 2.1, 4, 5 };
    final double[] y = new double[] {0, 2, 4, 6 };

    final int nX = x.length;
    final int nY = y.length;
    final double[] z = new double[nX * nY];
    int count = 0;
    for (int i = 0; i < nX; i++) {
      for (int j = 0; j < nY; j++) {
        z[count++] = 2 + x[i] + 2 * y[j] + 1.5 * x[i] * x[i] + 3 * y[j] * y[j] - 1.5 * x[i] * y[j];
      }
    }

    final DoubleMatrix2D dx = getXDiffMatrix(x, y, 2, 2);
    System.out.println("size: " + dx.getNumberOfColumns());
    //  System.out.println(d);

    final DoubleMatrix1D zp1 = (DoubleMatrix1D) MA.multiply(dx, new DoubleMatrix1D(z));
    System.out.println(zp1);

    final DoubleMatrix2D dy = getYDiffMatrix(x, y, 2, 1);

    final DoubleMatrix1D zp2 = (DoubleMatrix1D) MA.multiply(dy, new DoubleMatrix1D(z));
    System.out.println(zp2);
  }

  public DoubleMatrix2D getXDiffMatrix(final double[] x, final double[] y, final int kx, final int ky) {
    final DoubleMatrix2D d = getDiffMatrix(x, kx);
    final DoubleMatrix2D iy = DoubleMatrixUtils.getIdentityMatrix2D(y.length);
    return (DoubleMatrix2D) MA.kroneckerProduct(d, iy);
  }

  public DoubleMatrix2D getYDiffMatrix(final double[] x, final double[] y, final int kx, final int ky) {
    final DoubleMatrix2D d = getDiffMatrix(y, ky);
    final DoubleMatrix2D ix = DoubleMatrixUtils.getIdentityMatrix2D(x.length);
    return (DoubleMatrix2D) MA.kroneckerProduct(ix, d);
  }

  public DoubleMatrix2D getDiffMatrix(final double[] x, final int k) {
    ArgumentChecker.notEmpty(x, "x");
    ArgumentChecker.notNegative(k, "k");
    final int size = x.length;
    ArgumentChecker.isTrue(k < size, "differce order too high");
    if (k == 0) {
      return DoubleMatrixUtils.getIdentityMatrix2D(size);
    } else if (k > 2) {
      throw new NotImplementedException("cannot handle order (k) > 2");
      //      final int kd2 = k / 2;
      //      final DoubleMatrix2D d2 = getDiffMatrix(x, 2);
      //      final DoubleMatrix2D d = MA.getPower(d2, kd2);
      //      if (k % 2 == 0) {
      //        return d;
      //      } else {
      //        final DoubleMatrix2D rem = getDiffMatrix(x, 1);
      //        return (DoubleMatrix2D) MA.multiply(rem, d);
      //      }

    } else {
      final double[] dx = new double[size - 1];
      final double[] dx2 = new double[size - 1];
      for (int i = 0; i < (size - 1); i++) {
        final double temp = x[i + 1] - x[i];
        ArgumentChecker.isTrue(temp > 0.0, "x not in ascending order, or two identical points");
        dx[i] = temp;
        dx2[i] = temp * temp;
      }
      final double[] w = new double[size - 2];
      for (int i = 0; i < (size - 2); i++) {
        w[i] = 1.0 / dx[i] / dx[i + 1] / (dx[i] + dx[i + 1]);
      }

      final DoubleMatrix2D res = new DoubleMatrix2D(size, size);
      final double[][] data = res.getData();

      if (k == 1) {
        for (int i = 1; i < (size - 1); i++) {
          data[i][i - 1] = -w[i - 1] * dx2[i];
          data[i][i] = w[i - 1] * (dx2[i] - dx2[i - 1]);
          data[i][i + 1] = w[i - 1] * dx2[i - 1];
        }
        //ends 
        data[0][0] = -w[0] * dx[1] * (2 * dx[0] + dx[1]);
        data[0][1] = w[0] * (dx2[0] + dx2[1] + 2 * dx[0] * dx[1]);
        data[0][2] = -w[0] * dx2[0];
        data[size - 1][size - 3] = w[size - 3] * dx2[size - 2];
        data[size - 1][size - 2] = -w[size - 3] * (dx2[size - 3] + dx2[size - 2] + 2 * dx[size - 2] * dx[size - 3]);
        data[size - 1][size - 1] = w[size - 3] * dx[size - 3] * (2 * dx[size - 2] + dx[size - 3]);
        return res;
      } else {
        for (int i = 1; i < (size - 1); i++) {
          data[i][i - 1] = 2 * w[i - 1] * dx[i];
          data[i][i] = -2 * w[i - 1] * (dx[i] + dx[i - 1]);
          data[i][i + 1] = 2 * w[i - 1] * dx[i - 1];
        }
        //ends 
        data[0] = data[1];
        data[size - 1] = data[size - 2];
        return res;
      }
    }
  }
}
