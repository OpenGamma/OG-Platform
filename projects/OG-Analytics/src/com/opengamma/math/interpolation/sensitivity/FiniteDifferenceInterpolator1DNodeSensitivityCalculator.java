/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.sensitivity;

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
    final double[] result = new double[n];
    final T dataUp = _interpolator.getDataBundleFromSortedArrays(x, y);
    final T dataDown = _interpolator.getDataBundleFromSortedArrays(x, y);
    for (int i = 0; i < n; i++) {
      if (i != 0) {
        dataUp.setYValueAtIndex(i - 1, y[i - 1]);
        dataDown.setYValueAtIndex(i - 1, y[i - 1]);
      }
      dataUp.setYValueAtIndex(i, y[i] + EPS);
      dataDown.setYValueAtIndex(i, y[i] - EPS);
      final double up = _interpolator.interpolate(dataUp, value);
      final double down = _interpolator.interpolate(dataDown, value);
      result[i] = (up - down) / TWO_EPS;
    }
    return result;
  }

}
