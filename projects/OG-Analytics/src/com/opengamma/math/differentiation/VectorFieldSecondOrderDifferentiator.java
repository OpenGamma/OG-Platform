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

  private static final double EPS = 1e-4;
  private static final double TWO_EPS;
  private static final double EPS_SQ;
  static {
    TWO_EPS = 2 * EPS;
    EPS_SQ = EPS * EPS;
  }

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
          xData[j] += EPS;
          up = function.evaluate(x);
          xData[j] -= TWO_EPS;
          down = function.evaluate(x);
          for (i = 0; i < m; i++) {
            res[i][j] = (up.getEntry(i) + down.getEntry(i) - 2 * y.getEntry(i)) / EPS_SQ;
          }
          xData[j] = oldValue;
        }
        return new DoubleMatrix2D(res);
      }
    };
  }

  public Function1D<DoubleMatrix1D, DoubleMatrix2D[]> differentiateFull(final Function1D<DoubleMatrix1D, DoubleMatrix1D> function) {
    return new Function1D<DoubleMatrix1D, DoubleMatrix2D[]>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public DoubleMatrix2D[] evaluate(final DoubleMatrix1D x) {
        Validate.notNull(x, "x");
        final DoubleMatrix1D y = function.evaluate(x);
        final int n = x.getNumberOfElements();
        final int m = y.getNumberOfElements();
        final double[] xData = x.getData();
        double oldValueJ, oldValueK;
        final double[][][] res = new double[m][n][n];
        int i, j, k;

        DoubleMatrix1D up, down, upup, updown, downup, downdown;
        for (j = 0; j < n; j++) {
          oldValueJ = xData[j];
          xData[j] += EPS;
          up = function.evaluate(x);
          xData[j] -= TWO_EPS;
          down = function.evaluate(x);
          for (i = 0; i < m; i++) {
            res[i][j][j] = (up.getEntry(i) + down.getEntry(i) - 2 * y.getEntry(i)) / EPS_SQ;
          }
          for (k = j + 1; k < n; k++) {
            oldValueK = xData[k];
            xData[k] += EPS;
            downup = function.evaluate(x);
            xData[k] -= TWO_EPS;
            downdown = function.evaluate(x);
            xData[j] += TWO_EPS;
            updown = function.evaluate(x);
            xData[k] += TWO_EPS;
            upup = function.evaluate(x);
            xData[k] = oldValueK;
            for (i = 0; i < m; i++) {
              res[i][j][k] = (upup.getEntry(i) + downdown.getEntry(i) - updown.getEntry(i) - downup.getEntry(i)) / 4 / EPS_SQ;
            }
          }
          xData[j] = oldValueJ;
        }
        DoubleMatrix2D[] mres = new DoubleMatrix2D[m];
        for (i = 0; i < m; i++) {
          for (j = 0; j < n; j++) {
            for (k = 0; k < j; k++) {
              res[i][j][k] = res[i][k][j];
            }
          }
          mres[i] = new DoubleMatrix2D(res[i]);
        }
        return mres;
      }
    };
  }
}
