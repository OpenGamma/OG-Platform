/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import java.util.Arrays;

import org.apache.commons.lang.NotImplementedException;

import com.google.common.primitives.Doubles;
import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ParallelArrayBinarySort;

/**
 * C1 cubic interpolation preserving monotonicity based on 
 * Fritsch, F. N.; Carlson, R. E. (1980) 
 * "Monotone Piecewise Cubic Interpolation", SIAM Journal on Numerical Analysis 17 (2): 238–246. 
 * Fritsch, F. N. and Butland, J. (1984)
 * "A method for constructing local monotone piecewise cubic interpolants", SIAM Journal on Scientific and Statistical Computing 5 (2): 300-304.
 */
public class PiecewiseCubicHermiteSplineInterpolatorWithSensitivity extends PiecewisePolynomialInterpolator {

  @Override
  public PiecewisePolynomialResultsWithSensitivity interpolate(final double[] xValues, final double[] yValues) {

    ArgumentChecker.notNull(xValues, "xValues");
    ArgumentChecker.notNull(yValues, "yValues");

    ArgumentChecker.isTrue(xValues.length == yValues.length, "xValues length = yValues length");
    ArgumentChecker.isTrue(xValues.length > 1, "Data points should be more than 1");

    final int nDataPts = xValues.length;

    for (int i = 0; i < nDataPts; ++i) {
      ArgumentChecker.isFalse(Double.isNaN(xValues[i]), "xData containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(xValues[i]), "xData containing Infinity");
      ArgumentChecker.isFalse(Double.isNaN(yValues[i]), "yData containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(yValues[i]), "yData containing Infinity");
    }

    double[] xValuesSrt = Arrays.copyOf(xValues, nDataPts);
    double[] yValuesSrt = Arrays.copyOf(yValues, nDataPts);
    ParallelArrayBinarySort.parallelBinarySort(xValuesSrt, yValuesSrt);

    for (int i = 1; i < nDataPts; ++i) {
      ArgumentChecker.isFalse(xValuesSrt[i - 1] == xValuesSrt[i], "xValues should be distinct");
    }

    final DoubleMatrix2D[] temp = solve(xValuesSrt, yValuesSrt);

    // check the matrices
    // TODO remove some of these tests
    ArgumentChecker.noNulls(temp, "error in solve - some matrices are null");
    int n = temp.length;
    ArgumentChecker.isTrue(n == nDataPts, "wrong number of matricies");
    for (int k = 0; k < n; k++) {
      DoubleMatrix2D m = temp[k];
      final int rows = m.getNumberOfRows();
      final int cols = m.getNumberOfColumns();
      for (int i = 0; i < rows; ++i) {
        for (int j = 0; j < cols; ++j) {
          ArgumentChecker.isTrue(Doubles.isFinite(m.getEntry(i, j)), "Matrix contains a NaN or infinite");
        }
      }
    }

    DoubleMatrix2D coefMatrix = temp[0];
    DoubleMatrix2D[] coefMatrixSense = new DoubleMatrix2D[n - 1];
    System.arraycopy(temp, 1, coefMatrixSense, 0, n - 1);

    return new PiecewisePolynomialResultsWithSensitivity(new DoubleMatrix1D(xValuesSrt), coefMatrix, nDataPts, 1, coefMatrixSense);
  }

  /**
   * @param xValues X values of data
   * @param yValues Y values of data
   * @return Coefficient matrix whose i-th row vector is {a3, a2, a1, a0} of f(x) = a3 * (x-x_i)^3 + a2 * (x-x_i)^2 +... for the i-th interval
   */
  private DoubleMatrix2D[] solve(final double[] xValues, final double[] yValues) {

    final int n = xValues.length;

    double[][] coeff = new double[n - 1][4];
    double[] h = new double[n - 1];
    double[] delta = new double[n - 1];
    DoubleMatrix2D[] res = new DoubleMatrix2D[n];

    for (int i = 0; i < n - 1; ++i) {
      h[i] = xValues[i + 1] - xValues[i];
      delta[i] = (yValues[i + 1] - yValues[i]) / h[i];
    }

    if (n == 2) {
      // TODO check this - should be yValues
      coeff[0][2] = delta[0];
      coeff[0][3] = xValues[0];
    } else {
      double[][] temp = slopeFinder(h, delta);
      double[] d = temp[0];
      double[][] dDDelta = new double[n - 1][n];
      System.arraycopy(temp, 1, dDDelta, 0, n - 1);
      // the i,j element of dDDelta is d(d_j)/d(delta_i) -i.e. it is transposed

      for (int i = 0; i < n - 1; ++i) {
        coeff[i][0] = (d[i] - 2 * delta[i] + d[i + 1]) / h[i] / h[i]; // b
        coeff[i][1] = (3 * delta[i] - 2. * d[i] - d[i + 1]) / h[i]; // c
        coeff[i][2] = d[i];
        coeff[i][3] = yValues[i];
      }

      // TODO this would all be a lot nicer if we had multiplication of sparse matrices
      double[][] dDy = new double[n][];
      for (int i = 0; i < n; i++) {
        final double[] vec = new double[n];
        vec[0] = -dDDelta[0][i] / h[0];
        vec[n - 1] = dDDelta[n - 2][i] / h[n - 2];
        for (int j = 1; j < n - 1; j++) {
          vec[j] = -dDDelta[j][i] / h[j] + dDDelta[j - 1][i] / h[j - 1];
        }
        dDy[i] = vec;
      }

      double[][] bDy = new double[n - 1][n];
      double[][] cDy = new double[n - 1][n];

      for (int i = 0; i < n - 1; i++) {
        final double invH = 1 / h[i];
        final double invH2 = invH * invH;
        final double invH3 = invH * invH2;
        cDy[i][i] = -3 * invH2;
        cDy[i][i + 1] = 3 * invH2;
        bDy[i][i] = 2 * invH3;
        bDy[i][i + 1] = -2 * invH3;
        for (int j = 0; j < n; j++) {
          cDy[i][j] -= (2 * dDy[i][j] + dDy[i + 1][j]) * invH;
          bDy[i][j] += (dDy[i][j] + dDy[i + 1][j]) * invH2;
        }
      }

      // Now we have to pack this into an array of DoubleMatrix2D - my kingdom for a tensor class
      res[0] = new DoubleMatrix2D(coeff);
      for (int k = 0; k < n - 1; k++) {
        double[][] coeffSense = new double[4][];
        coeffSense[0] = bDy[k];
        coeffSense[1] = cDy[k];
        coeffSense[2] = dDy[k];
        coeffSense[3] = new double[n];
        coeffSense[3][k] = 1.0;
        res[k + 1] = new DoubleMatrix2D(coeffSense);
      }

    }
    return res;
  }

  /**
   * Finds the the first derivatives at knots and their sensitivity to delta
   * @param h 
   * @param delta 
   * @return An array of arrays - the first row is the first derivatives at knots (d), while the remaining rows are the sensitivity to delta, so the ith row is the 
   * of d to the (i-1)th delta
   */
  private double[][] slopeFinder(final double[] h, final double[] delta) {
    final int n = h.length + 1;
    // TODO it would be better if this were a sparse matrix
    double[][] res = new double[n][n];

    // internal points
    for (int i = 1; i < n - 1; ++i) {
      if (delta[i] * delta[i - 1] > 0.) {
        final double w1 = 2. * h[i] + h[i - 1];
        final double w2 = h[i] + 2. * h[i - 1];
        final double w12 = w1 + w2;
        final double d = w12 / (w1 / delta[i - 1] + w2 / delta[i]);
        res[0][i] = d;
        res[i][i] = w1 / (w12) * FunctionUtils.square(d / delta[i - 1]);
        res[i + 1][i] = w2 / (w12) * FunctionUtils.square(d / delta[i]);
      }
    }

    // fill in end points
    double[] temp = endpointSlope(h[0], h[1], delta[0], delta[1]);
    for (int i = 0; i < 3; i++) {
      res[i][0] = temp[i];
    }
    temp = endpointSlope(h[n - 2], h[n - 3], delta[n - 2], delta[n - 3]);
    res[0][n - 1] = temp[0];
    res[n - 1][n - 1] = temp[1];
    res[n - 2][n - 1] = temp[2];

    return res;
  }

  /**
   * First derivative at end point and its sensitivity to delta
   * @param h1
   * @param h2
   * @param del1
   * @param del2
   * @return array of length 3 - the first element contains d, while the other two are sensitivities 
   */
  private double[] endpointSlope(final double h1, final double h2, final double del1, final double del2) {

    final double d = ((2. * h1 + h2) * del1 - h1 * del2) / (h1 + h2);
    final double[] res = new double[3];
    if (Math.signum(d) != Math.signum(del1)) {
      return res;
    } else if (Math.signum(del1) != Math.signum(del2) && Math.abs(d) > 3. * Math.abs(del1)) {
      res[0] = 3 * del1;
      res[1] = 3;
    } else {
      res[0] = d;
      res[1] = (2 * h1 + h2) / (h1 + h2);
      res[2] = -h1 / (h1 + h2);
    }
    return res;
  }

  @Override
  public PiecewisePolynomialResult interpolate(double[] xValues, double[][] yValuesMatrix) {
    throw new NotImplementedException();
  }

}
