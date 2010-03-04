/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.analysis;

import com.opengamma.math.function.Function1D;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 * @author emcleod
 */
public class DoubleTimeSeriesStatisticsCalculator extends Function1D<DoubleTimeSeries<?>, Double> {
  private final Function1D<Double[], Double> _statistic;

  public DoubleTimeSeriesStatisticsCalculator(final Function1D<Double[], Double> statistic) {
    _statistic = statistic;
  }

  @Override
  public Double evaluate(final DoubleTimeSeries<?> x) {
    if (x == null)
      throw new IllegalArgumentException("Time series was null");
    if (x.isEmpty())
      throw new IllegalArgumentException("Time series was empty");
    return _statistic.evaluate(x.valuesArray());
  }

}
