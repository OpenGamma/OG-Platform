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

  /* (non-Javadoc)
   * @see com.opengamma.math.interpolation.Interpolator1DWithSensitivities#interpolate(com.opengamma.math.interpolation.Interpolator1DModel, java.lang.Double)
   */
  @Override
  public InterpolationResultWithSensitivities interpolate(Interpolator1DModel model, Double value) {

    Validate.notNull(value, "Value to be interpolated must not be null");
    Validate.notNull(model, "Model must not be null");

    int nNodes = model.size();
    double[] sense = new double[nNodes];

    final InterpolationBoundedValues boundedValues = model.getBoundedValues(value);
    if (boundedValues.getLowerBoundKey() == null) {
      throw new InterpolationException("value out of range - too small");
    }

    int index = model.getLowerBoundIndex(value);

    //in this case we are at the last node 
    if (boundedValues.getHigherBoundKey() == null) {
      if (value > boundedValues.getLowerBoundKey()) {
        throw new InterpolationException("value out of range - too large");
      }
      sense[index] = 1.0;
      return new InterpolationResultWithSensitivities(boundedValues.getLowerBoundValue(), sense);
    }

    final double x1 = boundedValues.getLowerBoundKey();
    final double x2 = boundedValues.getHigherBoundKey();
    final double y1 = boundedValues.getLowerBoundValue();
    final double y2 = boundedValues.getHigherBoundValue();
    final double dx = x2 - x1;
    final double a = (x2 - value) / dx;
    final double b = 1.0 - a;
    final double result = a * y1 + b * y2;
    sense[index] = a;
    sense[index + 1] = b;

    return new InterpolationResultWithSensitivities(result, sense);
  }

}
