/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import java.util.Arrays;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * Solves cubic spline problem with Not-A-Knot endpoint conditions, where the third derivative at the endpoints is the same as that of their adjacent points
 */
public class CubicSplineNakSolver extends CubicSplineSolver {

  @Override
  public DoubleMatrix2D solve(final double[] xValues, final double[] yValues) {

    final double[] intervals = getDiffs(xValues);

    return getSplineCoeffs(xValues, yValues, intervals, matrixEqnSolver(getMatrix(intervals), getVector(yValues, intervals)));
  }

  @Override
  public DoubleMatrix2D[] solveMultiDim(final double[] xValues, final DoubleMatrix2D yValuesMatrix) {
    final int dim = yValuesMatrix.getNumberOfRows();
    DoubleMatrix2D[] coefMatrix = new DoubleMatrix2D[dim];

    for (int i = 0; i < dim; ++i) {
      coefMatrix[i] = solve(xValues, yValuesMatrix.getRowVector(i).getData());
    }

    return coefMatrix;
  }

  @Override
  public DoubleMatrix1D getKnotsMat1D(final double[] xValues) {
    final int nData = xValues.length;
    if (nData == 2) {
      return new DoubleMatrix1D(new double[] {xValues[0], xValues[nData - 1] });
    }
    if (nData == 3) {
      return new DoubleMatrix1D(new double[] {xValues[0], xValues[nData - 1] });
    } else {
      return new DoubleMatrix1D(xValues);
    }

  }

  /**
   * @param xValues X values of Data
   * @param yValues Y values of Data
   * @param intervals {xValues[1]-xValues[0], xValues[2]-xValues[1],...}
   * @param solnVector Values of second derivative at knots
   * @return Coefficient matrix whose i-th row vector is (a_0,a_1,...) for i-th intervals, where a_0,a_1,... are coefficients of f(x) = a_0 + a_1 x^1 + ....
   */
  private DoubleMatrix2D getSplineCoeffs(final double[] xValues, final double[] yValues, final double[] intervals, final double[] solnVector) {

    final int nData = xValues.length;

    if (nData == 2) {
      final double[][] res = new double[][] {{
          yValues[1] / intervals[0] - yValues[0] / intervals[0] - intervals[0] * solnVector[0] / 2. - intervals[0] * solnVector[1] / 6. + intervals[0] * solnVector[0] / 6., yValues[0] } };
      return new DoubleMatrix2D(res);
    }
    if (nData == 3) {
      final double[][] res = new double[][] {{solnVector[0] / 2., yValues[1] / intervals[0] - yValues[0] / intervals[0] - intervals[0] * solnVector[0] / 2., yValues[0] } };
      return new DoubleMatrix2D(res);
    } else {
      return getCommonSplineCoeffs(xValues, yValues, intervals, solnVector);
    }
  }

  /**
   * Cubic spline is obtained by solving a linear problem Ax=b where A is a square matrix and x,b are vector
   * @param yValues Y Values of data
   * @param intervals {xValues[1]-xValues[0], xValues[2]-xValues[1],...}
   * @return Vector b
   */
  private double[] getVector(final double[] yValues, final double[] intervals) {

    final int nData = yValues.length;
    double[] res = new double[nData];

    if (nData == 3) {
      for (int i = 0; i < nData; ++i) {
        res[i] = 2. * yValues[2] / (intervals[0] + intervals[1]) - 2. * yValues[0] / (intervals[0] + intervals[1]) - 2. * yValues[1] / (intervals[0]) + 2. * yValues[0] / (intervals[0]);
      }
    } else {
      res = getCommonVectorElements(yValues, intervals);
    }
    return res;
  }

  /**
   * Cubic spline is obtained by solving a linear problem Ax=b where A is a square matrix and x,b are vector
   * @param intervals {xValues[1]-xValues[0], xValues[2]-xValues[1],...}
   * @return Matrix A
   */
  private double[][] getMatrix(final double[] intervals) {

    final int nData = intervals.length + 1;
    double[][] res = new double[nData][nData];

    for (int i = 0; i < nData; ++i) {
      Arrays.fill(res[i], 0.);
    }

    if (nData == 2) {
      res[0][1] = intervals[0];
      res[1][0] = intervals[0];
      return res;
    } else {
      if (nData == 3) {
        res[0][0] = intervals[1];
        res[1][1] = intervals[1];
        res[2][2] = intervals[1];
        return res;
      } else {
        res = getCommonMatrixElements(intervals);
        res[0][0] = -intervals[1];
        res[0][1] = intervals[0] + intervals[1];
        res[0][2] = -intervals[0];
        res[nData - 1][nData - 3] = -intervals[nData - 2];
        res[nData - 1][nData - 2] = intervals[nData - 3] + intervals[nData - 2];
        res[nData - 1][nData - 1] = -intervals[nData - 3];
        return res;
      }
    }

  }

}
