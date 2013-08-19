/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import java.util.Arrays;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.math.interpolation.data.Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ParallelArrayBinarySort;

/**
 * Shape-preserving C2 cubic spline interpolation based on
 * S. Pruess "Shape preserving C2 cubic spline interpolation"
 *  IMA Journal of Numerical Analysis (1993) 13 (4): 493-507.
 * where two extra knots are introduced between adjacent data points
 * As the position of the new knots are data dependent, the matrix form of yValues producing multi-splines is not relevant
 */
public class ShapePreservingCubicSplineInterpolator extends PiecewisePolynomialInterpolator {

  private static final double INF = 1. / 0.;
  private static final double ERROR = 1.e-12;

  @Override
  public PiecewisePolynomialResult interpolate(final double[] xValues, final double[] yValues) {

    ArgumentChecker.notNull(xValues, "xValues");
    ArgumentChecker.notNull(yValues, "yValues");

    ArgumentChecker.isTrue(xValues.length == yValues.length, "xValues length = yValues length");
    ArgumentChecker.isTrue(xValues.length > 2, "Data points should be more than 1");

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

    final double[] xValuesSrt = Arrays.copyOf(xValues, nDataPts);
    final double[] yValuesSrt = Arrays.copyOf(yValues, nDataPts);
    ParallelArrayBinarySort.parallelBinarySort(xValuesSrt, yValuesSrt);

    final double[] intervals = intervalsCalculator(xValuesSrt);
    final double[] slopes = slopesCalculator(yValuesSrt, intervals);
    final double[] beta = betaCalculator(slopes);
    double[] first = firstDiffFinder(intervals, slopes);
    double[] rValues = rValuesCalculator(slopes, first);

    boolean correctSign = false;
    int it = 0;

    while (correctSign == false) {
      correctSign = signChecker(beta, rValues);
      if (correctSign == false) {
        first = firstDiffSweep(intervals, slopes, beta, first);
        rValues = rValuesCalculator(slopes, first);
      }
      ++it;
      if (it > 10) {
        throw new IllegalArgumentException("Spline is not found!");
      }
    }

    final double[] second = secondDiffFinder(intervals, beta, rValues);
    final double[] tau = tauFinder(intervals, slopes, beta, first, second);
    final double[] knots = knotsProvider(xValuesSrt, intervals, tau);

    final double[][] coefMatrix = solve(yValuesSrt, intervals, slopes, first, second, tau);

    for (int i = 0; i < coefMatrix.length; ++i) {
      double ref = 0.;
      final double interval = knots[i + 1] - knots[i];
      for (int j = 0; j < 4; ++j) {
        ref += coefMatrix[i][j] * Math.pow(interval, 3 - j);
        ArgumentChecker.isFalse(Double.isNaN(coefMatrix[i][j]), "Too large input");
        ArgumentChecker.isFalse(Double.isInfinite(coefMatrix[i][j]), "Too large input");
      }
      final double yVal = i == coefMatrix.length - 1 ? yValues[nDataPts - 1] : coefMatrix[i + 1][3];
      final double bound = Math.max(Math.abs(ref) + Math.abs(yVal), 1.e-1);
      ArgumentChecker.isTrue(Math.abs(ref - yVal) < ERROR * bound, "Input is too large/small or data points are too close");
    }

    return new PiecewisePolynomialResult(new DoubleMatrix1D(knots), new DoubleMatrix2D(coefMatrix), 4, 1);
  }

  @Override
  public PiecewisePolynomialResult interpolate(final double[] xValues, final double[][] yValuesMatrix) {
    throw new IllegalArgumentException("Method with multidimensional yValues is not supported");
  }

  /**
   * Since this interpolation method introduces new breakpoints in certain cases, {@link PiecewisePolynomialResultsWithSensitivity} is not well-defined
   * Instead the node sensitivity is computed in {@link MonotoneConvexSplineInterpolator1D} via {@link Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle}
   * @param xValues The xValues
   * @param yValues The yValues
   * @return NotImplementedException
   */
  @Override
  public PiecewisePolynomialResultsWithSensitivity interpolateWithSensitivity(final double[] xValues, final double[] yValues) {
    throw new NotImplementedException();
  }

  /**
   * @param xValues
   * @return Intervals of xValues, ( xValues_i - xValues_{i-1} )
   */
  private double[] intervalsCalculator(final double[] xValues) {

    final int nDataPts = xValues.length;
    final double[] intervals = new double[nDataPts - 1];

    for (int i = 0; i < nDataPts - 1; ++i) {
      intervals[i] = xValues[i + 1] - xValues[i];
    }

    return intervals;
  }

