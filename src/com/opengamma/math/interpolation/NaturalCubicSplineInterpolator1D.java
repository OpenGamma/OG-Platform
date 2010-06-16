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
public class NaturalCubicSplineInterpolator1D extends Interpolator1D<Interpolator1DWithSecondDerivativeModel, InterpolationResult> {

  @Override
  public InterpolationResult interpolate(final Interpolator1DWithSecondDerivativeModel model, final Double value) {
    Validate.notNull(value, "Value to be interpolated must not be null");
    Validate.notNull(model, "Model must not be null");
    checkValue(model, value);
    final int low = model.getLowerBoundIndex(value);
    final int high = low + 1;
    final int n = model.size() - 1;
    final double[] xData = model.getKeys();
    final double[] yData = model.getValues();
    if (model.getLowerBoundIndex(value) == n) {
      return new InterpolationResult(yData[n]);
    }
    final double delta = xData[high] - xData[low];
    if (Math.abs(delta) < getEPS()) {
      throw new InterpolationException("x data points were not distinct");
    }
    final double a = (xData[high] - value) / delta;
    final double b = (value - xData[low]) / delta;
    final double[] y2 = model.getSecondDerivatives();
    return new InterpolationResult(a * yData[low] + b * yData[high] + (a * (a * a - 1) * y2[low] + b * (b * b - 1) * y2[high]) * delta * delta / 6.);
  }
}
