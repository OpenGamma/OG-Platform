/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.differentiation;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;

/**
 * Differentiates a vector field (i.e. there is a vector value for every point
 * in some vector space) with respect to the vector space using finite
 * difference.
 * <p>
 * For a function $\mathbf{y} = f(\mathbf{x})$ where $\mathbf{x}$ is a
 * n-dimensional vector and $\mathbf{y}$ is a m-dimensional vector, this class
 * produces the Jacobian function $\mathbf{J}(\mathbf{x})$, i.e. a function
 * that returns the Jacobian for each point $\mathbf{x}$, where
 * $\mathbf{J}$ is the $m \times n$ matrix $\frac{dy_i}{dx_j}$
 */
public class VectorFieldFirstOrderDifferentiator implements Differentiator<DoubleMatrix1D, DoubleMatrix1D, DoubleMatrix2D> {
  private static final double DEFAULT_EPS = 1e-5;
  private static final FiniteDifferenceType DIFF_TYPE = FiniteDifferenceType.CENTRAL;

  private final double _eps;
  private final double _twoEps;
  private final FiniteDifferenceType _differenceType;

  /**
   * Uses the default values of differencing type (central) and eps (1e-5).
   */
  public VectorFieldFirstOrderDifferentiator() {
    this(DIFF_TYPE, DEFAULT_EPS);
  }

  /**
   * Uses the default value of eps (10<sup>-5</sup>)
   * @param differenceType The differencing type to be used in calculating the gradient function
   */
  public VectorFieldFirstOrderDifferentiator(final FiniteDifferenceType differenceType) {
    this(differenceType, DEFAULT_EPS);
  }

  /**
   * Approximates the derivative of a vector function by finite difference. If the size of the domain is very small or very large, consider re-scaling first.
   * @param differenceType {@link FiniteDifferenceType#FORWARD}, {@link FiniteDifferenceType#BACKWARD}, or {@link FiniteDifferenceType#CENTRAL}. In most situations,
   * {@link FiniteDifferenceType#CENTRAL} is preferable. Not null
   * @param eps The step size used to approximate the derivative. If this value is too small, the result will most likely be dominated by noise.
   * Use around 10<sup>-5</sup> times the domain size.
   */
  public VectorFieldFirstOrderDifferentiator(final FiniteDifferenceType differenceType, final double eps) {
    Validate.notNull(differenceType);
    _differenceType = differenceType;
    _eps = eps;
    _twoEps = 2 * _eps;
  }

  /**
   * Approximates the derivative of a vector function by finite difference. If the size of the domain is very small or very large, consider re-scaling first.
   * @param eps The step size used to approximate the derivative. If this value is too small, the result will most likely be dominated by noise.
   * Use around 10<sup>-5</sup> times the domain size.
   */
  public VectorFieldFirstOrderDifferentiator(final double eps) {
    this(DIFF_TYPE, eps);
  }

