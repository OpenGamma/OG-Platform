/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * Solves cubic spline problem with natural endpoint conditions, where the second derivative at the endpoints is 0
 */
public class CubicSplineNaturalSolver extends CubicSplineSolver {

  @Override
  public DoubleMatrix2D solve(final double[] xValues, final double[] yValues) {
    final double[] intervals = getDiffs(xValues);
    return getCommonSplineCoeffs(xValues, yValues, intervals, matrixEqnSolver(getMatrix(intervals), getCommonVectorElements(yValues, intervals)));
  }

  @Override
  public DoubleMatrix2D[] solveWithSensitivity(final double[] xValues, final double[] yValues) {
    final double[] intervals = getDiffs(xValues);
    final double[][] toBeInv = getMatrix(intervals);
    final double[] commonVector = getCommonVectorElements(yValues, intervals);
    final double[][] commonVecSensitivity = getCommonVectorSensitivity(intervals);

    return getCommonCoefficientWithSensitivity(xValues, yValues, intervals, toBeInv, commonVector, commonVecSensitivity);
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

  //  /**
  //   * @param xValues X values of Data
  //   * @param yValues Y values of Data
  //   * @param intervals {xValues[1]-xValues[0], xValues[2]-xValues[1],...}
  //   * @param solnVector Values of second derivative at knots
  //   * @return Coefficient matrix whose i-th row vector is (a_0,a_1,...) for i-th intervals, where a_0,a_1,... are coefficients of f(x) = a_0 + a_1 x^1 + ....
  //   */
  //  private DoubleMatrix2D getSplineCoeffs(final double[] xValues, final double[] yValues, final double[] intervals, final double[] solnVector) {
  //
  //    final int nData = xValues.length;
  //
  //    if (nData == 2) {
  //      final double[][] res = new double[][] {{
  //          yValues[1] / intervals[0] - yValues[0] / intervals[0] - intervals[0] * solnVector[0] / 2. - intervals[0] * solnVector[1] / 6. + intervals[0] * solnVector[0] / 6., yValues[0] } };
  //      return new DoubleMatrix2D(res);
  //    }
  //    if (nData == 3) {
  //      final double[][] res = new double[][] {{solnVector[0] / 2., yValues[1] / intervals[0] - yValues[0] / intervals[0] - intervals[0] * solnVector[0] / 2., yValues[0] } };
  //      return new DoubleMatrix2D(res);
  //    } else {
  //      return getCommonSplineCoeffs(xValues, yValues, intervals, solnVector);
  //    }
  //  }

  /**
   * Cubic spline is obtained by solving a linear problem Ax=b where A is a square matrix and x,b are vector
   * @param intervals {xValues[1]-xValues[0], xValues[2]-xValues[1],...}
   * @return Matrix A
   */
  private double[][] getMatrix(final double[] intervals) {

    final int nData = intervals.length + 1;
    double[][] res = new double[nData][nData];

    res = getCommonMatrixElements(intervals);
    res[0][0] = 1.;
    res[nData - 1][nData - 1] = 1.;

    return res;
  }
}
