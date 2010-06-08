/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import org.apache.commons.lang.Validate;

/**
 * 
 * @author emcleod
 */

public class NaturalCubicSplineInterpolator1D extends Interpolator1D {

  @Override
  public InterpolationResult<Double> interpolate(final Interpolator1DModel model, final Double value) {
    Validate.notNull(value, "Value to be interpolated must not be null");
    Validate.notNull(model, "Model must not be null");
    final int low = model.getLowerBoundIndex(value);
    if (low == model.size() - 1) {
      return new InterpolationResult<Double>(model.lastValue());
    }
    if (low < 0) {
      throw new InterpolationException("");
    }
    final int high = low + 1;
    final double[] xData = model.getKeys();
    final double[] yData = model.getValues();
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

  private double[] getSecondDerivative(final double[] x, final double[] y) {
    final int n = x.length;
    final double[] y2 = new double[n];
    double p, ratio;
    final double[] u = new double[n - 1];
    y2[0] = 0.0;
    u[0] = 0.0;
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
