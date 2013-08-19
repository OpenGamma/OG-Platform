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
import com.opengamma.util.ArgumentChecker;

/**
 * Differentiates a scalar field (i.e. there is a scalar value for every point
 * in some vector space) with respect to the vector space using finite
 * difference.
 * <p>
 * For a function $y = f(\mathbf{x})$ where $\mathbf{x}$ is a n-dimensional
 * vector and $y$ is a scalar, this class produces a gradient function
 * $\mathbf{g}(\mathbf{x})$, i.e. a function that returns the gradient for each
 * point $\mathbf{x}$, where $\mathbf{g}$ is the n-dimensional vector
 * $\frac{dy}{dx_i}$.
 */
public class ScalarFieldFirstOrderDifferentiator implements Differentiator<DoubleMatrix1D, Double, DoubleMatrix1D> {
  private static final double DEFAULT_EPS = 1e-5;
  private static final double MIN_EPS = Math.sqrt(Double.MIN_NORMAL);
  private static final FiniteDifferenceType DIFF_TYPE = FiniteDifferenceType.CENTRAL;

  private final double _eps;
  private final double _twoEps;
  private final FiniteDifferenceType _differenceType;

  /**
   * Uses the default values of differencing type (central) and eps (1e-5).
   */
  public ScalarFieldFirstOrderDifferentiator() {
    this(DIFF_TYPE, DEFAULT_EPS);
  }

  /**
   * Approximates the derivative of a scalar function by finite difference. If the size of the domain is very small or very large, consider re-scaling first.
   * @param differenceType {@link FiniteDifferenceType#FORWARD}, {@link FiniteDifferenceType#BACKWARD}, or {@link FiniteDifferenceType#CENTRAL}. In most situations,
   * {@link FiniteDifferenceType#CENTRAL} is preferable. Not null
   * @param eps The step size used to approximate the derivative. If this value is too small, the result will most likely be dominated by noise.
   * Use around 10<sup>-5</sup> times the domain size.
   */
  public ScalarFieldFirstOrderDifferentiator(final FiniteDifferenceType differenceType, final double eps) {
    Validate.notNull(differenceType);
    if (eps < MIN_EPS) {
      throw new IllegalArgumentException("eps is too small. A good value is 1e-5*size of domain. The minimum value is " + MIN_EPS);
    }
    _differenceType = differenceType;
    _eps = eps;
    _twoEps = 2 * _eps;
  }

  @Override
  public Function1D<DoubleMatrix1D, DoubleMatrix1D> differentiate(final Function1D<DoubleMatrix1D, Double> function) {
    Validate.notNull(function);
    switch (_differenceType) {
      case FORWARD:
        return new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

          @SuppressWarnings("synthetic-access")
          @Override
          public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
            Validate.notNull(x, "x");
            final int n = x.getNumberOfElements();
            final double y = function.evaluate(x);
            final double[] xData = x.getData();
            double oldValue;
            final double[] res = new double[n];
            for (int i = 0; i < n; i++) {
              oldValue = xData[i];
              xData[i] += _eps;
              res[i] = (function.evaluate(x) - y) / _eps;
              xData[i] = oldValue;
            }
            return new DoubleMatrix1D(res);
          }
        };
      case CENTRAL:
        return new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

          @SuppressWarnings("synthetic-access")
          @Override
          public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
            Validate.notNull(x, "x");
            final int n = x.getNumberOfElements();
            final double[] xData = x.getData();
            double oldValue;
            double up, down;
            final double[] res = new double[n];
            for (int i = 0; i < n; i++) {
              oldValue = xData[i];
              xData[i] += _eps;
              up = function.evaluate(x);
              xData[i] -= _twoEps;
              down = function.evaluate(x);
              res[i] = (up - down) / _twoEps;
              xData[i] = oldValue;
            }
            return new DoubleMatrix1D(res);
          }
        };
      case BACKWARD:
        return new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

          @SuppressWarnings("synthetic-access")
          @Override
          public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
            Validate.notNull(x, "x");
            final double y = function.evaluate(x);
            final int n = x.getNumberOfElements();
            final double[] xData = x.getData();
            double oldValue;
            final double[] res = new double[n];
            for (int i = 0; i < n; i++) {
              oldValue = xData[i];
              xData[i] -= _eps;
              res[i] = (y - function.evaluate(x)) / _eps;
              xData[i] = oldValue;
            }
            return new DoubleMatrix1D(res);
          }
        };
      default:
        throw new IllegalArgumentException("Can only handle forward, backward and central differencing");
    }
  }

  @Override
  public Function1D<DoubleMatrix1D, DoubleMatrix1D> differentiate(final Function1D<DoubleMatrix1D, Double> function, final Function1D<DoubleMatrix1D, Boolean> domain) {
    Validate.notNull(function);
    Validate.notNull(domain);

    final double[] wFwd = new double[] {-3. / _twoEps, 4. / _twoEps, -1. / _twoEps };
    final double[] wCent = new double[] {-1. / _twoEps, 0., 1. / _twoEps };
    final double[] wBack = new double[] {1. / _twoEps, -4. / _twoEps, 3. / _twoEps };

    return new Function1D<DoubleMatrix1D, DoubleMatrix1D>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
        Validate.notNull(x, "x");
        ArgumentChecker.isTrue(domain.evaluate(x), "point {} is not in the function domain", x.toString());

        final int n = x.getNumberOfElements();
        final double[] xData = x.getData();
        double oldValue;
        final double[] y = new double[3];
        final double[] res = new double[n];
        double[] w;
        for (int i = 0; i < n; i++) {
          oldValue = xData[i];
          xData[i] += _eps;
          if (!domain.evaluate(x)) {
            xData[i] = oldValue - _twoEps;
            if (!domain.evaluate(x)) {
              throw new MathException("cannot get derivative at point " + x.toString() + " in direction " + i);
            }
            y[0] = function.evaluate(x);
            xData[i] = oldValue;
            y[2] = function.evaluate(x);
            xData[i] = oldValue - _eps;
            y[1] = function.evaluate(x);
            w = wBack;
          } else {
            final double temp = function.evaluate(x);
            xData[i] = oldValue - _eps;
            if (!domain.evaluate(x)) {
              y[1] = temp;
              xData[i] = oldValue;
              y[0] = function.evaluate(x);
              xData[i] = oldValue + _twoEps;
              y[2] = function.evaluate(x);
              w = wFwd;
            } else {
              y[2] = temp;
              xData[i] = oldValue - _eps;
              y[0] = function.evaluate(x);
              w = wCent;
            }
          }
          res[i] = y[0] * w[0] + y[2] * w[2];
          if (w[1] != 0) {
            res[i] += y[1] * w[1];
          }
          xData[i] = oldValue;
        }
        return new DoubleMatrix1D(res);
      }
    };

  }

}
