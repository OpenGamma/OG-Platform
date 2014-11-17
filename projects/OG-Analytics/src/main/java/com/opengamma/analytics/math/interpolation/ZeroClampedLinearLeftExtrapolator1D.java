/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ZeroClampedLinearLeftExtrapolator1D extends Interpolator1D {
  private static final long serialVersionUID = 1L;

  @Override
  public Interpolator1DDataBundle getDataBundle(final double[] x, final double[] y) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Double interpolate(final Interpolator1DDataBundle data, final Double value) {
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.notNull(value, "value");
    ArgumentChecker.isTrue(value >= 0, "Cannot extrapolate to negative values, value provide: {}", value);
    double x = data.firstKey();
    ArgumentChecker.isTrue(value < x, "This is only vaild as a left extrapolator. Value must be between 0 and {}", x);
    double y = data.firstValue();
    return value * y / x;
  }

  @Override
  public double firstDerivative(final Interpolator1DDataBundle data, final Double value) {
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.notNull(value, "value");
    ArgumentChecker.isTrue(value >= 0, "Cannot extrapolate to negative values, value provide: {}", value);
    double x = data.firstKey();
    ArgumentChecker.isTrue(value < x, "This is only vaild as a left extrapolator. Value must be between 0 and {}", x);
    return data.firstValue() / x;
  }

  @Override
  public double[] getNodeSensitivitiesForValue(final Interpolator1DDataBundle data, final Double value) {
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.notNull(value, "value");
    ArgumentChecker.isTrue(value >= 0, "Cannot extrapolate to negative values, value provide: {}", value);
    double x = data.firstKey();
    ArgumentChecker.isTrue(value < x, "This is only vaild as a left extrapolator. Value must be between 0 and {}", x);
    int n = data.size();
    final double[] result = new double[n];
    result[0] = value / x;
    return result;
  }
}