  /**
   * @param yValues Y values of data
   * @param intervals Intervals of x data
   * @return Slopes
   */
  private double[] slopesCalculator(final double[] yValues, final double[] intervals) {

    final int nDataPts = yValues.length;
    final double[] slopes = new double[nDataPts - 1];

    for (int i = 0; i < nDataPts - 1; ++i) {
      slopes[i] = (yValues[i + 1] - yValues[i]) / intervals[i];
    }

    return slopes;
  }

  /**
   * @param intervals
   * @param slopes
   * @return First derivative at knots
   */
  private double[] firstDiffFinder(final double[] intervals, final double[] slopes) {
    final int nInts = intervals.length;
    final double[] res = new double[nInts + 1];

    res[0] = endpointFirst(intervals[0], intervals[1], slopes[0], slopes[1]);
    res[nInts] = endpointFirst(intervals[nInts - 1], intervals[nInts - 2], slopes[nInts - 1], slopes[nInts - 2]);

    for (int i = 1; i < nInts; ++i) {
      if (Math.signum(slopes[i]) != Math.signum(slopes[i - 1]) | (slopes[i] == 0 | slopes[i - 1] == 0)) {
        res[i] = 0.;
      } else {
        final double den1 = 2. * intervals[i] + intervals[i - 1];
        final double den2 = intervals[i] + 2. * intervals[i - 1];
        res[i] = 3. * (intervals[i] + intervals[i - 1]) / (den1 / slopes[i - 1] + den2 / slopes[i]);
      }
    }

    return res;
  }

  /**
   * Estimate first derivatives at endpoints
   * @param ints1 First (last) interval
   * @param ints2 Second (second last) Interval
   * @param grads1 First (last) slope
   * @param grads2 Second (second last) slope
   * @return Slope at the first (last) data point
   */
  private double endpointFirst(final double ints1, final double ints2, final double grads1, final double grads2) {
    final double val = (2. * ints1 + ints2) * grads1 / (ints1 + ints2) - ints1 * grads2 / (ints1 + ints2);

    if (Math.signum(val) != Math.signum(grads1)) {
      return 0.;
    }
    if (Math.signum(grads1) != Math.signum(grads2) && Math.abs(val) > 3. * Math.abs(grads1)) {
      return 3. * grads1;
    }
    return val;
  }

  /**
   * @param slopes
   * @return sign(slopes_{i + 1} - slopes_i)
   */
  private double[] betaCalculator(final double[] slopes) {
    final int nSlopes = slopes.length;
    final double[] res = new double[nSlopes + 1];

    for (int i = 0; i < nSlopes - 1; ++i) {
      res[i + 1] = Math.signum(slopes[i + 1] - slopes[i]);
    }
    res[0] = res[1];
    res[nSlopes] = res[nSlopes - 1];

    return res;
  }

  /**
   * In the notation i =1,2,...,N-1,
   * R_{2*i-1} = 6*slopes_i - 4*first_i - 2*first_{i+1}
   * R_{2*i}   = - 6*slopes_i + 2*first_i + 4*first_{i+1}
   * @param slopes
   * @param first First derivatives
   * @return R functions
   */
  private double[] rValuesCalculator(final double[] slopes, final double[] first) {
    final int nData = first.length;
    final double[] res = new double[2 * nData];

    for (int i = 1; i < nData - 1; ++i) {
      res[2 * i] = 2. * first[i - 1] + 4. * first[i] - 6. * slopes[i - 1];
      res[2 * i - 1] = -4. * first[i - 1] - 2. * first[i] + 6. * slopes[i - 1];
      if (Math.abs(res[2 * i]) <= 0.1 * ERROR) {
        res[2 * i] = 0.;
      }
      if (Math.abs(res[2 * i - 1]) <= 0.1 * ERROR) {
        res[2 * i - 1] = 0.;
      }
    }
    res[0] = INF;
    res[2 * nData - 1] = INF;

    return res;
  }

  /**
   * Check beta_i * R_{2*i-1} \geq 0 and beta_{i+1} *R_{2*i} \geq 0
   * @param beta
   * @param rValues
   * @return True if the two inequalities are satisfied
   */
  private boolean signChecker(final double[] beta, final double[] rValues) {
    final int nData = beta.length;

    for (int i = 1; i < nData - 2; ++i) {
      if (beta[i] * rValues[2 * i + 1] < 0 | beta[i + 1] * rValues[2 * i + 2] < 0) {
        return false;
      }
    }
    return true;
  }

