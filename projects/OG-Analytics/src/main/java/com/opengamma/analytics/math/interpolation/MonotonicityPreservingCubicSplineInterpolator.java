/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import java.util.Arrays;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.math.function.PiecewisePolynomialFunction1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ParallelArrayBinarySort;

/**
 * Filter for local monotonicity of cubic spline interpolation based on 
 * R. L. Dougherty, A. Edelman, and J. M. Hyman, "Nonnegativity-, Monotonicity-, or Convexity-Preserving Cubic and Quintic Hermite Interpolation" 
 * Mathematics Of Computation, v. 52, n. 186, April 1989, pp. 471-494. 
 * 
 * First, interpolant is computed by another cubic interpolation method. Then the first derivatives are modified such that local monotonicity conditions are satisfied. 
 */
public class MonotonicityPreservingCubicSplineInterpolator extends PiecewisePolynomialInterpolator {
  private final HermiteCoefficientsProvider _solver = new HermiteCoefficientsProvider();
  private final PiecewisePolynomialFunction1D _function = new PiecewisePolynomialFunction1D();
  private PiecewisePolynomialInterpolator _method;

  /**
   * Primary interpolation method should be passed
   * @param method PiecewisePolynomialInterpolator
   */
  public MonotonicityPreservingCubicSplineInterpolator(final PiecewisePolynomialInterpolator method) {
    _method = method;
  }

