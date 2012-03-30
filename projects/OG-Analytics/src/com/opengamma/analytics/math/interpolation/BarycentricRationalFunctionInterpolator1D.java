/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 */
public class BarycentricRationalFunctionInterpolator1D extends Interpolator1D {
  private static final long serialVersionUID = 1L;
  private final int _degree;
  private final double _eps;

  public BarycentricRationalFunctionInterpolator1D(final int degree, double eps) {
    Validate.isTrue(degree > 0, "Cannot perform interpolation with rational functions of degree < 1");
    _degree = degree;
    _eps = eps;
  }

  @Override
  public Double interpolate(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(value, "value");
    Validate.notNull(data, "data bundle");
    if (data.size() < _degree) {
      throw new MathException("Cannot interpolate " + data.size() + " data points with rational functions of degree " + _degree);
    }
    final int m = data.size();
    final double[] x = data.getKeys();
    final double[] y = data.getValues();
    if (data.getLowerBoundIndex(value) == m - 1) {
      return y[m - 1];
    }
    final double[] w = getWeights(x);
    final int n = x.length;
    double delta, temp, num = 0, den = 0;
    for (int i = 0; i < n; i++) {
      delta = value - x[i];
      if (Math.abs(delta) < _eps) {
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
  public Interpolator1DDataBundle getDataBundle(final double[] x, final double[] y) {
    return new ArrayInterpolator1DDataBundle(x, y);
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    return new ArrayInterpolator1DDataBundle(x, y, true);
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

  @Override
  public double[] getNodeSensitivitiesForValue(Interpolator1DDataBundle data, Double value) {
    return getFiniteDifferenceSensitivities(data, value);
  }

}
