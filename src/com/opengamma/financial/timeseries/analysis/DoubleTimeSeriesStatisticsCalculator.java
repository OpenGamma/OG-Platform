/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.analysis;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class DoubleTimeSeriesStatisticsCalculator extends Function1D<DoubleTimeSeries<?>, Double> {
  private final Function1D<double[], Double> _statistic;

  public DoubleTimeSeriesStatisticsCalculator(final Function1D<double[], Double> statistic) {
    Validate.notNull(statistic, "statistic");
    _statistic = statistic;
  }

  @Override
  public Double evaluate(final DoubleTimeSeries<?> x) {
    Validate.notNull(x, "x");
    if (x.isEmpty()) {
      throw new IllegalArgumentException("Time series was empty");
    }
    return _statistic.evaluate(x.toFastLongDoubleTimeSeries().valuesArrayFast());
  }

}