  /**
   * As the double sweep algorithm determines only intervals [minTmp, maxTmp] which should contain first derivative, choice of the values of first derivatives is not unique.
   * Infeasibility is returned in some cases, which is resolved by another choice of first derivatives within the allowed intervals, [minTmp, maxTmp].
   * @param intervals
   * @param slopes
   * @param beta
   * @param first
   * @return First derivatives satisfying convexity conditions
   */
  private double[] firstDiffSweep(final double[] intervals, final double[] slopes, final double[] beta, final double[] first) {
    final int nData = intervals.length + 1;
    final double[] res = new double[nData];

    double minVal = 0.;
    double maxVal = 0.;

    if (beta[0] > 0) {
      minVal = 3. * slopes[0] - 2. * slopes[1];
      maxVal = slopes[0];
    } else {
      minVal = slopes[0];
      maxVal = 3. * slopes[0] - 2. * slopes[1];
    }

    for (int i = 1; i < nData; ++i) {
      double minTmp = 0.;
      double maxTmp = 0.;
      if (beta[i - 1] == 0.) {
        if (beta[i] == 0.) {
          minTmp = -INF;
          maxTmp = INF;
        } else {
          if (beta[i] > 0.) {
            minTmp = 0.5 * (3. * slopes[i - 1] - maxVal);
            maxTmp = INF;
          } else {
            minTmp = -INF;
            maxTmp = Math.min(slopes[i - 1], 0.5 * (3. * slopes[i - 1] - minVal));
          }
        }
      } else {
        if (beta[i - 1] > 0.) {
          if (beta[i] == 0.) {
            minTmp = slopes[i - 1];
            maxTmp = 3. * slopes[i - 1] - 2. * minVal;
          } else {
            if (beta[i] > 0.) {
              minTmp = Math.max(slopes[i - 1], 0.5 * (3. * slopes[i - 1] - maxVal));
              maxTmp = 3. * slopes[i - 1] - 2. * minVal;
            } else {
              minTmp = -INF;
              maxTmp = Math.min(3. * slopes[i - 1] - 2. * minVal, 0.5 * (3. * slopes[i - 1] - minVal));
            }
          }
        } else {
          if (beta[i] == 0) {
            minTmp = 3. * slopes[i - 1] - 2. * minVal;
            maxTmp = INF;
          } else {
            if (beta[i] > 0.) {
              minTmp = Math.max(3. * slopes[i - 1] - 2. * maxVal, 0.5 * (3. * slopes[i - 1] - maxVal));
              maxTmp = INF;
            } else {
              minTmp = 3. * slopes[i - 1] - 2. * maxVal;
              maxTmp = Math.min(slopes[i - 1], 0.5 * (3. * slopes[i - 1] - minVal));
            }
          }
        }
      }
      minVal = minTmp;
      maxVal = maxTmp;

      if (minTmp != -INF && maxTmp != INF) {
        res[i] = 0.5 * (minTmp + maxTmp);
      } else {
        if (first[i] < minTmp) {
          res[i] = minTmp != INF ? minTmp : first[i];
        } else {
          if (first[i] > maxTmp) {
            res[i] = maxTmp != -INF ? maxTmp : first[i];
          } else {
            res[i] = first[i];
          }
        }
      }
    }

    if (minVal > maxVal) {
      res[nData - 1] = first[nData - 1];
    } else {
      if (first[nData - 1] < minVal) {
        res[nData - 1] = minVal != INF ? minVal : first[nData - 1];
      } else {
        if (first[nData - 1] > maxVal) {
          res[nData - 1] = maxVal != -INF ? maxVal : first[nData - 1];
        } else {
          res[nData - 1] = first[nData - 1];
        }
      }
    }

    double minTmp = 0.;
    double maxTmp = 0.;
    for (int i = nData - 2; i > -1; --i) {
      minTmp = 0.;
      maxTmp = 0.;
      if (beta[i] == 0.) {
        if (beta[i + 1] == 0.) {
          minTmp = -INF;
          maxTmp = INF;
        } else {
          if (beta[i + 1] > 0.) {
            minTmp = 3. * slopes[i] - 2. * res[i + 1];
            maxTmp = INF;
          } else {
            minTmp = -INF;
            maxTmp = 3. * slopes[i] - 2. * res[i + 1];
          }
        }
      } else {
        if (beta[i] > 0.) {
          if (beta[i + 1] == 0.) {
            minTmp = -INF;
            maxTmp = 0.5 * (3. * slopes[i] - res[i + 1]);
          } else {
            if (beta[i + 1] > 0.) {
              minTmp = 3. * slopes[i] - 2. * res[i + 1];
              maxTmp = 0.5 * (3. * slopes[i] - res[i + 1]);
            } else {
              minTmp = -INF;
              maxTmp = Math.min(3. * slopes[i] - 2. * res[i + 1], 0.5 * (3. * slopes[i] - res[i + 1]));
            }
          }
        } else {
          if (beta[i + 1] == 0.) {
            minTmp = 0.5 * (3. * slopes[i] - res[i + 1]);
            maxTmp = INF;
          } else {
            if (beta[i + 1] > 0.) {
              minTmp = Math.max(3. * slopes[i] - 2. * res[i + 1], 0.5 * (3. * slopes[i] - res[i + 1]));
              maxTmp = INF;
            } else {
              minTmp = 0.5 * (3. * slopes[i] - res[i + 1]);
              maxTmp = 3. * slopes[i] - 2. * res[i + 1];
            }
          }
        }
      }

      if (minTmp > maxTmp) {
        throw new IllegalArgumentException("Local monotonicity can not be preserved");
      }
      if (res[i] < minTmp) {
        res[i] = minTmp != INF ? minTmp : res[i];
      } else {
        if (res[i] > maxTmp) {
          res[i] = maxTmp != -INF ? maxTmp : res[i];
        }
      }
    }

    return res;
  }

