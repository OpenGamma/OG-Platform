/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.differentiation;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class VectorFieldSecondOrderDifferentiator implements Differentiator<DoubleMatrix1D, DoubleMatrix1D, DoubleMatrix2D> {

  private final double _eps = 1e-5;
  private final double _twoEps = 2e-5;
  private final double _epsSqr = 1e-10;

  @Override
  public Function1D<DoubleMatrix1D, DoubleMatrix2D> differentiate(final Function1D<DoubleMatrix1D, DoubleMatrix1D> function) {
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

        DoubleMatrix1D up, down;
        for (j = 0; j < n; j++) {
          oldValue = xData[j];
          xData[j] += _eps;
          up = function.evaluate(x);
          xData[j] -= _twoEps;
          down = function.evaluate(x);
          for (i = 0; i < m; i++) {
            res[i][j] = (up.getEntry(i) + down.getEntry(i) - 2 * y.getEntry(i)) / _epsSqr;
          }
          xData[j] = oldValue;
        }
        return new DoubleMatrix2D(res);
      }
    };
  }

}
