/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.analysis;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class MovingAverageTimeSeriesOrderIdentifier {
  private final int _maxOrder;
  private final double _criticalValue;
  private final Function1D<DoubleTimeSeries<?>, double[]> _calculator = new AutocorrelationFunctionCalculator();

  public MovingAverageTimeSeriesOrderIdentifier(final int maxOrder, final double level) {
    if (maxOrder < 1) {
      throw new IllegalArgumentException("Maximum order must be greater than zero");
    }
    if (!ArgumentChecker.isInRangeExcludingLow(0, 1, level)) {
      throw new IllegalArgumentException("Level must be between 0 and 1");
    }
    _maxOrder = maxOrder;
    _criticalValue = new NormalDistribution(0, 1).getInverseCDF(1 - level / 2.);
  }

  public int getOrder(final DoubleTimeSeries<?> ts) {
    Validate.notNull(ts);
    if (ts.isEmpty()) {
      throw new IllegalArgumentException("Time series was empty");
    }
    if (ts.size() < _maxOrder) {
      throw new IllegalArgumentException("Number of data points lower than the maximum order to calculate");
    }
    final double[] acf = _calculator.evaluate(ts);
    final int n = ts.size();
    final double bound = _criticalValue / Math.sqrt(n);
    for (int i = _maxOrder; i > 0; i--) {
      if (Math.abs(acf[i]) > bound) {
        return i;
      }
    }
    throw new IllegalArgumentException("Could not find order of series; no significant autocorrelations");
  }
}
