/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.Map;
import java.util.TreeMap;

/**
 * 
 * @author emcleod
 * 
 */

public class RationalFunctionInterpolator1D extends Interpolator1D {
  private final int _degree;

  public RationalFunctionInterpolator1D(final int degree) {
    if (degree < 1)
      throw new IllegalArgumentException("Need a degree of at least 1 to perform rational function interpolation");
    _degree = degree;
  }

  @Override
  public InterpolationResult<Double> interpolate(final Map<Double, Double> data, final Double value) {
    // REVIEW kirk 2009-12-30 -- If the inputs are already sorted, we shouldn't double-sort it.
    final TreeMap<Double, Double> sorted = initData(data);
    final int m = _degree + 1;
    if (data.size() < m)
      throw new IllegalArgumentException("Need at least " + (_degree + 1) + " data points to perform this interpolation");
    // REVIEW kirk 2009-12-30 -- It may make sense to convert these early on
    // to double[] and take the conversion hit once, rather than on every loop
    // through the arrays.
    final Double[] xArray = sorted.keySet().toArray(new Double[0]);
    final Double[] yArray = sorted.values().toArray(new Double[0]);
    double diff = Math.abs(value - xArray[0]);
    if (Math.abs(diff) < EPS)
      return new InterpolationResult<Double>(yArray[0], 0.0);
    double diff1;
    final double[] c = new double[m];
    final double[] d = new double[m];
    int ns = 0;
    for (int i = 0; i < m; i++) {
      diff1 = Math.abs(value - xArray[i]);
      if (diff < EPS)
        return new InterpolationResult<Double>(yArray[i], 0.);
      else if (diff1 < diff) {
        ns = i;
        diff = diff1;
      }
      c[i] = yArray[i];
      d[i] = yArray[i] + EPS;
    }
    double y = yArray[ns--];
    double w, t, dd, dy = 0;
    for (int i = 1; i < m; i++) {
      for (int j = 0; j < m - i; j++) {
        w = c[j + 1] - d[j];
        diff = xArray[i + j] - value;
        t = (xArray[j] - value) * d[j] / diff;
        dd = t - c[j + 1];
        if (Math.abs(dd) < EPS)
          throw new InterpolationException("Interpolating function has a pole at x = " + value);
        dd = w / dd;
        d[j] = c[j + 1] * dd;
        c[j] = t * dd;
      }
      dy = 2 * (ns + 1) < m - i ? c[ns + 1] : d[ns--];
      y += dy;
    }
    return new InterpolationResult<Double>(y, dy);
  }
}
