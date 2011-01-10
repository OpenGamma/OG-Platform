/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import org.apache.commons.lang.Validate;

import com.opengamma.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 * 
 */
public class ExponentialInterpolator1D extends Interpolator1D<Interpolator1DDataBundle> {

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
  public Interpolator1DDataBundle getDataBundle(final double[] x, final double[] y) {
    return new ArrayInterpolator1DDataBundle(x, y);
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    return new ArrayInterpolator1DDataBundle(x, y, true);
  }

}
