/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.Map;
import java.util.TreeMap;

/**
 * 
 * @author emcleod
 */

public class NaturalCubicSplineInterpolator1D extends Interpolator1D {

  @Override
  public InterpolationResult<Double> interpolate(final Map<Double, Double> data, final Double value) {
    final TreeMap<Double, Double> sorted = initData(data);
    final int low = getLowerBoundIndex(sorted, value);
    final int high = low + 1;
    final Double[] xData = sorted.keySet().toArray(new Double[0]);
    final Double[] yData = sorted.values().toArray(new Double[0]);
    final double delta = xData[high] - xData[low];
    if (Math.abs(delta) < EPS)
      throw new InterpolationException("x data points were not distinct");
    final double a = (xData[high] - value) / delta;
    final double b = (value - xData[low]) / delta;
    final double[] y2 = getSecondDerivative(xData, yData);
    final double y = a * yData[low] + b * yData[high] + (a * (a * a - 1) * y2[low] + b * (b * b - 1) * y2[high]) * delta * delta / 6.;
    return new InterpolationResult<Double>(y);
  }

  private double[] getSecondDerivative(final Double[] x, final Double[] y) {
    final int n = x.length - 1;
    final double[] y2 = new double[n];
    double p, ratio;
    final double[] u = new double[n - 1];
    y2[0] = -0.5;
    u[0] = 3. * (y[1] - y[0]) / (x[1] - x[0]);
    for (int i = 1; i < n - 1; i++) {
      ratio = (x[i] - x[i - 1]) / (x[i + 1] - x[i - 1]);
      p = ratio * y2[i - 1] + 2.;
      y2[i] = (ratio - 1) / p;
      u[i] = (y[i + 1] - y[i]) / (x[i + 1] - x[i]) - (y[i] - y[i - 1]) / (x[i] - x[i - 1]);
      u[i] = (6 * u[i] / (x[i + 1] - x[i - 1]) - ratio * u[i - 1]) / p;
    }
    final double boundary = 0.5;
    final double uLast = 3. / (x[n - 1] - x[n - 2]) * -(y[n - 1] - y[n - 2]) / (x[n - 1] - x[n - 2]);
    y2[n - 1] = (uLast - boundary * u[n - 2]) / (boundary * y2[n - 2] + 1.);
    for (int j = n - 2; j >= 0; j--) {
      y2[j] = y2[j] * y2[j + 1] + u[j];
    }
    return y2;
  }
}
