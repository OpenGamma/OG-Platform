/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.sensitivity;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 * @param <T> 
 */
public class FiniteDifferenceInterpolator1DNodeSensitivityCalculator<T extends Interpolator1DDataBundle> implements Interpolator1DNodeSensitivityCalculator<T> {
  private static final double EPS = 1e-6;
  private static final double TWO_EPS = 2 * EPS;
  private final Interpolator1D<T> _interpolator;

  public FiniteDifferenceInterpolator1DNodeSensitivityCalculator(final Interpolator1D<T> interpolator) {
    Validate.notNull(interpolator);
    _interpolator = interpolator;
  }

  @Override
  public double[] calculate(final T data, final double value) {
    Validate.notNull(data, "data");
    final double[] x = data.getKeys();
    final double[] y = data.getValues();
    final int n = x.length;
    final double[] yUp = Arrays.copyOf(y, n);
    final double[] yDown = Arrays.copyOf(y, n);
    final double[] result = new double[n];
    for (int i = 0; i < n; i++) {
      if (i != 0) {
        yUp[i - 1] = y[i - 1];
        yDown[i - 1] = y[i - 1];
      }
      yUp[i] += EPS;
      yDown[i] -= EPS;
      final T dataUp = _interpolator.getDataBundleFromSortedArrays(x, yUp);
      final T dataDown = _interpolator.getDataBundleFromSortedArrays(x, yDown);
      final double up = _interpolator.interpolate(dataUp, value);
      final double down = _interpolator.interpolate(dataDown, value);
      result[i] = (up - down) / TWO_EPS;
    }
    return result;
  }

}
