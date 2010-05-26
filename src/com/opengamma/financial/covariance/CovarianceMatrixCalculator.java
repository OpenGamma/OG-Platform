/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.covariance;

import com.opengamma.math.function.Function;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class CovarianceMatrixCalculator implements Function<DoubleTimeSeries<?>, DoubleMatrix2D> {
  private final CovarianceCalculator _calculator;

  public CovarianceMatrixCalculator(final CovarianceCalculator calculator) {
    ArgumentChecker.notNull(calculator, "covariance calculator");
    _calculator = calculator;
  }

  @Override
  public DoubleMatrix2D evaluate(final DoubleTimeSeries<?>... x) {
    ArgumentChecker.notNull(x, "x");
    ArgumentChecker.notEmpty(x, "x");
    final int n = x.length;
    final double[][] covariance = new double[n][n];
    DoubleTimeSeries<?> ts;
    for (int i = 0; i < n; i++) {
      ts = x[i];
      covariance[i][i] = _calculator.evaluate(ts, ts);
      for (int j = 0; j < i; j++) {
        covariance[i][j] = _calculator.evaluate(ts, x[j]);
        covariance[j][i] = covariance[i][j];
      }
    }
    return new DoubleMatrix2D(covariance);
  }
}
