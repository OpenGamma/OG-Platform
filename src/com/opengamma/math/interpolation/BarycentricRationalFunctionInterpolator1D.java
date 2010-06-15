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
public class BarycentricRationalFunctionInterpolator1D extends Interpolator1D<Interpolator1DModel> {
  private final int _degree;

  public BarycentricRationalFunctionInterpolator1D(final int degree) {
    if (degree < 1) {
      throw new IllegalArgumentException("Cannot perform interpolation with rational functions of degree < 1");
    }
    _degree = degree;
  }

  @Override
  public Double interpolate(final Interpolator1DModel model, final Double value) {
    Validate.notNull(value, "Value to be interpolated must not be null");
    Validate.notNull(model, "Model must not be null");
    checkValue(model, value);
    if (model.size() < _degree) {
      throw new InterpolationException("Cannot interpolate " + model.size() + " data points with rational functions of degree " + _degree);
    }
    final int m = model.size();
    final double[] x = model.getKeys();
    final double[] y = model.getValues();
    if (model.getLowerBoundIndex(value) == m - 1) {
      return y[m - 1];
    }
    final double[] w = getWeights(x);
    final int n = x.length;
    double delta, temp, num = 0, den = 0;
    for (int i = 0; i < n; i++) {
      delta = value - x[i];
      if (Math.abs(delta) < getEPS()) {
        return y[i];
      }
      temp = w[i] / delta;
      num += temp * y[i];
      den += temp;
    }
    return num / den;
  }

  private double[] getWeights(final double[] x) {
    final int n = x.length;
    final double[] w = new double[n];
    int iMin, iMax, mult, jMax;
    double sum, term;
    for (int k = 0; k < n; k++) {
      iMin = Math.max(k - _degree, 0);
      iMax = k >= n - _degree ? n - _degree - 1 : k;
      mult = iMin % 2 == 0 ? 1 : -1;
      sum = 0;
      for (int i = iMin; i <= iMax; i++) {
        jMax = Math.min(i + _degree, n - 1);
        term = 1;
        for (int j = i; j <= jMax; j++) {
          if (j == k) {
            continue;
          }
          term *= x[k] - x[j];
        }
        term = mult / term;
        mult *= -1;
        sum += term;
      }
      w[k] = sum;
    }
    return w;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null) {
      return false;
    }
    if (o == this) {
      return true;
    }
    if (!(o instanceof BarycentricRationalFunctionInterpolator1D)) {
      return false;
    }
    final BarycentricRationalFunctionInterpolator1D other = (BarycentricRationalFunctionInterpolator1D) o;
    return _degree == other._degree;
  }

  @Override
  public int hashCode() {
    return getClass().hashCode() * 17 + _degree;
  }
}