  /**
   * @param intervals
   * @param beta
   * @param rValues
   * @return Second derivatives satisfying local monotonicity
   */
  private double[] secondDiffFinder(final double[] intervals, final double[] beta, final double[] rValues) {
    final int nData = intervals.length + 1;
    final double[] res = new double[nData];

    for (int i = 1; i < nData - 1; ++i) {
      res[i] = (rValues[2 * i + 1] > 0 && rValues[2 * i] > 0) ? beta[i] * Math.min(beta[i] * rValues[2 * i + 1] / intervals[i], beta[i] * rValues[2 * i] / intervals[i - 1]) : 0.;
    }
    res[0] = rValues[1] > 0 ? beta[0] * rValues[1] / intervals[0] : 0.;
    res[nData - 1] = rValues[2 * (nData - 1)] > 0 ? beta[nData - 1] * rValues[2 * (nData - 1)] / intervals[nData - 2] : 0.;

    return res;
  }

  /**
   * Extra knots are introduced at xValues[i] + tau[i] * intervals[i] and xValues[i + 1] - tau[i] * intervals[i]
   * @param intervals
   * @param slopes
   * @param beta
   * @param first
   * @param second
   * @return tau
   */
  private double[] tauFinder(final double[] intervals, final double[] slopes, final double[] beta, final double[] first, final double[] second) {
    final int nData = intervals.length + 1;
    final double[] res = new double[nData - 1];
    Arrays.fill(res, 1. / 3.);

    for (int i = 1; i < nData - 2; ++i) {
      boolean ineq1 = false;
      boolean ineq2 = false;
      final double bound1 = 6. * slopes[i] * beta[i];
      final double bound2 = 6. * slopes[i] * beta[i + 1];
      double ref1 = (4. * first[i] + 2. * first[i + 1] + intervals[i] * second[i] * res[i] * (2. - res[i]) - intervals[i] * second[i + 1] * res[i] * (1. - res[i])) * beta[i];
      double ref2 = (2. * first[i] + 4. * first[i + 1] + intervals[i] * second[i] * res[i] * (1. - res[i]) - intervals[i] * second[i + 1] * res[i] * (2. - res[i])) * beta[i + 1];
      while (ineq1 == false) {
        if (ref1 - ERROR * Math.abs(ref1) <= bound1 + ERROR * Math.abs(bound1)) {
          ineq1 = true;
        } else {
          res[i] *= 0.8;
        }
        if (res[i] < ERROR / 100.) {
          throw new IllegalArgumentException("Spline is not found");
        }
        ref1 = (4. * first[i] + 2. * first[i + 1] + intervals[i] * second[i] * res[i] * (2. - res[i]) - intervals[i] * second[i + 1] * res[i] * (1. - res[i])) * beta[i];
      }
      while (ineq2 == false) {
        if (ref2 + ERROR * Math.abs(ref2) >= bound2 - ERROR * Math.abs(bound2)) {
          ineq2 = true;
        } else {
          res[i] *= 0.8;
        }
        if (res[i] < ERROR / 100.) {
          throw new IllegalArgumentException("Spline is not found");
        }
        ref2 = (2. * first[i] + 4. * first[i + 1] + intervals[i] * second[i] * res[i] * (1. - res[i]) - intervals[i] * second[i + 1] * res[i] * (2. - res[i])) * beta[i + 1];
      }

    }

    return res;
  }

