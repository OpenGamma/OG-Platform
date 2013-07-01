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
 * Quintic interpolation preserving nonnegativity and C2 continuity based on 
 * R. L. Dougherty, A. Edelman, and J. M. Hyman, "Nonnegativity-, Monotonicity-, or Convexity-Preserving Cubic and Quintic Hermite Interpolation" 
 * Mathematics Of Computation, v. 52, n. 186, April 1989, pp. 471-494. 
 * 
 * The primary interpolant is used for computing first and second derivative at each data point. They are modified such that non-negativity conditions are satisfied. 
 * Note that shape-preserving three-point formula is used at endpoints 
 */
public class NonnegativityPreservingQuinticSplineInterpolator extends PiecewisePolynomialInterpolator {

  private final HermiteCoefficientsProvider _solver = new HermiteCoefficientsProvider();
  private final PiecewisePolynomialFunction1D _function = new PiecewisePolynomialFunction1D();
  private PiecewisePolynomialInterpolator _method;

  /**
   * Primary interpolation method should be passed
   * @param method PiecewisePolynomialInterpolator
   */
  public NonnegativityPreservingQuinticSplineInterpolator(final PiecewisePolynomialInterpolator method) {
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
    final double[] first = firstDerivativeCalculator(yValuesSrt, intervals, slopes, initialFirst);
    final double[] second = secondDerivativeCalculator(yValuesSrt, intervals, first, initialSecond);
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
      final double[] second = secondDerivativeCalculator(yValuesSrt, intervals, first, initialSecond);

      coefMatrix[i] = new DoubleMatrix2D(_solver.solve(yValuesSrt, intervals, slopes, first, second));
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

    for (int i = 1; i < nDataPts - 1; ++i) {
      final double tau = Math.signum(yValues[i]);
      res[i] = tau == 0. ? 0. : Math.min(5. * tau * yValues[i] / intervals[i - 1], Math.max(-5. * tau * yValues[i] / intervals[i], tau * initialFirst[i])) / tau;
    }
    final double tauIni = Math.signum(yValues[0]);
    final double tauFin = Math.signum(yValues[nDataPts - 1]);
    res[0] = tauIni == 0. ? 0. : Math.min(5. * tauIni * yValues[0] / intervals[0], Math.max(-5. * tauIni * yValues[0] / intervals[0], tauIni * initialFirst[0])) / tauIni;
    res[nDataPts - 1] = tauFin == 0. ? 0. : Math.min(5. * tauFin * yValues[nDataPts - 1] / intervals[nDataPts - 2],
        Math.max(-5. * tauFin * yValues[nDataPts - 1] / intervals[nDataPts - 2], tauFin * initialFirst[nDataPts - 1])) /
        tauFin;

    return res;
  }

  private double[] secondDerivativeCalculator(final double[] yValues, final double[] intervals, final double[] first, final double[] initialSecond) {
    final int nDataPts = yValues.length;
    double[] res = new double[nDataPts];

    for (int i = 1; i < nDataPts - 1; ++i) {
      final double tau = Math.signum(yValues[i]);
      res[i] = tau == 0. ? 0. : Math.max(initialSecond[i] * tau,
          tau * Math.max(8. * first[i] / intervals[i - 1] - 20. * yValues[i] / intervals[i - 1] / intervals[i - 1], -8. * first[i] / intervals[i] - 20. * yValues[i] / intervals[i] / intervals[i])) /
          tau;
    }
    final double tauIni = Math.signum(yValues[0]);
    final double tauFin = Math.signum(yValues[nDataPts - 1]);
    res[0] = tauIni == 0. ? 0. : Math.max(initialSecond[0] * tauIni,
        tauIni * Math.max(8. * first[0] / intervals[0] - 20. * yValues[0] / intervals[0] / intervals[0], -8. * first[0] / intervals[0] - 20. * yValues[0] / intervals[0] / intervals[0])) /
        tauIni;
    res[nDataPts - 1] = tauFin == 0. ? 0. : Math.max(
        initialSecond[0] * tauFin,
        tauFin *
            Math.max(8. * first[nDataPts - 1] / intervals[nDataPts - 2] - 20. * yValues[nDataPts - 1] / intervals[nDataPts - 2] / intervals[nDataPts - 2], -8. * first[nDataPts - 1] /
                intervals[nDataPts - 2] - 20. * yValues[nDataPts - 1] / intervals[nDataPts - 2] / intervals[nDataPts - 2])) / tauFin;
    ;

    return res;
  }
}
