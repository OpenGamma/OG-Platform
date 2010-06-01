/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class JacobianCalculator extends Function1D<DoubleMatrix1D, DoubleMatrix2D> {

  private final Function1D<DoubleMatrix1D, DoubleMatrix1D> _f;
  private static final double EPS = 1e-8;

  public JacobianCalculator(final Function1D<DoubleMatrix1D, DoubleMatrix1D> function) {
    _f = function;
  }

  @Override
  public DoubleMatrix2D evaluate(final DoubleMatrix1D x) {
    final double[] pos = x.toArray();
    final int m = pos.length;
    final double twoEPS = 2.0 * EPS;

    pos[0] += EPS;
    DoubleMatrix1D yp = _f.evaluate(new DoubleMatrix1D(pos));
    pos[0] -= twoEPS;
    DoubleMatrix1D ym = _f.evaluate(new DoubleMatrix1D(pos));
    pos[0] = x.getEntry(0);
    final int n = yp.getNumberOfElements();
    final double[][] res = new double[n][m];
    for (int i = 0; i < n; i++) {
      res[i][0] = (yp.getEntry(i) - ym.getEntry(i)) / twoEPS;
    }

    for (int j = 1; j < m; j++) {
      pos[j] += EPS;
      yp = _f.evaluate(new DoubleMatrix1D(pos));
      pos[j] -= twoEPS;
      ym = _f.evaluate(new DoubleMatrix1D(pos));
      pos[j] = x.getEntry(j);
      for (int i = 0; i < n; i++) {
        res[i][j] = (yp.getEntry(i) - ym.getEntry(i)) / twoEPS;
      }
    }

    return new DoubleMatrix2D(res);
  }

}
