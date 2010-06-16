/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class StepInterpolator1D extends Interpolator1D<Interpolator1DModel, InterpolationResult> {

  @Override
  public InterpolationResult interpolate(final Interpolator1DModel model, final Double value) {
    Validate.notNull(value, "Value to be interpolated must not be null");
    Validate.notNull(model, "Model must not be null");
    checkValue(model, value);
    return new InterpolationResult(model.get(model.getLowerBoundKey(value)));
  }

}
