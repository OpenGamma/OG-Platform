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
public class LinearInterpolator1DWithSensitivities extends Interpolator1DWithSensitivities<Interpolator1DModel> {

  public LinearInterpolator1DWithSensitivities() {
    super(new LinearInterpolator1D());
  }

  @Override
  public InterpolationResultWithSensitivities interpolate(final Interpolator1DModel model, final Double value) {
    Validate.notNull(value, "Value to be interpolated must not be null");
    Validate.notNull(model, "Model must not be null");
    final double interpolatedValue = getUnderlyingInterpolator().interpolate(model, value).getResult();
    final int n = model.size();
    final InterpolationBoundedValues boundedValues = model.getBoundedValues(value);
    if (boundedValues.getHigherBoundKey() == null) {
      return new InterpolationResultWithSensitivities(boundedValues.getLowerBoundValue(), new double[] { 1. });
    }
    final int index = model.getLowerBoundIndex(value);
    final double[] sensitivities = new double[n];
    final double x1 = boundedValues.getLowerBoundKey();
    final double x2 = boundedValues.getHigherBoundKey();
    final double dx = x2 - x1;
    final double a = (x2 - value) / dx;
    final double b = 1.0 - a;
    sensitivities[index] = a;
    sensitivities[index + 1] = b;
    return new InterpolationResultWithSensitivities(interpolatedValue, sensitivities);
  }

}
