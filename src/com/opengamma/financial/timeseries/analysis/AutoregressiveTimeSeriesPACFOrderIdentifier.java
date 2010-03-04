/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.analysis;

import com.opengamma.math.regression.LeastSquaresRegression;
import com.opengamma.math.regression.LeastSquaresRegressionResult;
import com.opengamma.math.regression.OrdinaryLeastSquaresRegression;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public class AutoregressiveTimeSeriesPACFOrderIdentifier {
  private final int _maxOrder;
  private final double _level;
  private final LeastSquaresRegression _regression = new OrdinaryLeastSquaresRegression();

  public AutoregressiveTimeSeriesPACFOrderIdentifier(final int maxOrder, final double level) {
    if (maxOrder < 1)
      throw new IllegalArgumentException("Maximum order must be greater than zero");
    if (level <= 0 || level > 1)
      throw new IllegalArgumentException("Level must be between 0 and 1");
    _maxOrder = maxOrder;
    _level = level;
  }

  public int getOrder(final DoubleTimeSeries<?> ts) {
    if (ts == null)
      throw new IllegalArgumentException("Time series was null");
    if (ts.isEmpty())
      throw new IllegalArgumentException("Time series was empty");
    if (ts.size() <= _maxOrder)
      throw new IllegalArgumentException("Need at least " + (_maxOrder + 1) + " points in the time series");
    final int n = ts.size();
    Integer order = null;
    final Double[] data = ts.valuesArray();
    for (int i = 1; i < _maxOrder; i++) {
      final Double[] y = new Double[n - i];
      final Double[][] x = new Double[n - i][i];
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
    if (order == null)
      throw new IllegalArgumentException("Could not find order of series using PACF; no significant coefficients");
    return order;
  }
}
