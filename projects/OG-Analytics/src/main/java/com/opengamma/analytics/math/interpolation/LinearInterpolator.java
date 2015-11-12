/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import java.util.Arrays;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ParallelArrayBinarySort;

/**
 * Interpolate consecutive two points by a straight line
 * Note that this interpolator is NOT included in {@link Interpolator1DFactory} 
 * Use {@link LinearInterpolator1D} for node sensitivity
 */
public class LinearInterpolator extends PiecewisePolynomialInterpolator {

  private static final double ERROR = 1.e-13;

  @Override
  public PiecewisePolynomialResult interpolate(final double[] xValues, final double[] yValues) {

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

    for (int i = 0; i < nDataPts; ++i) {
      for (int j = i + 1; j < nDataPts; ++j) {
        ArgumentChecker.isFalse(xValues[i] == xValues[j], "xValues should be distinct");
      }
    }

    double[] xValuesSrt = Arrays.copyOf(xValues, nDataPts);
    double[] yValuesSrt = Arrays.copyOf(yValues, nDataPts);
    ParallelArrayBinarySort.parallelBinarySort(xValuesSrt, yValuesSrt);

    final DoubleMatrix2D coefMatrix = solve(xValuesSrt, yValuesSrt);

    for (int i = 0; i < coefMatrix.getNumberOfRows(); ++i) {
      for (int j = 0; j < coefMatrix.getNumberOfColumns(); ++j) {
        ArgumentChecker.isFalse(Double.isNaN(coefMatrix.getData()[i][j]), "Too large input");
        ArgumentChecker.isFalse(Double.isInfinite(coefMatrix.getData()[i][j]), "Too large input");
      }
      double ref = 0.;
      final double interval = xValuesSrt[i + 1] - xValuesSrt[i];
      for (int j = 0; j < 2; ++j) {
        ref += coefMatrix.getData()[i][j] * Math.pow(interval, 1 - j);
        ArgumentChecker.isFalse(Double.isNaN(coefMatrix.getData()[i][j]), "Too large input");
        ArgumentChecker.isFalse(Double.isInfinite(coefMatrix.getData()[i][j]), "Too large input");
      }
      final double bound = Math.max(Math.abs(ref) + Math.abs(yValuesSrt[i + 1]), 1.e-1);
      ArgumentChecker.isTrue(Math.abs(ref - yValuesSrt[i + 1]) < ERROR * bound, "Input is too large/small or data are not distinct enough");
    }

    return new PiecewisePolynomialResult(new DoubleMatrix1D(xValuesSrt), coefMatrix, coefMatrix.getNumberOfColumns(), 1);
  }

  @Override
  public PiecewisePolynomialResult interpolate(final double[] xValues, final double[][] yValuesMatrix) {

    ArgumentChecker.notNull(xValues, "xValues");
    ArgumentChecker.notNull(yValuesMatrix, "yValuesMatrix");

    ArgumentChecker.isTrue(xValues.length == yValuesMatrix[0].length, "(xValues length = yValuesMatrix's row vector length)");
    ArgumentChecker.isTrue(xValues.length > 1, "Data points should be more than 1");

    final int nDataPts = xValues.length;
    final int dim = yValuesMatrix.length;

    for (int i = 0; i < nDataPts; ++i) {
      ArgumentChecker.isFalse(Double.isNaN(xValues[i]), "xData containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(xValues[i]), "xData containing Infinity");
      for (int j = 0; j < dim; ++j) {
        ArgumentChecker.isFalse(Double.isNaN(yValuesMatrix[j][i]), "yValuesMatrix containing NaN");
        ArgumentChecker.isFalse(Double.isInfinite(yValuesMatrix[j][i]), "yValuesMatrix containing Infinity");
      }
    }

    for (int k = 0; k < dim; ++k) {
      for (int i = 0; i < nDataPts; ++i) {
        for (int j = i + 1; j < nDataPts; ++j) {
          ArgumentChecker.isFalse(xValues[i] == xValues[j], "xValues should be distinct");
        }
      }
    }

    double[] xValuesSrt = new double[nDataPts];
    DoubleMatrix2D[] coefMatrix = new DoubleMatrix2D[dim];

    for (int i = 0; i < dim; ++i) {
      xValuesSrt = Arrays.copyOf(xValues, nDataPts);
      double[] yValuesSrt = Arrays.copyOf(yValuesMatrix[i], nDataPts);
      ParallelArrayBinarySort.parallelBinarySort(xValuesSrt, yValuesSrt);

      coefMatrix[i] = solve(xValuesSrt, yValuesSrt);

      for (int k = 0; k < xValuesSrt.length - 1; ++k) {
        double ref = 0.;
        final double interval = xValuesSrt[k + 1] - xValuesSrt[k];
        for (int j = 0; j < 2; ++j) {
          ref += coefMatrix[i].getData()[k][j] * Math.pow(interval, 1 - j);
          ArgumentChecker.isFalse(Double.isNaN(coefMatrix[i].getData()[k][j]), "Too large input");
          ArgumentChecker.isFalse(Double.isInfinite(coefMatrix[i].getData()[k][j]), "Too large input");
        }
        final double bound = Math.max(Math.abs(ref) + Math.abs(yValuesSrt[k + 1]), 1.e-1);
        ArgumentChecker.isTrue(Math.abs(ref - yValuesSrt[k + 1]) < ERROR * bound, "Input is too large/small or data points are too close");
      }
    }

    final int nIntervals = coefMatrix[0].getNumberOfRows();
    final int nCoefs = coefMatrix[0].getNumberOfColumns();
    double[][] resMatrix = new double[dim * nIntervals][nCoefs];

    for (int i = 0; i < nIntervals; ++i) {
      for (int j = 0; j < dim; ++j) {
        resMatrix[dim * i + j] = coefMatrix[j].getRowVector(i, false).getData();
      }
    }

    return new PiecewisePolynomialResult(new DoubleMatrix1D(xValuesSrt, false), DoubleMatrix2D.noCopy(resMatrix), nCoefs, dim);
  }

  @Override
  public PiecewisePolynomialResultsWithSensitivity interpolateWithSensitivity(final double[] xValues, final double[] yValues) {
    throw new NotImplementedException("Use LinearInterpolator1D for node sensitivity");
  }

  /**
   * @param xValues X values of data
   * @param yValues Y values of data
   * @return Coefficient matrix whose i-th row vector is {a1, a0} of f(x) = a1 * (x-x_i) + a0 for the i-th interval
   */
  private DoubleMatrix2D solve(final double[] xValues, final double[] yValues) {

    final int nDataPts = xValues.length;

    double[][] res = new double[nDataPts - 1][2];

    for (int i = 0; i < nDataPts - 1; ++i) {
      res[i][1] = yValues[i];
      res[i][0] = (yValues[i + 1] - yValues[i]) / (xValues[i + 1] - xValues[i]);
    }

    return new DoubleMatrix2D(res);
  }

}
