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
import com.opengamma.analytics.math.interpolation.data.Interpolator1DQuadraticSplineDataBundle;

/**
 * 
 */
public class QuadraticSplineInterpolator1D extends Interpolator1D {
  private static final long serialVersionUID = 1L;

  @Override
  public Double interpolate(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(value, "value");
    Validate.notNull(data, "data bundle");
    Validate.isTrue(data instanceof Interpolator1DQuadraticSplineDataBundle);
    final Interpolator1DQuadraticSplineDataBundle quadraticData = (Interpolator1DQuadraticSplineDataBundle) data;

    final int n = data.size() - 1;
    final double[] xData = data.getKeys();
    final double[] yData = data.getValues();

    double h, dx, a, b;
    if (value < data.firstKey()) {
      h = 0;
      dx = value;
      a = quadraticData.getA(0);
      b = quadraticData.getB(0);
    } else if (value > data.lastKey()) {
      h = yData[n];
      dx = value - xData[n];
      a = quadraticData.getA(n + 1);
      b = quadraticData.getB(n + 1);
    } else {
      final int low = data.getLowerBoundIndex(value);
      h = yData[low];
      dx = value - xData[low];
      a = quadraticData.getA(low + 1);
      b = quadraticData.getB(low + 1);
    }
    return h + a * a * dx + a * b * dx * dx + b * b / 3 * dx * dx * dx;

  }

  @Override
  public double firstDerivative(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(value, "value");
    Validate.notNull(data, "data bundle");
    Validate.isTrue(data instanceof Interpolator1DQuadraticSplineDataBundle);
    final Interpolator1DQuadraticSplineDataBundle quadraticData = (Interpolator1DQuadraticSplineDataBundle) data;

    final int n = data.size() - 1;
    final double[] xData = data.getKeys();

    double dx, a, b;
    if (value < data.firstKey()) {
      dx = value;
      a = quadraticData.getA(0);
      b = quadraticData.getB(0);
    } else if (value > data.lastKey()) {
      dx = value - xData[n];
      a = quadraticData.getA(n + 1);
      b = quadraticData.getB(n + 1);
    } else {
      final int low = data.getLowerBoundIndex(value);
      dx = value - xData[low];
      a = quadraticData.getA(low + 1);
      b = quadraticData.getB(low + 1);
    }
    return a * a + 2. * a * b * dx + b * b * dx * dx;

  }

  @Override
  public double[] getNodeSensitivitiesForValue(final Interpolator1DDataBundle data, final Double value) {
    throw new NotImplementedException();
  }

  @Override
  public Interpolator1DDataBundle getDataBundle(final double[] x, final double[] y) {
    return new Interpolator1DQuadraticSplineDataBundle(new ArrayInterpolator1DDataBundle(x, y));
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    return new Interpolator1DQuadraticSplineDataBundle(new ArrayInterpolator1DDataBundle(x, y, true));
  }

}
