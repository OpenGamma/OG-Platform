/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.differentiation;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class MatrixFieldFirstOrderDifferentiator implements Differentiator<DoubleMatrix1D, DoubleMatrix2D, DoubleMatrix2D[]> {

  private static final MatrixAlgebra MA = new OGMatrixAlgebra();
  private static final double DEFAULT_EPS = 1e-5;

  private final double _eps;
  private final double _twoEps;
  private final double _oneOverTwpEps;

  public MatrixFieldFirstOrderDifferentiator() {
    _eps = DEFAULT_EPS;
    _twoEps = 2 * DEFAULT_EPS;
    _oneOverTwpEps = 1.0 / _twoEps;
  }

  public MatrixFieldFirstOrderDifferentiator(final double eps) {
    ArgumentChecker.isTrue(eps > 1e-15, "eps of {} is below machine tolerance of 1e-15. Please choose a higher value or use default", eps);
    _eps = eps;
    _twoEps = 2 * eps;
    _oneOverTwpEps = 1.0 / _twoEps;
  }

  @Override
  public Function1D<DoubleMatrix1D, DoubleMatrix2D[]> differentiate(final Function1D<DoubleMatrix1D, DoubleMatrix2D> function) {
    Validate.notNull(function);

    return new Function1D<DoubleMatrix1D, DoubleMatrix2D[]>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public DoubleMatrix2D[] evaluate(final DoubleMatrix1D x) {
        Validate.notNull(x, "x");
        final int n = x.getNumberOfElements();

        final DoubleMatrix2D[] res = new DoubleMatrix2D[n];
        final double[] xData = x.getData();
        for (int i = 0; i < n; i++) {
          final double oldValue = xData[i];
          xData[i] += _eps;
          final DoubleMatrix2D up = function.evaluate(x);
          xData[i] -= _twoEps;
          final DoubleMatrix2D down = function.evaluate(x);
          res[i] = (DoubleMatrix2D) MA.scale(MA.subtract(up, down), _oneOverTwpEps); //TODO have this in one operation
          xData[i] = oldValue;
        }
        return res;
      }
    };
  }

  @Override
  public Function1D<DoubleMatrix1D, DoubleMatrix2D[]> differentiate(final Function1D<DoubleMatrix1D, DoubleMatrix2D> function, final Function1D<DoubleMatrix1D, Boolean> domain) {
    Validate.notNull(function);
    Validate.notNull(domain);

    final double[] wFwd = new double[] {-3. / _twoEps, 4. / _twoEps, -1. / _twoEps };
    final double[] wCent = new double[] {-1. / _twoEps, 0., 1. / _twoEps };
    final double[] wBack = new double[] {1. / _twoEps, -4. / _twoEps, 3. / _twoEps };

    return new Function1D<DoubleMatrix1D, DoubleMatrix2D[]>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public DoubleMatrix2D[] evaluate(final DoubleMatrix1D x) {
        Validate.notNull(x, "x");
        ArgumentChecker.isTrue(domain.evaluate(x), "point {} is not in the function domain", x.toString());

        final int n = x.getNumberOfElements();
        final double[] xData = x.getData();
        double oldValue;
        final DoubleMatrix2D[] y = new DoubleMatrix2D[3];
        final DoubleMatrix2D[] res = new DoubleMatrix2D[n];
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
            final DoubleMatrix2D temp = function.evaluate(x);
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
          res[i] = (DoubleMatrix2D) MA.add(MA.scale(y[0], w[0]), MA.scale(y[2], w[2]));
          if (w[1] != 0) {
            res[i] = (DoubleMatrix2D) MA.add(res[i], MA.scale(y[1], w[1]));
          }
          xData[i] = oldValue;
        }
        return res;
      }
    };

  }

}
