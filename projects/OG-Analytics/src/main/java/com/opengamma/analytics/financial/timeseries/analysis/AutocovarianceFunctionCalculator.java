/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.timeseries.analysis;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.descriptive.MeanCalculator;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class AutocovarianceFunctionCalculator extends Function1D<DoubleTimeSeries<?>, double[]> {
  private final Function<DoubleTimeSeries<?>, Double> _meanCalculator = new DoubleTimeSeriesStatisticsCalculator(new MeanCalculator());

  @Override
  public double[] evaluate(final DoubleTimeSeries<?> x) {
    Validate.notNull(x, "x");
    if (x.isEmpty()) {
      throw new IllegalArgumentException("Time series was empty");
    }
    final int h = x.size() - 1;
    final double[] result = new double[h];
    final double mean = _meanCalculator.evaluate(x);
    double[] x1, x2;
    final int n = x.size();
    double sum;
    final double[] x0 = x.valuesArrayFast();
    for (int i = 0; i < h; i++) {
      x1 = Arrays.copyOfRange(x0, 0, n - i);
      x2 = Arrays.copyOfRange(x0, i, n);
      if (x1.length != x2.length) {
        throw new IllegalArgumentException("Series were not the same length; this should not happen");
      }
      sum = 0;
      for (int j = 0; j < x1.length; j++) {
        sum += (x1[j] - mean) * (x2[j] - mean);
      }
      result[i] = sum / n;
    }
    return result;
  }
}
