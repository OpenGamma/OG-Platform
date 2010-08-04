/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.sensitivity;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.math.interpolation.InterpolationResult;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.Interpolator1DDataBundleFactory;

/**
 * 
 * @param <T> 
 */
public class FiniteDifferenceInterpolator1DNodeSensitivityCalculator<T extends Interpolator1DDataBundle> implements Interpolator1DNodeSensitivityCalculator<T> {
  private static final double EPS = 1e-6;

  @Override
  public double[] calculate(final Interpolator1D<T, InterpolationResult> interpolator, final T data, final Double value) {
    Validate.notNull(interpolator, "interpolator");
    Validate.notNull(data, "data");
    Validate.notNull(value, "value");
    final double[] x = data.getKeys();
    final double[] y = data.getValues();
    final int n = x.length;
    double[] yUp, yDown;
    final double[] result = new double[n];
    for (int i = 0; i < n; i++) {
      yUp = Arrays.copyOf(y, n);
      yDown = Arrays.copyOf(y, n);
      yUp[i] += EPS;
      yDown[i] -= EPS;
      final T dataUp = Interpolator1DDataBundleFactory.fromSortedArrays(x, yUp, interpolator);
      final T dataDown = Interpolator1DDataBundleFactory.fromSortedArrays(x, yDown, interpolator);
    }
    return null;
  }

}
