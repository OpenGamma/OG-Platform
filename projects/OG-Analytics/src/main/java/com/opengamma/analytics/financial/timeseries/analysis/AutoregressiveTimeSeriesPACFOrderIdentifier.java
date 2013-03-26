/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.analysis;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.regression.LeastSquaresRegression;
import com.opengamma.analytics.math.regression.LeastSquaresRegressionResult;
import com.opengamma.analytics.math.regression.OrdinaryLeastSquaresRegression;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class AutoregressiveTimeSeriesPACFOrderIdentifier {
  private final int _maxOrder;
  private final double _level;
  private final LeastSquaresRegression _regression = new OrdinaryLeastSquaresRegression();

  public AutoregressiveTimeSeriesPACFOrderIdentifier(final int maxOrder, final double level) {
    if (maxOrder < 1) {
      throw new IllegalArgumentException("Maximum order must be greater than zero");
    }
    if (level <= 0 || level > 1) {
      throw new IllegalArgumentException("Level must be between 0 and 1");
    }
    _maxOrder = maxOrder;
    _level = level;
  }

  public int getOrder(final DoubleTimeSeries<?> ts) {
    Validate.notNull(ts);
    if (ts.isEmpty()) {
      throw new IllegalArgumentException("Time series was empty");
    }
    if (ts.size() <= _maxOrder) {
      throw new IllegalArgumentException("Need at least " + (_maxOrder + 1) + " points in the time series");
    }
    final int n = ts.size();
    Integer order = null;
    final double[] data = ts.valuesArrayFast();
    for (int i = 1; i < _maxOrder; i++) {
      final double[] y = new double[n - i];
      final double[][] x = new double[n - i][i];
      for (int j = n - 1; j >= i; j--) {
        y[j - i] = data[j];
        for (int k = 1; k <= i; k++) {
          x[j - i][k - 1] = data[j - k];
        }
      }
      final LeastSquaresRegressionResult result = _regression.regress(x, null, y, true);
      if (result.getPValues()[i] < _level) {
        order = i;
      }
    }
    if (order == null) {
      throw new IllegalArgumentException("Could not find order of series using PACF; no significant coefficients");
    }
    return order;
  }
}
