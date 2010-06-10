/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class NaturalCubicSplineInterpolator1D extends Interpolator1D<Interpolator1DWithSecondDerivativeModel> {

  @Override
  public InterpolationResult<Double> interpolate(final Interpolator1DWithSecondDerivativeModel model, final Double value) {
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
    final double[] y2 = model.getSecondDerivatives();
    final double y = a * yData[low] + b * yData[high] + (a * (a * a - 1) * y2[low] + b * (b * b - 1) * y2[high]) * delta * delta / 6.;
    return new InterpolationResult<Double>(y);
  }
}
