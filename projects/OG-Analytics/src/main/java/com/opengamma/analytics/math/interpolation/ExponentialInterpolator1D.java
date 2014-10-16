/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;

/**
 * Exponential interpolator description.
 * 
 */
public class ExponentialInterpolator1D extends Interpolator1D {
  private static final long serialVersionUID = 1L;

  @Override
  public Double interpolate(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(value, "value");
    Validate.notNull(data, "data bundle");
    final Double x1 = data.getLowerBoundKey(value);
    final Double y1 = data.get(x1);
    if (data.getLowerBoundIndex(value) == data.size() - 1) {
      return y1;
    }
    final Double x2 = data.higherKey(x1);
    final Double y2 = data.get(x2);
    final double xDiff = x2 - x1;
    return Math.pow(y1, value * (x2 - value) / xDiff / x1) * Math.pow(y2, value * (value - x1) / xDiff / x2);
  }

  @Override
  public double firstDerivative(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(value, "value");
    Validate.notNull(data, "data bundle");
    final Double x1 = data.getLowerBoundKey(value);
    final Double y1 = data.get(x1);
    if (data.getLowerBoundIndex(value) == data.size() - 1) {
      return 0.;
    }
    final Double x2 = data.higherKey(x1);
    final Double y2 = data.get(x2);
    final double xDiff = x2 - x1;
    return Math.pow(y1, value * (x2 - value) / xDiff / x1) * Math.pow(y2, value * (value - x1) / xDiff / x2) * (Math.log(y1) * (x2 - 2. * value) / x1 + Math.log(y2) * (2. * value - x1) / x2) / xDiff;
  }

  @Override
  public Interpolator1DDataBundle getDataBundle(final double[] x, final double[] y) {
    return new ArrayInterpolator1DDataBundle(x, y);
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    return new ArrayInterpolator1DDataBundle(x, y, true);
  }

  @Override
  public double[] getNodeSensitivitiesForValue(Interpolator1DDataBundle data, Double value) {
    return getFiniteDifferenceSensitivities(data, value);
  }

}
