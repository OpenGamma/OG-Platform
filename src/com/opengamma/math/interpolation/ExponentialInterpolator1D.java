/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import org.apache.commons.lang.Validate;

/**
 * 
 * 
 */
public class ExponentialInterpolator1D extends Interpolator1D<Interpolator1DModel, InterpolationResult> {

  @Override
  public InterpolationResult interpolate(final Interpolator1DModel model, final Double value) {
    Validate.notNull(value, "Value to be interpolated must not be null");
    Validate.notNull(model, "Model must not be null");
    checkValue(model, value);
    final Double x1 = model.getLowerBoundKey(value);
    final Double y1 = model.get(x1);
    if (model.getLowerBoundIndex(value) == model.size() - 1) {
      return new InterpolationResult(y1);
    }
    final Double x2 = model.higherKey(x1);
    final Double y2 = model.get(x2);
    final double xDiff = x2 - x1;
    return new InterpolationResult(Math.pow(y1, value * (x2 - value) / xDiff / x1) * Math.pow(y2, value * (value - x1) / xDiff / x2));
  }

}
