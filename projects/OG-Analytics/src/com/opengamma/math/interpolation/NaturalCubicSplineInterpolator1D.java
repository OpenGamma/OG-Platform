/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import org.apache.commons.lang.Validate;

import com.opengamma.math.MathException;
import com.opengamma.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.math.interpolation.data.Interpolator1DCubicSplineDataBundle;

/**
 * 
 */
public class NaturalCubicSplineInterpolator1D extends Interpolator1D<Interpolator1DCubicSplineDataBundle> {

  @Override
  public Double interpolate(final Interpolator1DCubicSplineDataBundle data, final Double value) {
    Validate.notNull(value, "value");
    Validate.notNull(data, "data bundle");
    final int low = data.getLowerBoundIndex(value);
    final int high = low + 1;
    final int n = data.size() - 1;
    final double[] xData = data.getKeys();
    final double[] yData = data.getValues();
    if (data.getLowerBoundIndex(value) == n) {
      return yData[n];
    }
    final double delta = xData[high] - xData[low];
    if (Math.abs(delta) < getEPS()) {
      throw new MathException("x data points were not distinct");
    }
    final double a = (xData[high] - value) / delta;
    final double b = (value - xData[low]) / delta;
    final double[] y2 = data.getSecondDerivatives();
    return a * yData[low] + b * yData[high] + (a * (a * a - 1) * y2[low] + b * (b * b - 1) * y2[high]) * delta * delta / 6.;
  }

  @Override
  public Interpolator1DCubicSplineDataBundle getDataBundle(final double[] x, final double[] y) {
    return new Interpolator1DCubicSplineDataBundle(new ArrayInterpolator1DDataBundle(x, y));
  }

  @Override
  public Interpolator1DCubicSplineDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    return new Interpolator1DCubicSplineDataBundle(new ArrayInterpolator1DDataBundle(x, y, true));
  }
}
