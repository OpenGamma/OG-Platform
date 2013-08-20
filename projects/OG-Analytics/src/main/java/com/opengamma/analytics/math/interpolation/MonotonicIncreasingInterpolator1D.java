/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DMonotonicIncreasingDataBundle;

/**
 * 
 */
public class MonotonicIncreasingInterpolator1D extends Interpolator1D {
  private static final long serialVersionUID = 1L;

  @Override
  public Double interpolate(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(value, "value");
    Validate.notNull(data, "data bundle");
    Validate.isTrue(data instanceof Interpolator1DMonotonicIncreasingDataBundle);
    final Interpolator1DMonotonicIncreasingDataBundle miData = (Interpolator1DMonotonicIncreasingDataBundle) data;

    final int n = data.size() - 1;
    final double[] xData = data.getKeys();
    final double[] yData = data.getValues();

    double h, dx, a, b;
    if (value < data.firstKey()) {
      h = 0;
      dx = value;
      a = miData.getA(0);
      b = miData.getB(0);
    } else if (value > data.lastKey()) {
      h = yData[n];
      dx = value - xData[n];
      a = miData.getA(n + 1);
      b = miData.getB(n + 1);
    } else {
      final int low = data.getLowerBoundIndex(value);
      h = yData[low];
      dx = value - xData[low];
      a = miData.getA(low + 1);
      b = miData.getB(low + 1);
    }
    if (Math.abs(b * dx) < 1e-8) {
      return h + a * (dx + b * dx * dx / 2);
    }
    return h + a / b * (Math.exp(b * dx) - 1);

  }

  @Override
  public double firstDerivative(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(value, "value");
    Validate.notNull(data, "data bundle");
    Validate.isTrue(data instanceof Interpolator1DMonotonicIncreasingDataBundle);
    final Interpolator1DMonotonicIncreasingDataBundle miData = (Interpolator1DMonotonicIncreasingDataBundle) data;

    final int n = data.size() - 1;
    final double[] xData = data.getKeys();
    final double[] yData = data.getValues();

    double h, dx, a, b;
    if (value < data.firstKey()) {
      h = 0;
      dx = value;
      a = miData.getA(0);
      b = miData.getB(0);
    } else if (value > data.lastKey()) {
      h = yData[n];
      dx = value - xData[n];
      a = miData.getA(n + 1);
      b = miData.getB(n + 1);
    } else {
      final int low = data.getLowerBoundIndex(value);
      h = yData[low];
      dx = value - xData[low];
      a = miData.getA(low + 1);
      b = miData.getB(low + 1);
    }
    if (Math.abs(b * dx) < 1e-8) {
      return h + a * (dx + b * dx * dx / 2);
    }

    return a * Math.exp(b * dx);

  }

  @Override
  public double[] getNodeSensitivitiesForValue(final Interpolator1DDataBundle data, final Double value) {
    throw new NotImplementedException();
  }

  @Override
  public Interpolator1DDataBundle getDataBundle(final double[] x, final double[] y) {
    return new Interpolator1DMonotonicIncreasingDataBundle(new ArrayInterpolator1DDataBundle(x, y));
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    return new Interpolator1DMonotonicIncreasingDataBundle(new ArrayInterpolator1DDataBundle(x, y, true));
  }

}
