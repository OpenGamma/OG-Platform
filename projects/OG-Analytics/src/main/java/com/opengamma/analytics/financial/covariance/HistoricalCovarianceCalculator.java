/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.covariance;

import java.util.Iterator;

import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculates the historical covariance of two return series. The covariance is
 * given by:
 * $$
 * \begin{eqnarray*}
 * \frac{1}{n(n-1)}\sum\limits_{i=1}^n (x_i - \overline{x})(y_i - \overline{y})
 * \end{eqnarray*}
 * $$
 * where $x$ is the first return series, $y$ is the second return series and
 * $n$ is the number of data points.
 */
public class HistoricalCovarianceCalculator extends CovarianceCalculator {

  /**
   * Given two price time series, calculates their covariance
   * @param ts An array of price time series
   * @return The covariance of the price series
   * @throws IllegalArgumentException If the time series array is null; if the length of the time series array is not two; if the dates of the time series do not coincide.
   */
  @Override
  public Double evaluate(final DoubleTimeSeries<?>... ts) {
    ArgumentChecker.notNull(ts, "time series array");
    ArgumentChecker.isTrue(ts.length == 2, "must have two time series");
    testTimeSeries(ts[0], ts[1]);
    final DoubleTimeSeries<?> returnTS1 = ts[0];
    final DoubleTimeSeries<?> returnTS2 = ts[1];
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
