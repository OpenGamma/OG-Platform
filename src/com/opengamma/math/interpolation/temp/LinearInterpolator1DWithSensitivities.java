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
public class LinearInterpolator1DWithSensitivities extends Interpolator1DWithSensitivities<Interpolator1DDataBundle> {

  public LinearInterpolator1DWithSensitivities() {
    super(new LinearInterpolator1D());
  }

  @Override
  public InterpolationResultWithSensitivities interpolate(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(value, "value");
    Validate.notNull(data, "data bundle");
    final double interpolatedValue = getUnderlyingInterpolator().interpolate(data, value).getResult();
    final int n = data.size();
    final InterpolationBoundedValues boundedValues = data.getBoundedValues(value);
    final double[] sensitivities = new double[n];
    if (boundedValues.getHigherBoundKey() == null) {
      sensitivities[n - 1] = 1;
      return new InterpolationResultWithSensitivities(boundedValues.getLowerBoundValue(), sensitivities);
    }
    final int index = data.getLowerBoundIndex(value);
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
