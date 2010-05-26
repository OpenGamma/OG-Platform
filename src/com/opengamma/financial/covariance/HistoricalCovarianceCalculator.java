/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.covariance;

import java.util.Iterator;

import com.opengamma.financial.timeseries.returns.TimeSeriesReturnCalculator;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class HistoricalCovarianceCalculator extends CovarianceCalculator {
  private final TimeSeriesReturnCalculator _returnCalculator;

  public HistoricalCovarianceCalculator(final TimeSeriesReturnCalculator returnCalculator) {
    ArgumentChecker.notNull(returnCalculator, "return calculator");
    _returnCalculator = returnCalculator;
  }

  @Override
  public Double evaluate(final DoubleTimeSeries<?> ts1, final DoubleTimeSeries<?> ts2) {
    testTimeSeries(ts1, ts2);
    final DoubleTimeSeries<?> returnTS1 = _returnCalculator.evaluate(ts1);
    final DoubleTimeSeries<?> returnTS2 = _returnCalculator.evaluate(ts2);
    final int n = returnTS1.size();
    double xyMean = 0;
    double xMean = 0;
    double yMean = 0;
    final Iterator<Double> iter1 = returnTS1.valuesIterator();
    final Iterator<Double> iter2 = returnTS2.valuesIterator();
    double x, y;
    while (iter1.hasNext()) {
      x = iter1.next();
      y = iter2.next();
      xyMean += x * y;
      xMean += x;
      yMean += y;
    }
    xyMean /= n - 1;
    xMean /= n;
    yMean /= n;
    return xyMean - xMean * yMean;
  }
}
