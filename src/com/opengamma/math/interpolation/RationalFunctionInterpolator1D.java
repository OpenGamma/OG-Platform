/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class RationalFunctionInterpolator1D extends Interpolator1D<Interpolator1DModel> {
  private final int _degree;

  public RationalFunctionInterpolator1D(final int degree) {
    if (degree < 1) {
      throw new IllegalArgumentException("Need a degree of at least 1 to perform rational function interpolation");
    }
    _degree = degree;
  }

  @Override
  public InterpolationResult<Double> interpolate(final Interpolator1DModel model, final Double value) {
    Validate.notNull(value, "Value to be interpolated must not be null");
    Validate.notNull(model, "Model must not be null");
    final int m = _degree + 1;
    if (model.size() < m) {
      throw new IllegalArgumentException("Need at least " + (_degree + 1) + " data points to perform this interpolation");
    }
    final double[] xArray = model.getKeys();
    final double[] yArray = model.getValues();
    double diff = Math.abs(value - xArray[0]);
    if (Math.abs(diff) < EPS) {
      return new InterpolationResult<Double>(yArray[0], 0.0);
    }
    double diff1;
    final double[] c = new double[m];
    final double[] d = new double[m];
    int ns = 0;
    for (int i = 0; i < m; i++) {
      diff1 = Math.abs(value - xArray[i]);
      if (diff < EPS) {
        return new InterpolationResult<Double>(yArray[i], 0.);
      } else if (diff1 < diff) {
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
        if (Math.abs(dd) < EPS) {
          throw new InterpolationException("Interpolating function has a pole at x = " + value);
        }
        dd = w / dd;
        d[j] = c[j + 1] * dd;
        c[j] = t * dd;
      }
      dy = 2 * (ns + 1) < m - i ? c[ns + 1] : d[ns--];
      y += dy;
    }
    return new InterpolationResult<Double>(y, dy);
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null) {
      return false;
    }
    if (o == this) {
      return true;
    }
    if (!(o instanceof RationalFunctionInterpolator1D)) {
      return false;
    }
    final RationalFunctionInterpolator1D other = (RationalFunctionInterpolator1D) o;
    return _degree == other._degree;
  }

  @Override
  public int hashCode() {
    return getClass().hashCode() * 17 + _degree;
  }

}
