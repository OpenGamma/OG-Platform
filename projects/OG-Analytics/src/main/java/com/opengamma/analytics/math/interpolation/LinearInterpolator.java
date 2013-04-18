/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import java.util.Arrays;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class LinearInterpolator extends PiecewisePolynomialInterpolator {

  private double[] _xValuesSrt;
  private double[] _yValuesSrt;

  /**
   * 
   */
  public LinearInterpolator() {
    _xValuesSrt = null;
    _yValuesSrt = null;

  }

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

    _xValuesSrt = Arrays.copyOf(xValues, nDataPts);
    _yValuesSrt = Arrays.copyOf(yValues, nDataPts);

    parallelBinarySort(nDataPts);

    final DoubleMatrix2D coefMatrix = solve(_xValuesSrt, _yValuesSrt);

    for (int i = 0; i < coefMatrix.getNumberOfRows(); ++i) {
      for (int j = 0; j < coefMatrix.getNumberOfColumns(); ++j) {
        ArgumentChecker.isFalse(Double.isNaN(coefMatrix.getData()[i][j]), "Too large input");
        ArgumentChecker.isFalse(Double.isInfinite(coefMatrix.getData()[i][j]), "Too large input");
      }
    }

    return new PiecewisePolynomialResult(new DoubleMatrix1D(_xValuesSrt), coefMatrix, coefMatrix.getNumberOfColumns(), 1);
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

    _xValuesSrt = new double[nDataPts];
    DoubleMatrix2D[] coefMatrix = new DoubleMatrix2D[dim];

    for (int i = 0; i < dim; ++i) {
      _xValuesSrt = Arrays.copyOf(xValues, nDataPts);
      _yValuesSrt = Arrays.copyOf(yValuesMatrix[i], nDataPts);

      parallelBinarySort(nDataPts);

      coefMatrix[i] = solve(_xValuesSrt, _yValuesSrt);
    }

    final int nIntervals = coefMatrix[0].getNumberOfRows();
    final int nCoefs = coefMatrix[0].getNumberOfColumns();
    double[][] resMatrix = new double[dim * nIntervals][nCoefs];

    for (int i = 0; i < nIntervals; ++i) {
      for (int j = 0; j < dim; ++j) {
        resMatrix[dim * i + j] = coefMatrix[j].getRowVector(i).getData();
      }
    }

    for (int i = 0; i < (nIntervals * dim); ++i) {
      for (int j = 0; j < nCoefs; ++j) {
        ArgumentChecker.isFalse(Double.isNaN(resMatrix[i][j]), "Too large input");
        ArgumentChecker.isFalse(Double.isInfinite(resMatrix[i][j]), "Too large input");
      }
    }

    return new PiecewisePolynomialResult(new DoubleMatrix1D(_xValuesSrt), new DoubleMatrix2D(resMatrix), nCoefs, dim);

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

  /**
   * A set of methods below is for sorting xValues and yValues in the ascending order in terms of xValues
   * @param nDataPts 
   */
  protected void parallelBinarySort(final int nDataPts) {
    dualArrayQuickSort(_xValuesSrt, _yValuesSrt, 0, nDataPts - 1);
  }

  private static void dualArrayQuickSort(final double[] keys, final double[] values, final int left, final int right) {
    if (right > left) {
      final int pivot = (left + right) >> 1;
      final int pivotNewIndex = partition(keys, values, left, right, pivot);
      dualArrayQuickSort(keys, values, left, pivotNewIndex - 1);
      dualArrayQuickSort(keys, values, pivotNewIndex + 1, right);
    }
  }

  private static int partition(final double[] keys, final double[] values, final int left, final int right,
      final int pivot) {
    final double pivotValue = keys[pivot];
    swap(keys, values, pivot, right);
    int storeIndex = left;
    for (int i = left; i < right; i++) {
      if (keys[i] <= pivotValue) {
        swap(keys, values, i, storeIndex);
        storeIndex++;
      }
    }
    swap(keys, values, storeIndex, right);
    return storeIndex;
  }

  private static void swap(final double[] keys, final double[] values, final int first, final int second) {
    double t = keys[first];
    keys[first] = keys[second];
    keys[second] = t;

    t = values[first];
    values[first] = values[second];
    values[second] = t;
  }

}
