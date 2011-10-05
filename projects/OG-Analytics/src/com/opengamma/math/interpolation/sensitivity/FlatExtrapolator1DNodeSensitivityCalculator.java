/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.sensitivity;

import org.apache.commons.lang.Validate;

import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 */
public class FlatExtrapolator1DNodeSensitivityCalculator implements Interpolator1DNodeSensitivityCalculator {

  @Override
  public double[] calculate(final Interpolator1DDataBundle data, final double value) {
    Validate.notNull(data, "data");
    final int n = data.size();
    if (value < data.firstKey()) {
      final double[] result = new double[n];
      result[0] = 1;
      return result;
    } else if (value > data.lastKey()) {
      final double[] result = new double[n];
      result[n - 1] = 1;
      return result;
    }
    throw new IllegalArgumentException("Value " + value + " was within data range");
  }

}
