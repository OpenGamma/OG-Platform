/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.covariance;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 * Given a covariance calculator and an array of {@link DoubleTimeSeries} calculates a covariance matrix
 */
public class CovarianceMatrixCalculator implements Function<DoubleTimeSeries<?>, DoubleMatrix2D> {
  private final CovarianceCalculator _calculator;

  /**   
   * @param calculator A covariance calculator
   * @throws IllegalArgumentException If the calculator is null
   */
  public CovarianceMatrixCalculator(final CovarianceCalculator calculator) {
    Validate.notNull(calculator, "covariance calculator");
    _calculator = calculator;
  }

  /**
   * 
   * Calculates a covariance matrix given an array of time series. The ordering of the elements is determined by the order of the array
   * @param x An array of {@link DoubleTimeSeries}
   * @return The covariance matrix
   * @throws IllegalArgumentException If the array of time series is null or empty
   */
  @Override
  public DoubleMatrix2D evaluate(final DoubleTimeSeries<?>... x) {
    Validate.notNull(x, "x");
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