  /**
   * @param xValues
   * @param intervals
   * @param tau
   * @return {... , xValues[i], xValues[i] + tau[i] * intervals[i], xValues[i + 1] - tau[i] * intervals[i], xValues[i + 1], ...}
   */
  private double[] knotsProvider(final double[] xValues, final double[] intervals, final double[] tau) {
    final int nData = xValues.length;
    final double[] res = new double[3 * nData - 2];

    for (int i = 0; i < nData - 1; ++i) {
      res[3 * i] = xValues[i];
      res[3 * i + 1] = xValues[i] + tau[i] * intervals[i];
      res[3 * i + 2] = xValues[i + 1] - tau[i] * intervals[i];
    }
    res[3 * (nData - 1)] = xValues[nData - 1];

    return res;
  }

  /**
   * Determine value, first derivative, second derivative and third derivative of interpolant at extra knots
   * @param yValues
   * @param intervals
   * @param slopes
   * @param first
   * @param second
   * @param tau
   * @return Coefficient matrix whose i-th row vector is {a_n, a_{n-1}, ... } of f(x) = a_n * (x-x_i)^n + a_{n-1} * (x-x_i)^{n-1} +... for the i-th interval
   */
  private double[][] solve(final double[] yValues, final double[] intervals, final double[] slopes, final double[] first, final double[] second, final double[] tau) {
    final int nData = yValues.length;
    final double[][] res = new double[3 * (nData - 1)][4];

    final double[] secNewKnots1 = new double[nData - 1];
    final double[] secNewKnots2 = new double[nData - 1];
    for (int i = 0; i < nData - 1; ++i) {
      secNewKnots1[i] = 6. * slopes[i] / intervals[i] / (1. - tau[i]) - 4. * first[i] / intervals[i] / (1. - tau[i]) - 2. * first[i + 1] / intervals[i] / (1. - tau[i]) - tau[i] * (2. - tau[i]) *
          second[i] / (1. - tau[i]) + tau[i] * second[i + 1];
      secNewKnots2[i] = -6. * slopes[i] / intervals[i] / (1. - tau[i]) + 4. * first[i + 1] / intervals[i] / (1. - tau[i]) + 2. * first[i] / intervals[i] / (1. - tau[i]) - tau[i] * (2. - tau[i]) *
          second[i + 1] / (1. - tau[i]) + tau[i] * second[i];
    }

    for (int i = 0; i < nData - 1; ++i) {
      res[3 * i][0] = (secNewKnots1[i] - second[i]) / 6. / tau[i] / intervals[i];
      res[3 * i][1] = 0.5 * second[i];
      res[3 * i][2] = first[i];
      res[3 * i][3] = yValues[i];
      res[3 * i + 1][0] = (secNewKnots2[i] - secNewKnots1[i]) / 6. / (1. - 2. * tau[i]) / intervals[i];
      res[3 * i + 1][1] = 0.5 * secNewKnots1[i];
      res[3 * i + 1][2] = first[i] + (second[i] + secNewKnots1[i]) * tau[i] * intervals[i] * 0.5;
      res[3 * i + 1][3] = yValues[i] + tau[i] * intervals[i] * first[i] + (2. * second[i] + secNewKnots1[i]) * tau[i] * intervals[i] * tau[i] * intervals[i] / 6.;
      res[3 * i + 2][0] = (second[i + 1] - secNewKnots2[i]) / 6. / tau[i] / intervals[i];
      res[3 * i + 2][1] = 0.5 * secNewKnots2[i];
      res[3 * i + 2][2] = first[i + 1] - (second[i + 1] + secNewKnots2[i]) * tau[i] * intervals[i] * 0.5;
      res[3 * i + 2][3] = yValues[i + 1] - tau[i] * intervals[i] * first[i + 1] + (2. * second[i + 1] + secNewKnots2[i]) * tau[i] * intervals[i] * tau[i] * intervals[i] / 6.;
    }

    return res;
  }

}
