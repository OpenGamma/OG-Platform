/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import java.util.Arrays;

import com.google.common.primitives.Doubles;
import com.opengamma.analytics.math.function.PiecewisePolynomialFunction1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ParallelArrayBinarySort;

/**
 * Quintic interpolation preserving local monotonicity and C2 continuity based on 
 * R. L. Dougherty, A. Edelman, and J. M. Hyman, "Nonnegativity-, Monotonicity-, or Convexity-Preserving Cubic and Quintic Hermite Interpolation" 
 * Mathematics Of Computation, v. 52, n. 186, April 1989, pp. 471-494. 
 * 
 * The primary interpolant is used for computing first and second derivative at each data point. They are modified such that local monotonicity conditions are satisfied. 
 * Note that shape-preserving three-point formula is used at endpoints 
 */
public class MonotonicityPreservingQuinticSplineInterpolator extends PiecewisePolynomialInterpolator {

  private static final double ERROR = 1.e-12;
  private static final double EPS = 1.e-6;
  private static final double SMALL = 1.e-14;

  private final HermiteCoefficientsProvider _solver = new HermiteCoefficientsProvider();
  private final PiecewisePolynomialFunction1D _function = new PiecewisePolynomialFunction1D();
  private PiecewisePolynomialInterpolator _method;

  /**
   * Primary interpolation method should be passed
   * @param method PiecewisePolynomialInterpolator
   */
  public MonotonicityPreservingQuinticSplineInterpolator(final PiecewisePolynomialInterpolator method) {
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

    for (int i = 0; i < nDataPts - 1; ++i) {
      for (int j = i + 1; j < nDataPts; ++j) {
        ArgumentChecker.isFalse(xValues[i] == xValues[j], "xValues should be distinct");
      }
    }

    double[] xValuesSrt = Arrays.copyOf(xValues, nDataPts);
    double[] yValuesSrt = new double[nDataPts];
    if (nDataPts == yValuesLen) {
      yValuesSrt = Arrays.copyOf(yValues, nDataPts);
    } else {
      yValuesSrt = Arrays.copyOfRange(yValues, 1, nDataPts + 1);
    }
    ParallelArrayBinarySort.parallelBinarySort(xValuesSrt, yValuesSrt);

    final double[] intervals = _solver.intervalsCalculator(xValuesSrt);
    final double[] slopes = _solver.slopesCalculator(yValuesSrt, intervals);
    final PiecewisePolynomialResult result = _method.interpolate(xValues, yValues);

    ArgumentChecker.isTrue(result.getOrder() >= 3, "Primary interpolant should be degree >= 2");

    final double[] initialFirst = _function.differentiate(result, xValuesSrt).getData()[0];
    final double[] initialSecond = _function.differentiateTwice(result, xValuesSrt).getData()[0];
    double[] first = firstDerivativeCalculator(yValuesSrt, intervals, slopes, initialFirst);

    boolean modFirst = false;
    int k;
    double[] aValues = aValuesCalculator(slopes, first);
    double[] bValues = bValuesCalculator(slopes, first);
    double[][] intervalsA = getIntervalsA(intervals, slopes, first, bValues);
    double[][] intervalsB = getIntervalsB(intervals, slopes, first, aValues);
    while (modFirst == false) {
      k = 0;
      for (int i = 0; i < nDataPts - 2; ++i) {
        if (first[i + 1] > 0.) {
          if (intervalsA[i + 1][1] + Math.abs(intervalsA[i + 1][1]) * ERROR < intervalsB[i][0] - Math.abs(intervalsB[i][0]) * ERROR |
              intervalsA[i + 1][0] - Math.abs(intervalsA[i + 1][0]) * ERROR > intervalsB[i][1] + Math.abs(intervalsB[i][1]) * ERROR) {
            ++k;
            first[i + 1] = firstDerivativesRecalculator(intervals, slopes, aValues, bValues, i + 1);
          }
        }
      }
      if (k == 0) {
        modFirst = true;
      }
      aValues = aValuesCalculator(slopes, first);
      bValues = bValuesCalculator(slopes, first);
      intervalsA = getIntervalsA(intervals, slopes, first, bValues);
      intervalsB = getIntervalsB(intervals, slopes, first, aValues);
    }
    final double[] second = secondDerivativeCalculator(initialSecond, intervalsA, intervalsB);
    final double[][] coefs = _solver.solve(yValuesSrt, intervals, slopes, first, second);

    for (int i = 0; i < nDataPts - 1; ++i) {
      for (int j = 0; j < 6; ++j) {
        ArgumentChecker.isFalse(Double.isNaN(coefs[i][j]), "Too large input");
        ArgumentChecker.isFalse(Double.isInfinite(coefs[i][j]), "Too large input");
      }
    }

    return new PiecewisePolynomialResult(new DoubleMatrix1D(xValuesSrt), new DoubleMatrix2D(coefs), 6, 1);
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

      ArgumentChecker.isTrue(result.getOrder() >= 3, "Primary interpolant should be degree >= 2");

      final double[] initialFirst = _function.differentiate(result, xValuesSrt).getData()[0];
      final double[] initialSecond = _function.differentiateTwice(result, xValuesSrt).getData()[0];
      final double[] first = firstDerivativeCalculator(yValuesSrt, intervals, slopes, initialFirst);

      boolean modFirst = false;
      int k;
      double[] aValues = aValuesCalculator(slopes, first);
      double[] bValues = bValuesCalculator(slopes, first);
      double[][] intervalsA = getIntervalsA(intervals, slopes, first, bValues);
      double[][] intervalsB = getIntervalsB(intervals, slopes, first, aValues);
      while (modFirst == false) {
        k = 0;
        for (int j = 0; j < nDataPts - 2; ++j) {
          if (first[j + 1] > 0.) {
            if (intervalsA[j + 1][1] + Math.abs(intervalsA[j + 1][1]) * ERROR < intervalsB[j][0] - Math.abs(intervalsB[j][0]) * ERROR |
                intervalsA[j + 1][0] - Math.abs(intervalsA[j + 1][0]) * ERROR > intervalsB[j][1] + Math.abs(intervalsB[j][1]) * ERROR) {
              ++k;
              first[j + 1] = firstDerivativesRecalculator(intervals, slopes, aValues, bValues, j + 1);
            }
          }
        }
        if (k == 0) {
          modFirst = true;
        }
        aValues = aValuesCalculator(slopes, first);
        bValues = bValuesCalculator(slopes, first);
        intervalsA = getIntervalsA(intervals, slopes, first, bValues);
        intervalsB = getIntervalsB(intervals, slopes, first, aValues);
      }
      final double[] second = secondDerivativeCalculator(initialSecond, intervalsA, intervalsB);

      coefMatrix[i] = new DoubleMatrix2D(_solver.solve(yValuesSrt, intervals, slopes, first, second));
    }

