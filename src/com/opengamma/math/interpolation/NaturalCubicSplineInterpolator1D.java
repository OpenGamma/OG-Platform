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
    if (low == sorted.size() - 1) {
      return new InterpolationResult<Double>(sorted.lastEntry().getValue());
    }
    final int high = low + 1;
    final Double[] xData = sorted.keySet().toArray(new Double[0]);
    final Double[] yData = sorted.values().toArray(new Double[0]);
    final double delta = xData[high] - xData[low];
    if (Math.abs(delta) < EPS) {
      throw new InterpolationException("x data points were not distinct");
    }
    final double a = (xData[high] - value) / delta;
    final double b = (value - xData[low]) / delta;
    final double[] y2 = getSecondDerivative(xData, yData);
    final double y = a * yData[low] + b * yData[high] + (a * (a * a - 1) * y2[low] + b * (b * b - 1) * y2[high])
        * delta * delta / 6.;
    return new InterpolationResult<Double>(y);
  }

  private double[] getSecondDerivative(final Double[] x, final Double[] y) {
    final int n = x.length;
    final double[] y2 = new double[n];
    double p, ratio;
    final double[] u = new double[n - 1];
    y2[0] = u[0] = 0.0;
    for (int i = 1; i < n - 1; i++) {
      ratio = (x[i] - x[i - 1]) / (x[i + 1] - x[i - 1]);
      p = ratio * y2[i - 1] + 2.0;
      y2[i] = (ratio - 1.0) / p;
      u[i] = (y[i + 1] - y[i]) / (x[i + 1] - x[i]) - (y[i] - y[i - 1]) / (x[i] - x[i - 1]);
      u[i] = (6.0 * u[i] / (x[i + 1] - x[i - 1]) - ratio * u[i - 1]) / p;
    }
    y2[n - 1] = 0.0;
    for (int k = n - 2; k >= 0; k--) {
      y2[k] = y2[k] * y2[k + 1] + u[k];
    }

    return y2;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null) {
      return false;
    }
    if (o == this) {
      return true;
    }
    if (!(o instanceof NaturalCubicSplineInterpolator1D)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

}
