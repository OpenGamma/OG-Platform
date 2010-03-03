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
public class DoubleTimeSeriesStatisticsCalculator<T extends DoubleTimeSeries<?>> extends Function1D<T, Double> {
  private final Function1D<Double[], Double> _statistic;

  public DoubleTimeSeriesStatisticsCalculator(final Function1D<Double[], Double> statistic) {
    _statistic = statistic;
  }

  @Override
  public Double evaluate(final T x) {
    if (x == null)
      throw new IllegalArgumentException("Time series was null");
    if (x.isEmpty())
      throw new IllegalArgumentException("Time series was empty");
    return _statistic.evaluate(x.valuesArray());
  }

}
