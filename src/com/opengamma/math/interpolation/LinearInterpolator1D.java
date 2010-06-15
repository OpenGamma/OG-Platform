/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import org.apache.commons.lang.Validate;

/**
 * A one-dimensional linear interpolator. The interpolated value of the function
 * <i>y</i> at <i>x</i> between two data points <i>(x<sub>1</sub>,
 * y<sub>1</sub>)</i> and <i>(x<sub>2</sub>, y<sub>2</sub>)</i> is given by:<br>
 * <i>y = y<sub>1</sub> + (x - x<sub>1</sub>) * (y<sub>2</sub> - y<sub>1</sub>)
 * / (x<sub>2</sub> - x<sub>1</sub>)</i>
 */
public class LinearInterpolator1D extends Interpolator1D<Interpolator1DModel> {

  @Override
  public Double interpolate(final Interpolator1DModel model, final Double value) {
    Validate.notNull(value, "Value to be interpolated must not be null");
    Validate.notNull(model, "Model must not be null");
    checkValue(model, value);
    final InterpolationBoundedValues boundedValues = model.getBoundedValues(value);
    final double x1 = boundedValues.getLowerBoundKey();
    final double y1 = boundedValues.getLowerBoundValue();
    if (model.getLowerBoundIndex(value) == model.size() - 1) {
      return y1;
    }
    final double x2 = boundedValues.getHigherBoundKey();
    final double y2 = boundedValues.getHigherBoundValue();
    final double result = y1 + (value - x1) / (x2 - x1) * (y2 - y1);
    return result;
  }

}