  @Override
  public Function1D<DoubleMatrix1D, DoubleMatrix2D> differentiate(final Function1D<DoubleMatrix1D, DoubleMatrix1D> function) {
    Validate.notNull(function);
    switch (_differenceType) {
      case FORWARD:
        return new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {

          @SuppressWarnings("synthetic-access")
          @Override
          public DoubleMatrix2D evaluate(final DoubleMatrix1D x) {
            Validate.notNull(x, "x");
            final DoubleMatrix1D y = function.evaluate(x);
            final int n = x.getNumberOfElements();
            final int m = y.getNumberOfElements();
            final double[] xData = x.getData();
            double oldValue;
            final double[][] res = new double[m][n];
            int i, j;
            DoubleMatrix1D up;
            for (j = 0; j < n; j++) {
              oldValue = xData[j];
              xData[j] += _eps;
              up = function.evaluate(x);
              for (i = 0; i < m; i++) {
                res[i][j] = (up.getEntry(i) - y.getEntry(i)) / _eps;
              }
              xData[j] = oldValue;
            }
            return new DoubleMatrix2D(res);
          }
        };
      case CENTRAL:
        return new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {

          @SuppressWarnings("synthetic-access")
          @Override
          public DoubleMatrix2D evaluate(final DoubleMatrix1D x) {
            Validate.notNull(x, "x");
            final DoubleMatrix1D y = function.evaluate(x); // need this unused evaluation to get size of y
            final int n = x.getNumberOfElements();
            final int m = y.getNumberOfElements();
            final double[] xData = x.getData();
            double oldValue;
            final double[][] res = new double[m][n];
            int i, j;
            DoubleMatrix1D up, down;
            for (j = 0; j < n; j++) {
              oldValue = xData[j];
              xData[j] += _eps;
              up = function.evaluate(x);
              xData[j] -= _twoEps;
              down = function.evaluate(x);
              for (i = 0; i < m; i++) {
                res[i][j] = (up.getEntry(i) - down.getEntry(i)) / _twoEps;
              }
              xData[j] = oldValue;
            }
            return new DoubleMatrix2D(res);
          }
        };
      case BACKWARD:
        return new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {

          @SuppressWarnings("synthetic-access")
          @Override
          public DoubleMatrix2D evaluate(final DoubleMatrix1D x) {
            Validate.notNull(x, "x");
            final DoubleMatrix1D y = function.evaluate(x);
            final int n = x.getNumberOfElements();
            final int m = y.getNumberOfElements();
            final double[] xData = x.getData();
            double oldValue;
            final double[][] res = new double[m][n];
            int i, j;
            DoubleMatrix1D down;
            for (j = 0; j < n; j++) {
              oldValue = xData[j];
              xData[j] -= _eps;
              down = function.evaluate(x);
              for (i = 0; i < m; i++) {
                res[i][j] = (y.getEntry(i) - down.getEntry(i)) / _eps;
              }
              xData[j] = oldValue;
            }
            return new DoubleMatrix2D(res);
          }
        };
      default:
        throw new IllegalArgumentException("Can only handle forward, backward and central differencing");
    }
  }

  @Override
  public Function1D<DoubleMatrix1D, DoubleMatrix2D> differentiate(final Function1D<DoubleMatrix1D, DoubleMatrix1D> function, final Function1D<DoubleMatrix1D, Boolean> domain) {
    Validate.notNull(function);
    Validate.notNull(domain);

    final double[] wFwd = new double[] {-3., 4., -1. };
    final double[] wCent = new double[] {-1., 0., 1. };
    final double[] wBack = new double[] {1., -4., 3. };

    return new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public DoubleMatrix2D evaluate(final DoubleMatrix1D x) {
        Validate.notNull(x, "x");
        ArgumentChecker.isTrue(domain.evaluate(x), "point {} is not in the function domain", x.toString());

        final DoubleMatrix1D mid = function.evaluate(x); // need this unused evaluation to get size of y
        final int n = x.getNumberOfElements();
        final int m = mid.getNumberOfElements();
        final double[] xData = x.getData();
        double oldValue;
        final double[][] res = new double[m][n];
        int i, j;
        final DoubleMatrix1D[] y = new DoubleMatrix1D[3];
        double[] w;

        for (j = 0; j < n; j++) {
          oldValue = xData[j];
          xData[j] += _eps;
          if (!domain.evaluate(x)) {
            xData[j] = oldValue - _twoEps;
            if (!domain.evaluate(x)) {
              throw new MathException("cannot get derivative at point " + x.toString() + " in direction " + j);
            }
            y[2] = mid;
            y[0] = function.evaluate(x);
            xData[j] = oldValue - _eps;
            y[1] = function.evaluate(x);
            w = wBack;
          } else {
            final DoubleMatrix1D temp = function.evaluate(x);
            xData[j] = oldValue - _eps;
            if (!domain.evaluate(x)) {
              y[0] = mid;
              y[1] = temp;
              xData[j] = oldValue + _twoEps;
              y[2] = function.evaluate(x);
              w = wFwd;
            } else {
              y[2] = temp;
              xData[j] = oldValue - _eps;
              y[0] = function.evaluate(x);
              y[1] = mid;
              w = wCent;
            }
          }

          for (i = 0; i < m; i++) {
            double sum = 0;
            for (int k = 0; k < 3; k++) {
              if (w[k] != 0.0) {
                sum += w[k] * y[k].getEntry(i);
              }
            }
            res[i][j] = sum / _twoEps;
          }
          xData[j] = oldValue;
        }
        return new DoubleMatrix2D(res);
      }
    };

  }

}
