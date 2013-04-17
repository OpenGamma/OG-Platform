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
 * C1 cubic interpolation preserving monotonicity 
 */
public class PiecewiseCubicHermiteSplineInterpolator extends PiecewisePolynomialInterpolator {

  private double[] _xValuesSrt;
  private double[] _yValuesSrt;

  /**
   * 
   */
  public PiecewiseCubicHermiteSplineInterpolator() {
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
      ArgumentChecker.isFalse(Double.isNaN(xValues[i]), "xValues containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(xValues[i]), "xValues containing Infinity");
      for (int j = 0; j < dim; ++j) {
        ArgumentChecker.isFalse(Double.isNaN(yValuesMatrix[j][i]), "yValuesMatrix containing NaN");
        ArgumentChecker.isFalse(Double.isInfinite(yValuesMatrix[j][i]), "yValuesMatrix containing Infinity");
      }
    }

    for (int i = 0; i < nDataPts; ++i) {
      for (int j = i + 1; j < nDataPts; ++j) {
        ArgumentChecker.isFalse(xValues[i] == xValues[j], "xValues should be distinct");
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
   * @return Coefficient matrix whose i-th row vector is {a3, a2, a1, a0} of f(x) = a3 * (x-x_i)^3 + a2 * (x-x_i)^2 +... for the i-th interval
   */
  private DoubleMatrix2D solve(final double[] xValues, final double[] yValues) {

    final int nDataPts = xValues.length;

    double[][] res = new double[nDataPts - 1][4];
    double[] intervals = new double[nDataPts - 1];
    double[] grads = new double[nDataPts - 1];

    for (int i = 0; i < nDataPts - 1; ++i) {
      intervals[i] = xValues[i + 1] - xValues[i];
      grads[i] = (yValues[i + 1] - yValues[i]) / intervals[i];
    }

    if (nDataPts == 2) {
      res[0][2] = grads[0];
      res[0][3] = xValues[0];
    } else {
      double[] derivatives = slopeFinder(intervals, grads);
      for (int i = 0; i < nDataPts - 1; ++i) {
        res[i][0] = -2. * yValues[i + 1] / intervals[i] / intervals[i] / intervals[i] + 2. * yValues[i] / intervals[i] / intervals[i] / intervals[i] + derivatives[i + 1] / intervals[i] /
            intervals[i] +
            derivatives[i] / intervals[i] / intervals[i];
        res[i][1] = 3. * yValues[i + 1] / intervals[i] / intervals[i] - 3. * yValues[i] / intervals[i] / intervals[i] - derivatives[i + 1] / intervals[i] - 2. * derivatives[i] / intervals[i];
        res[i][2] = derivatives[i];
        res[i][3] = yValues[i];
      }
    }
    return new DoubleMatrix2D(res);
  }

  /**
   * @param intervals 
   * @param grads 
   * @return A set of the first derivatives at knots
   */
  private double[] slopeFinder(final double[] intervals, final double[] grads) {
    final int nInts = intervals.length;
    double[] res = new double[nInts + 1];

    res[0] = endpointSlope(intervals[0], intervals[1], grads[0], grads[1]);
    res[nInts] = endpointSlope(intervals[nInts - 1], intervals[nInts - 2], grads[nInts - 1], grads[nInts - 2]);

    for (int i = 1; i < nInts; ++i) {
      if (Math.signum(grads[i]) != Math.signum(grads[i - 1]) | (grads[i] == 0 | grads[i - 1] == 0)) {
        res[i] = 0.;
      } else {
        final double den1 = 2. * intervals[i] + intervals[i - 1];
        final double den2 = intervals[i] + 2. * intervals[i - 1];
        res[i] = 3. * (intervals[i] + intervals[i - 1]) / (den1 / grads[i - 1] + den2 / grads[i]);
      }
    }

    return res;
  }

  private double endpointSlope(final double ints1, final double ints2, final double grads1, final double grads2) {
    final double val = (2. * ints1 + ints2) * grads1 / (ints1 + ints2) - ints1 * grads2 / (ints1 + ints2);

    if (Math.signum(val) != Math.signum(grads1)) {
      return 0.;
    } else {
      if (Math.signum(grads1) != Math.signum(grads2) && Math.abs(val) > 3. * Math.abs(grads1)) {
        return 3. * grads1;
      }
    }
    return val;
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
