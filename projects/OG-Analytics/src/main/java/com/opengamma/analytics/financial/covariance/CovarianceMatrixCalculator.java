/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.covariance;

import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 *
 * Given a covariance calculator and an array of {@link DoubleTimeSeries} calculates a covariance matrix.
 */
public class CovarianceMatrixCalculator implements Function<DoubleTimeSeries<?>, DoubleMatrix2D> {
  /** The covariance calculator */
  private final CovarianceCalculator _calculator;

  /**
   * @param calculator A covariance calculator, not null
   */
  public CovarianceMatrixCalculator(final CovarianceCalculator calculator) {
    ArgumentChecker.notNull(calculator, "covariance calculator");
    _calculator = calculator;
  }

  /**
   *
   * Calculates a covariance matrix given an array of time series. The ordering of the elements is determined by the order of the array
   * @param x An array of {@link DoubleTimeSeries}, not null or empty
   * @return The covariance matrix
   */
  @Override
  public DoubleMatrix2D evaluate(final DoubleTimeSeries<?>... x) {
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
