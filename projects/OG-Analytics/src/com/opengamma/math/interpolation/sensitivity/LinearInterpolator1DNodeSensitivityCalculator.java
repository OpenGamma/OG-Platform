/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.sensitivity;

import org.apache.commons.lang.Validate;

import com.opengamma.math.interpolation.data.InterpolationBoundedValues;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 */
public class LinearInterpolator1DNodeSensitivityCalculator implements Interpolator1DNodeSensitivityCalculator<Interpolator1DDataBundle> {

  @Override
  public double[] calculate(final Interpolator1DDataBundle data, final double value) {
    Validate.notNull(data, "data");
    final int n = data.size();
    final double[] result = new double[n];
    final InterpolationBoundedValues boundedValues = data.getBoundedValues(value);
    if (boundedValues.getHigherBoundKey() == null) {
      result[n - 1] = 1.0;
      return result;
    }
    final int index = data.getLowerBoundIndex(value);
    final double x1 = boundedValues.getLowerBoundKey();
    final double x2 = boundedValues.getHigherBoundKey();
    final double dx = x2 - x1;
    final double a = (x2 - value) / dx;
    final double b = 1 - a;
    result[index] = a;
    result[index + 1] = b;
    return result;
  }

}