    final int nIntervals = coefMatrix[0].getNumberOfRows();
    final int nCoefs = coefMatrix[0].getNumberOfColumns();
    double[][] resMatrix = new double[dim * nIntervals][nCoefs];

    for (int i = 0; i < nIntervals; ++i) {
      for (int j = 0; j < dim; ++j) {
        resMatrix[dim * i + j] = coefMatrix[j].getRowVector(i, false).getData();
      }
    }

    for (int i = 0; i < (nIntervals * dim); ++i) {
      for (int j = 0; j < nCoefs; ++j) {
        ArgumentChecker.isFalse(Double.isNaN(resMatrix[i][j]), "Too large input");
        ArgumentChecker.isFalse(Double.isInfinite(resMatrix[i][j]), "Too large input");
      }
    }

    return new PiecewisePolynomialResult(new DoubleMatrix1D(xValuesSrt, false), DoubleMatrix2D.noCopy(resMatrix), nCoefs, dim);
  }

  @Override
  public PiecewisePolynomialResultsWithSensitivity interpolateWithSensitivity(final double[] xValues, final double[] yValues) {

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

    for (int i = 0; i < nDataPts - 1; ++i) {
      for (int j = i + 1; j < nDataPts; ++j) {
        ArgumentChecker.isFalse(xValues[i] == xValues[j], "xValues should be distinct");
      }
    }

    double[] yValuesSrt = new double[nDataPts];
    if (nDataPts == yValuesLen) {
      yValuesSrt = Arrays.copyOf(yValues, nDataPts);
    } else {
      yValuesSrt = Arrays.copyOfRange(yValues, 1, nDataPts + 1);
    }

    final double[] intervals = _solver.intervalsCalculator(xValues);
    final double[] slopes = _solver.slopesCalculator(yValuesSrt, intervals);
    double[][] slopesSensitivity = _solver.slopeSensitivityCalculator(intervals);
    final DoubleMatrix1D[] firstWithSensitivity = new DoubleMatrix1D[nDataPts + 1];
    final DoubleMatrix1D[] secondWithSensitivity = new DoubleMatrix1D[nDataPts + 1];
    final PiecewisePolynomialResult result = _method.interpolate(xValues, yValues);

    ArgumentChecker.isTrue(result.getOrder() >= 3, "Primary interpolant should be degree >= 2");

    final double[] initialFirst = _function.differentiate(result, xValues).getData()[0];
    final double[] initialSecond = _function.differentiateTwice(result, xValues).getData()[0];
    double[] first = firstDerivativeCalculator(yValuesSrt, intervals, slopes, initialFirst);
    boolean modFirst = false;
    int k;
    double[] aValues = aValuesCalculator(slopes, first);
    double[] bValues = bValuesCalculator(slopes, first);
    double[][] intervalsA = getIntervalsA(intervals, slopes, first, bValues);
    double[][] intervalsB = getIntervalsB(intervals, slopes, first, aValues);
    while (modFirst == false) {
      k = 0;
      for (int i = 0; i < nDataPts - 2; ++i) {
        if (first[i + 1] > 0.) {
          if (intervalsA[i + 1][1] + Math.abs(intervalsA[i + 1][1]) * ERROR < intervalsB[i][0] - Math.abs(intervalsB[i][0]) * ERROR |
              intervalsA[i + 1][0] - Math.abs(intervalsA[i + 1][0]) * ERROR > intervalsB[i][1] + Math.abs(intervalsB[i][1]) * ERROR) {
            ++k;
            first[i + 1] = firstDerivativesRecalculator(intervals, slopes, aValues, bValues, i + 1);
          }
        }
      }
      if (k == 0) {
        modFirst = true;
      }
      aValues = aValuesCalculator(slopes, first);
      bValues = bValuesCalculator(slopes, first);
      intervalsA = getIntervalsA(intervals, slopes, first, bValues);
      intervalsB = getIntervalsB(intervals, slopes, first, aValues);
    }
    final double[] second = secondDerivativeCalculator(initialSecond, intervalsA, intervalsB);
    firstWithSensitivity[0] = new DoubleMatrix1D(first);
    secondWithSensitivity[0] = new DoubleMatrix1D(second);

    /*
     * Centered finite difference method is used for computing node sensitivity
     */
    int nExtra = (nDataPts == yValuesLen) ? 0 : 1;
    final double[] yValuesUp = Arrays.copyOf(yValues, nDataPts + 2 * nExtra);
    final double[] yValuesDw = Arrays.copyOf(yValues, nDataPts + 2 * nExtra);
    final double[][] tmpFirst = new double[nDataPts][nDataPts];
    final double[][] tmpSecond = new double[nDataPts][nDataPts];
    for (int l = nExtra; l < nDataPts + nExtra; ++l) {
      final double den = Math.abs(yValues[l]) < SMALL ? EPS : yValues[l] * EPS;
      yValuesUp[l] = Math.abs(yValues[l]) < SMALL ? EPS : yValues[l] * (1. + EPS);
      yValuesDw[l] = Math.abs(yValues[l]) < SMALL ? -EPS : yValues[l] * (1. - EPS);
      final double[] yValuesSrtUp = Arrays.copyOfRange(yValuesUp, nExtra, nDataPts + nExtra);
      final double[] yValuesSrtDw = Arrays.copyOfRange(yValuesDw, nExtra, nDataPts + nExtra);

      final DoubleMatrix1D[] yValuesUpDw = new DoubleMatrix1D[] {new DoubleMatrix1D(yValuesUp), new DoubleMatrix1D(yValuesDw) };
      final DoubleMatrix1D[] yValuesSrtUpDw = new DoubleMatrix1D[] {new DoubleMatrix1D(yValuesSrtUp), new DoubleMatrix1D(yValuesSrtDw) };
      final DoubleMatrix1D[] firstSecondUpDw = new DoubleMatrix1D[4];
      for (int ii = 0; ii < 2; ++ii) {
        final double[] slopesUpDw = _solver.slopesCalculator(yValuesSrtUpDw[ii].getData(), intervals);
        final PiecewisePolynomialResult resultUpDw = _method.interpolate(xValues, yValuesUpDw[ii].getData());
        final double[] initialFirstUpDw = _function.differentiate(resultUpDw, xValues).getData()[0];
        final double[] initialSecondUpDw = _function.differentiateTwice(resultUpDw, xValues).getData()[0];
        double[] firstUpDw = firstDerivativeCalculator(yValuesSrtUpDw[ii].getData(), intervals, slopesUpDw, initialFirstUpDw);
        boolean modFirstUpDw = false;
        double[] aValuesUpDw = aValuesCalculator(slopesUpDw, firstUpDw);
        double[] bValuesUpDw = bValuesCalculator(slopesUpDw, firstUpDw);
        double[][] intervalsAUpDw = getIntervalsA(intervals, slopesUpDw, firstUpDw, bValuesUpDw);
        double[][] intervalsBUpDw = getIntervalsB(intervals, slopesUpDw, firstUpDw, aValuesUpDw);
        while (modFirstUpDw == false) {
          k = 0;
          for (int i = 0; i < nDataPts - 2; ++i) {
            if (firstUpDw[i + 1] > 0.) {
              if (intervalsAUpDw[i + 1][1] + Math.abs(intervalsAUpDw[i + 1][1]) * ERROR < intervalsBUpDw[i][0] - Math.abs(intervalsBUpDw[i][0]) * ERROR |
                  intervalsAUpDw[i + 1][0] - Math.abs(intervalsAUpDw[i + 1][0]) * ERROR > intervalsBUpDw[i][1] + Math.abs(intervalsBUpDw[i][1]) * ERROR) {
                ++k;
                firstUpDw[i + 1] = firstDerivativesRecalculator(intervals, slopesUpDw, aValuesUpDw, bValuesUpDw, i + 1);
              }
            }
          }
          if (k == 0) {
            modFirstUpDw = true;
          }
          aValuesUpDw = aValuesCalculator(slopesUpDw, firstUpDw);
          bValuesUpDw = bValuesCalculator(slopesUpDw, firstUpDw);
          intervalsAUpDw = getIntervalsA(intervals, slopesUpDw, firstUpDw, bValuesUpDw);
          intervalsBUpDw = getIntervalsB(intervals, slopesUpDw, firstUpDw, aValuesUpDw);
        }
        final double[] secondUpDw = secondDerivativeCalculator(initialSecondUpDw, intervalsAUpDw, intervalsBUpDw);
        firstSecondUpDw[ii] = new DoubleMatrix1D(firstUpDw);
        firstSecondUpDw[2 + ii] = new DoubleMatrix1D(secondUpDw);
      }
      for (int j = 0; j < nDataPts; ++j) {
        tmpFirst[j][l - nExtra] = 0.5 * (firstSecondUpDw[0].getData()[j] - firstSecondUpDw[1].getData()[j]) / den;
        tmpSecond[j][l - nExtra] = 0.5 * (firstSecondUpDw[2].getData()[j] - firstSecondUpDw[3].getData()[j]) / den;
      }
      yValuesUp[l] = yValues[l];
      yValuesDw[l] = yValues[l];
    }
    for (int i = 0; i < nDataPts; ++i) {
      firstWithSensitivity[i + 1] = new DoubleMatrix1D(tmpFirst[i]);
      secondWithSensitivity[i + 1] = new DoubleMatrix1D(tmpSecond[i]);
    }

    final DoubleMatrix2D[] resMatrix = _solver.solveWithSensitivity(yValuesSrt, intervals, slopes, slopesSensitivity, firstWithSensitivity, secondWithSensitivity);

    for (int l = 0; l < nDataPts; ++l) {
      DoubleMatrix2D m = resMatrix[l];
      final int rows = m.getNumberOfRows();
      final int cols = m.getNumberOfColumns();
      for (int i = 0; i < rows; ++i) {
        for (int j = 0; j < cols; ++j) {
          ArgumentChecker.isTrue(Doubles.isFinite(m.getEntry(i, j)), "Matrix contains a NaN or infinite");
        }
      }
    }

    final DoubleMatrix2D coefMatrix = resMatrix[0];
    final DoubleMatrix2D[] coefSenseMatrix = new DoubleMatrix2D[nDataPts - 1];
    System.arraycopy(resMatrix, 1, coefSenseMatrix, 0, nDataPts - 1);
    final int nCoefs = coefMatrix.getNumberOfColumns();

    return new PiecewisePolynomialResultsWithSensitivity(new DoubleMatrix1D(xValues), coefMatrix, nCoefs, 1, coefSenseMatrix);
  }

  @Override
  public PiecewisePolynomialInterpolator getPrimaryMethod() {
    return _method;
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

    res[0] = Math.max(Math.min(Math.max(0., initialFirst[0]), 5. * Math.abs(slopes[0])), -5. * Math.abs(slopes[0]));
    res[nDataPts - 1] = Math.max(Math.min(Math.max(0., initialFirst[nDataPts - 2]), 5. * Math.abs(slopes[nDataPts - 2])), -5. * Math.abs(slopes[nDataPts - 2]));
    for (int i = 1; i < nDataPts - 1; ++i) {
      final double sigma = slopes[i - 1] * slopes[i] < 0 ? Math.signum(initialFirst[i]) : 0.;
      if (sigma >= 0.) {
        res[i] = Math.min(Math.max(0., initialFirst[i]), 5. * Math.min(Math.abs(slopes[i - 1]), Math.abs(slopes[i])));
      } else {
        res[i] = Math.max(Math.min(0., initialFirst[i]), -5. * Math.min(Math.abs(slopes[i - 1]), Math.abs(slopes[i])));
      }
    }

    return res;
  }

  private double[] aValuesCalculator(final double[] slopes, final double[] first) {
    final int nData = slopes.length + 1;
    double[] res = new double[nData - 1];

    for (int i = 0; i < nData - 1; ++i) {
      res[i] = slopes[i] == 0. ? 0. : Math.max(0., first[i] / slopes[i]);
    }
    return res;
  }

  private double[] bValuesCalculator(final double[] slopes, final double[] first) {
    final int nData = slopes.length + 1;
    double[] res = new double[nData - 1];

    for (int i = 0; i < nData - 1; ++i) {
      res[i] = slopes[i] == 0. ? 0. : Math.max(0., first[i + 1] / slopes[i]);
    }
    return res;
  }

  private double[][] getIntervalsA(final double[] intervals, final double[] slopes, final double[] first, final double[] bValues) {
    final int nData = intervals.length + 1;
    double[][] res = new double[nData - 1][2];

    for (int i = 0; i < nData - 1; ++i) {
      final double dPlus = first[i] * slopes[i] > 0 ? first[i] : 0.;
      final double left = (-7.9 * dPlus - 0.26 * dPlus * bValues[i]) / intervals[i];
      final double right = ((20. - 2. * bValues[i]) * slopes[i] - 8. * dPlus - 0.48 * dPlus * bValues[i]) / intervals[i];
      if (dPlus == 0.) {
        res[i][0] = Math.min(left, right);
        res[i][1] = Math.max(left, right);
      } else {
        res[i][0] = left;
        res[i][1] = right;
      }
      if (Math.abs(res[i][0]) < ERROR / 100.) {
        res[i][0] = 0.;
      }
      if (Math.abs(res[i][1]) < ERROR / 100.) {
        res[i][1] = 0.;
      }
    }

    return res;
  }

  private double[][] getIntervalsB(final double[] intervals, final double[] slopes, final double[] first, final double[] aValues) {
    final int nData = intervals.length + 1;
    double[][] res = new double[nData - 1][2];

    for (int i = 0; i < nData - 1; ++i) {
      final double dMinus = first[i + 1] * slopes[i] > 0 ? first[i + 1] : 0.;
      final double left = ((-20. + 2. * aValues[i]) * slopes[i] + 8. * dMinus + 0.48 * dMinus * aValues[i]) / intervals[i];
      final double right = (7.9 * dMinus + 0.26 * dMinus * aValues[i]) / intervals[i];
      if (dMinus == 0.) {
        res[i][0] = Math.min(left, right);
        res[i][1] = Math.max(left, right);
      } else {
        res[i][0] = left;
        res[i][1] = right;
      }
      if (Math.abs(res[i][0]) < ERROR / 100.) {
        res[i][0] = 0.;
      }
      if (Math.abs(res[i][1]) < ERROR / 100.) {
        res[i][1] = 0.;
      }
    }

    return res;
  }

  private double firstDerivativesRecalculator(final double[] intervals, final double[] slopes, final double[] aValues, final double[] bValues, final int position) {
    return ((20. - 2. * bValues[position]) * slopes[position] / intervals[position] + (20. - 2. * aValues[position - 1]) * slopes[position - 1] / intervals[position - 1]) /
        ((8. + 0.48 * bValues[position]) / intervals[position] + (8. + 0.48 * aValues[position - 1]) / intervals[position - 1]);
  }

  private double[] secondDerivativeCalculator(final double[] initialSecond, final double[][] intervalsA, final double[][] intervalsB) {
    final int nData = initialSecond.length;
    double[] res = new double[nData];

    for (int i = 0; i < nData - 2; ++i) {
      res[i + 1] = Math.min(intervalsA[i + 1][1], Math.max(intervalsA[i + 1][0], initialSecond[i + 1]));
      res[i + 1] = Math.min(intervalsB[i][1], Math.max(intervalsB[i][0], res[i + 1]));
    }
    res[0] = Math.min(intervalsA[0][1], Math.max(intervalsA[0][0], initialSecond[0]));
    res[nData - 1] = Math.min(intervalsB[nData - 2][1], Math.max(intervalsB[nData - 2][0], initialSecond[nData - 1]));

    return res;
  }
}
