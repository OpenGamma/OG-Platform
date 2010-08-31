/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.differentiation;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * Differentiates a vector field (i.e. there is a vector value for every point in some vector space) with respect to the vector space using finite difference. For a function <b>y</b> =f(<b>x</b>) 
 * where <b>x</b> is a n-dimensional vector and <b>y</b> is a m-dimensional vector this produces the Jacobian function, <b>J</b>(<b>x</b>), i.e. a function that returns the Jacobian for each point <b>x</b>,
 * where <b>J</b> is the m by n matrix dy_i/dx_j
 */
public class VectorFieldFirstOrderDifferentiator implements Derivative<DoubleMatrix1D, DoubleMatrix1D, DoubleMatrix2D> {

  private static final double DEFAULT_EPS = 1e-5;
  private static final double MIN_EPS = Math.sqrt(Double.MIN_NORMAL);
  private static final FiniteDifferenceType DIFF_TYPE = FiniteDifferenceType.CENTRAL;

  private final double _eps;
  private final double _twoEps;
  private final FiniteDifferenceType _differenceType;

  public VectorFieldFirstOrderDifferentiator() {
    this(DIFF_TYPE, DEFAULT_EPS);
  }

  public VectorFieldFirstOrderDifferentiator(final FiniteDifferenceType differenceType) {
    this(differenceType, DEFAULT_EPS);
  }

  public VectorFieldFirstOrderDifferentiator(final FiniteDifferenceType differenceType, final double eps) {
    Validate.notNull(differenceType);
    _differenceType = differenceType;
    _eps = eps;
    _twoEps = 2 * _eps;
  }

  @Override
  public Function1D<DoubleMatrix1D, DoubleMatrix2D> derivative(final Function1D<DoubleMatrix1D, DoubleMatrix1D> function) {
    Validate.notNull(function);
    switch (_differenceType) {
      case FORWARD:
        return new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {

          @SuppressWarnings("synthetic-access")
          @Override
          public DoubleMatrix2D evaluate(DoubleMatrix1D x) {
            DoubleMatrix1D y = function.evaluate(x);
            int n = x.getNumberOfElements();
            int m = y.getNumberOfElements();
            double[] xData = x.getData();
            double oldValue;
            double[][] res = new double[m][n];
            int i, j;
            DoubleMatrix1D up;
            for (j = 0; j < n; j++) {
              oldValue = xData[j];
              xData[j] += _eps;
              up = function.evaluate(x); // x has been changed via the access to the underlying data
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
          public DoubleMatrix2D evaluate(DoubleMatrix1D x) {
            DoubleMatrix1D y = function.evaluate(x); // need this unused evaluation to get size of y
            int n = x.getNumberOfElements();
            int m = y.getNumberOfElements();
            double[] xData = x.getData();
            double oldValue;
            double[][] res = new double[m][n];
            int i, j;
            DoubleMatrix1D up, down;
            for (j = 0; j < n; j++) {
              oldValue = xData[j];
              xData[j] += _eps;
              up = function.evaluate(x); // x has been changed via the access to the underlying data
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
          public DoubleMatrix2D evaluate(DoubleMatrix1D x) {
            DoubleMatrix1D y = function.evaluate(x);
            int n = x.getNumberOfElements();
            int m = y.getNumberOfElements();
            double[] xData = x.getData();
            double oldValue;
            double[][] res = new double[m][n];
            int i, j;
            DoubleMatrix1D down;
            for (j = 0; j < n; j++) {
              oldValue = xData[j];
              xData[j] -= _eps;
              down = function.evaluate(x); // x has been changed via the access to the underlying data
              for (i = 0; i < m; i++) {
                res[i][j] = (y.getEntry(i) - down.getEntry(i)) / _eps;
              }
              xData[j] = oldValue;
            }
            return new DoubleMatrix2D(res);
          }
        };
    }
    throw new IllegalArgumentException("Can only handle forward, backward and central differencing");
  }

}