  @Override
  public PiecewisePolynomialResult interpolate(final double[] xValues, final double[] yValues) {

    ArgumentChecker.notNull(xValues, "xValues");
    ArgumentChecker.notNull(yValues, "yValues");

    ArgumentChecker.isTrue(xValues.length == yValues.length | xValues.length + 2 == yValues.length, "(xValues length = yValues length) or (xValues length + 2 = yValues length)");
    ArgumentChecker.isTrue(xValues.length > 2, "Data points should be more than 2");

    final int nDataPts = xValues.length;
    final int yValuesLen = yValues.length;

    for (int i = 0; i < nDataPts; ++i) {
      ArgumentChecker.isFalse(Double.isNaN(xValues[i]), "xValues containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(xValues[i]), "xValues containing Infinity");
    }
    for (int i = 0; i < yValuesLen; ++i) {
      ArgumentChecker.isFalse(Double.isNaN(yValues[i]), "yValues containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(yValues[i]), "yValues containing Infinity");
    }

    double[] xValuesSrt = Arrays.copyOf(xValues, nDataPts);
    double[] yValuesSrt = new double[nDataPts];
    if (nDataPts == yValuesLen) {
      yValuesSrt = Arrays.copyOf(yValues, nDataPts);
    } else {
      yValuesSrt = Arrays.copyOfRange(yValues, 1, nDataPts + 1);
    }
    ParallelArrayBinarySort.parallelBinarySort(xValuesSrt, yValuesSrt);

    for (int i = 1; i < nDataPts; ++i) {
      ArgumentChecker.isFalse(xValuesSrt[i - 1] == xValuesSrt[i], "xValues should be distinct");
    }

    final double[] intervals = _solver.intervalsCalculator(xValuesSrt);
    final double[] slopes = _solver.slopesCalculator(yValuesSrt, intervals);
    final PiecewisePolynomialResult result = _method.interpolate(xValues, yValues);

    ArgumentChecker.isTrue(result.getOrder() == 4, "Primary interpolant is not cubic");

    final double[] initialFirst = _function.differentiate(result, xValuesSrt).getData()[0];
    final double[] first = firstDerivativeCalculator(yValuesSrt, intervals, slopes, initialFirst);
    final double[][] coefs = _solver.solve(yValuesSrt, intervals, slopes, first);

    for (int i = 0; i < nDataPts - 1; ++i) {
      for (int j = 0; j < 4; ++j) {
        ArgumentChecker.isFalse(Double.isNaN(coefs[i][j]), "Too large input");
        ArgumentChecker.isFalse(Double.isInfinite(coefs[i][j]), "Too large input");
      }
    }

    return new PiecewisePolynomialResult(new DoubleMatrix1D(xValuesSrt), new DoubleMatrix2D(coefs), 4, 1);
  }

  @Override
  public PiecewisePolynomialResult interpolate(final double[] xValues, final double[][] yValuesMatrix) {
    ArgumentChecker.notNull(xValues, "xValues");
    ArgumentChecker.notNull(yValuesMatrix, "yValuesMatrix");

    ArgumentChecker.isTrue(xValues.length == yValuesMatrix[0].length | xValues.length + 2 == yValuesMatrix[0].length,
        "(xValues length = yValuesMatrix's row vector length) or (xValues length + 2 = yValuesMatrix's row vector length)");
    ArgumentChecker.isTrue(xValues.length > 2, "Data points should be more than 2");

    final int nDataPts = xValues.length;
    final int yValuesLen = yValuesMatrix[0].length;
    final int dim = yValuesMatrix.length;

    for (int i = 0; i < nDataPts; ++i) {
      ArgumentChecker.isFalse(Double.isNaN(xValues[i]), "xValues containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(xValues[i]), "xValues containing Infinity");
    }
    for (int i = 0; i < yValuesLen; ++i) {
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

    double[] xValuesSrt = new double[nDataPts];
    DoubleMatrix2D[] coefMatrix = new DoubleMatrix2D[dim];

    for (int i = 0; i < dim; ++i) {
      xValuesSrt = Arrays.copyOf(xValues, nDataPts);
      double[] yValuesSrt = new double[nDataPts];
      if (nDataPts == yValuesLen) {
        yValuesSrt = Arrays.copyOf(yValuesMatrix[i], nDataPts);
      } else {
        yValuesSrt = Arrays.copyOfRange(yValuesMatrix[i], 1, nDataPts + 1);
      }
      ParallelArrayBinarySort.parallelBinarySort(xValuesSrt, yValuesSrt);

      final double[] intervals = _solver.intervalsCalculator(xValuesSrt);
      final double[] slopes = _solver.slopesCalculator(yValuesSrt, intervals);
      final PiecewisePolynomialResult result = _method.interpolate(xValues, yValuesMatrix[i]);

      ArgumentChecker.isTrue(result.getOrder() == 4, "Primary interpolant is not cubic");

      final double[] initialFirst = _function.differentiate(result, xValuesSrt).getData()[0];
      final double[] first = firstDerivativeCalculator(yValuesSrt, intervals, slopes, initialFirst);

      coefMatrix[i] = new DoubleMatrix2D(_solver.solve(yValuesSrt, intervals, slopes, first));
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

    return new PiecewisePolynomialResult(new DoubleMatrix1D(xValuesSrt), new DoubleMatrix2D(resMatrix), nCoefs, dim);
  }

  @Override
  public PiecewisePolynomialResultsWithSensitivity interpolateWithSensitivity(final double[] xValues, final double[] yValues) {
    throw new NotImplementedException();
    //TODO Implement sensitivity calculator
  }

  /**
   * First derivatives are modified such that cubic interpolant has the same sign as linear interpolator 
   * @param yValues 
   * @param intervals 
   * @param slopes 
   * @param initialFirst 
   * @return first derivative 
   */
  private double[] firstDerivativeCalculator(final double[] yValues, final double[] intervals, final double[] slopes, final double[] initialFirst) {
    final int nDataPts = yValues.length;
    double[] res = new double[nDataPts];
    final double[][] pSlopes = parabolaSlopesCalculator(intervals, slopes);

    for (int i = 1; i < nDataPts - 1; ++i) {
      double refValue = 3. * Math.min(Math.abs(slopes[i - 1]), Math.min(Math.abs(slopes[i]), Math.abs(pSlopes[i - 1][1])));
      if (i > 1) {
        final double sig1 = Math.signum(pSlopes[i - 1][1]);
        final double sig2 = Math.signum(pSlopes[i - 1][0]);
        final double sig3 = Math.signum(slopes[i - 1] - slopes[i - 2]);
        final double sig4 = Math.signum(slopes[i] - slopes[i - 1]);
        if (Math.abs(sig1 - sig2) <= 1. && Math.abs(sig1 - sig3) <= 1. && Math.abs(sig1 - sig4) <= 1. && Math.abs(sig2 - sig3) <= 1. && Math.abs(sig2 - sig4) <= 1. && Math.abs(sig3 - sig4) <= 1.) {
          refValue = Math.max(refValue, 1.5 * Math.min(Math.abs(pSlopes[i - 1][0]), Math.abs(pSlopes[i - 1][1])));
        }
      }
      if (i < nDataPts - 2) {
        final double sig1 = Math.signum(-pSlopes[i - 1][1]);
        final double sig2 = Math.signum(-pSlopes[i - 1][2]);
        final double sig3 = Math.signum(slopes[i + 1] - slopes[i]);
        final double sig4 = Math.signum(slopes[i] - slopes[i - 1]);
        if (Math.abs(sig1 - sig2) <= 1. && Math.abs(sig1 - sig3) <= 1. && Math.abs(sig1 - sig4) <= 1. && Math.abs(sig2 - sig3) <= 1. && Math.abs(sig2 - sig4) <= 1. && Math.abs(sig3 - sig4) <= 1.) {
          refValue = Math.max(refValue, 1.5 * Math.min(Math.abs(pSlopes[i - 1][2]), Math.abs(pSlopes[i - 1][1])));
        }
      }
      res[i] = Math.signum(initialFirst[i]) != Math.signum(pSlopes[i - 1][1]) ? 0. : Math.signum(initialFirst[i]) * Math.min(Math.abs(initialFirst[i]), refValue);
    }
    res[0] = Math.signum(initialFirst[0]) != Math.signum(slopes[0]) ? 0. : Math.signum(initialFirst[0]) * Math.min(Math.abs(initialFirst[0]), 3. * Math.abs(slopes[0]));
    res[nDataPts - 1] = Math.signum(initialFirst[nDataPts - 1]) != Math.signum(slopes[nDataPts - 2]) ? 0. : Math.signum(initialFirst[nDataPts - 1])
        * Math.min(Math.abs(initialFirst[nDataPts - 1]), 3. * Math.abs(slopes[nDataPts - 2]));
    return res;
  }

  /** 
   * @param intervals 
   * @param slopes 
   * @return Parabola slopes, each row vactor is (p^{-1}, p^{0}, p^{1}) for xValues_1,...,xValues_{nDataPts-2}
   */
  private double[][] parabolaSlopesCalculator(final double[] intervals, final double[] slopes) {
    final int nData = intervals.length + 1;
    double[][] res = new double[nData - 2][3];

    res[0][0] = 1. / 0.;
    res[0][1] = (slopes[0] * intervals[1] - slopes[1] * intervals[0]) / (intervals[0] + intervals[1]);
    res[0][2] = (slopes[1] * (2. * intervals[1] + intervals[2]) - slopes[2] * intervals[1]) / (intervals[1] + intervals[2]);
    for (int i = 1; i < nData - 3; ++i) {
      res[i][0] = (slopes[i] * (2. * intervals[i] + intervals[i - 1]) - slopes[i - 1] * intervals[i]) / (intervals[i - 1] + intervals[i]);
      res[i][1] = (slopes[i] * intervals[i + 1] + slopes[i + 1] * intervals[i]) / (intervals[i] + intervals[i + 1]);
      res[i][2] = (slopes[i + 1] * (2. * intervals[i + 1] + intervals[i + 2]) - slopes[i + 2] * intervals[i + 1]) / (intervals[i + 1] + intervals[i + 2]);
    }
    res[nData - 3][0] = (slopes[nData - 3] * (2. * intervals[nData - 3] + intervals[nData - 4]) - slopes[nData - 4] * intervals[nData - 3]) / (intervals[nData - 4] + intervals[nData - 3]);
    res[nData - 3][1] = (slopes[nData - 3] * intervals[nData - 2] - slopes[nData - 2] * intervals[nData - 3]) / (intervals[nData - 3] + intervals[nData - 2]);
    res[nData - 3][2] = 1. / 0.;

    return res;
  }
}
