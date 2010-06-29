/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class FiniteDifferenceJacobianCalculator implements JacobianCalculator {
  private static final Logger s_logger = LoggerFactory.getLogger(FiniteDifferenceJacobianCalculator.class);
  private final double _eps;
  private final double _twoEPS;

  public FiniteDifferenceJacobianCalculator() {
    this(1e-8);
  }

  public FiniteDifferenceJacobianCalculator(final double eps) {
    _eps = eps;
    _twoEPS = 2 * eps;
  }

  @Override
  public DoubleMatrix2D evaluate(final DoubleMatrix1D x, final Function1D<DoubleMatrix1D, DoubleMatrix1D>... functions) {
    Validate.notNull(x);
    Validate.notNull(functions);
    Validate.notEmpty(functions);
    if (functions.length > 1) {
      s_logger.info("Have more than one function in array: only using the first one");
    }
    return evaluate(x, functions[0]);
  }

  private DoubleMatrix2D evaluate(final DoubleMatrix1D x, final Function1D<DoubleMatrix1D, DoubleMatrix1D> function) {
    final double[] xArray = x.toArray();
    final int m = xArray.length;
    xArray[0] += _eps;
    DoubleMatrix1D yUp = function.evaluate(new DoubleMatrix1D(xArray));
    xArray[0] -= _twoEPS;
    DoubleMatrix1D yDown = function.evaluate(new DoubleMatrix1D(xArray));
    xArray[0] = x.getEntry(0);
    final int n = yUp.getNumberOfElements();
    final double[][] res = new double[n][m];
    for (int i = 0; i < n; i++) {
      res[i][0] = (yUp.getEntry(i) - yDown.getEntry(i)) / _twoEPS;
    }
    for (int j = 1; j < m; j++) {
      xArray[j] += _eps;
      yUp = function.evaluate(new DoubleMatrix1D(xArray));
      xArray[j] -= _twoEPS;
      yDown = function.evaluate(new DoubleMatrix1D(xArray));
      xArray[j] = x.getEntry(j);
      for (int i = 0; i < n; i++) {
        res[i][j] = (yUp.getEntry(i) - yDown.getEntry(i)) / _twoEPS;
      }
    }
    return new DoubleMatrix2D(res);
  }
}
