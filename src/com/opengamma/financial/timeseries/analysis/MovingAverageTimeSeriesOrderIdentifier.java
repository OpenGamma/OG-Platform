/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.analysis;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public class MovingAverageTimeSeriesOrderIdentifier<T extends DoubleTimeSeries<?>> {
  private final int _maxOrder;
  private final double _criticalValue;
  private final Function1D<T, Double[]> _calculator = new AutocorrelationFunctionCalculator<T>();

  public MovingAverageTimeSeriesOrderIdentifier(final int maxOrder, final double level) {
    if (maxOrder < 1)
      throw new IllegalArgumentException("Maximum order must be greater than zero");
    if (level <= 0 || level > 1)
      throw new IllegalArgumentException("Level must be between 0 and 1");
    _maxOrder = maxOrder;
    _criticalValue = new NormalDistribution(0, 1).getInverseCDF(1 - level / 2.);
  }

  public int getOrder(final T ts) {
    if (ts == null)
      throw new IllegalArgumentException("Time series was null");
    if (ts.isEmpty())
      throw new IllegalArgumentException("Time series was empty");
    if (ts.size() < _maxOrder)
      throw new IllegalArgumentException("Number of data points lower than the maximum order to calculate");
    final Double[] acf = _calculator.evaluate(ts);
    final int n = ts.size();
    final double bound = _criticalValue / Math.sqrt(n);
    for (int i = _maxOrder; i > 0; i--) {
      if (Math.abs(acf[i]) > bound)
        return i;
    }
    throw new IllegalArgumentException("Could not find order of series; no significant autocorrelations");
  }
}
