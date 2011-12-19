/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.analysis;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class DoubleTimeSeriesStatisticsCalculator implements Function<DoubleTimeSeries<?>, Double> {
  private final Function<double[], Double> _statistic;

  public DoubleTimeSeriesStatisticsCalculator(final Function<double[], Double> statistic) {
    Validate.notNull(statistic, "statistic");
    _statistic = statistic;
  }

  @Override
  public Double evaluate(final DoubleTimeSeries<?>... x) {
    Validate.notNull(x, "x");
    Validate.isTrue(x.length > 0);
    ArgumentChecker.noNulls(x, "x");
    final int n = x.length;
    final double[][] arrays = new double[n][];
    for (int i = 0; i < n; i++) {
      arrays[i] = x[i].valuesArrayFast();
    }
    return _statistic.evaluate(arrays);
  }
}
