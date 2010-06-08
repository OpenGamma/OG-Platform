/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import org.apache.commons.lang.Validate;

/**
 * Interpolates between data points using a polynomial. The method used is
 * Neville's algorithm.
 * 
 * @author emcleod
 * 
 */
public class PolynomialInterpolator1D extends Interpolator1D {
  private final int _degree;

  /**
   * 
   * @param degree
   *          Degree of the interpolating polynomial.
   * @throws IllegalArgumentException
   *           If the degree is less than 1.
   */
  public PolynomialInterpolator1D(final int degree) {
    if (degree < 1) {
      throw new IllegalArgumentException("Need a degree of at least 1 to perform polynomial interpolation");
    }
    _degree = degree;
  }

  @Override
  public InterpolationResult<Double> interpolate(final Interpolator1DModel model, final Double value) {
    Validate.notNull(value, "Value to be interpolated must not be null");
    Validate.notNull(model, "Model must not be null");
    if (model.size() < _degree + 1) {
      throw new IllegalArgumentException("Need at least " + (_degree + 1) + " data points to perform polynomial interpolation of degree " + _degree);
    }
    final int lower = model.getLowerBoundIndex(value);
    if (lower == model.size() - 1) {
      return new InterpolationResult<Double>(model.lastValue());
    }
    final double[] xArray = model.getKeys();
    final double[] yArray = model.getValues();
    final double[] c = new double[_degree + 1];
    final double[] d = new double[_degree + 1];
    if (lower + _degree >= model.size()) {
      throw new InterpolationException("Lower bound index of x (=" + lower + ") is within " + _degree + " data points of end of series (length = " + model.size() + ")");
    }
    int ns = Math.abs(value - xArray[lower]) < Math.abs(value - xArray[lower + 1]) ? 0 : 1;
    for (int i = 0; i <= _degree; i++) {
      c[i] = yArray[i + lower];
      d[i] = c[i];
    }
    double y = yArray[lower + ns--];
    double dy = 0;
    double diff = 0;
    for (int i = 1; i <= _degree; i++) {
      for (int j = 0, k = lower; j <= _degree - i; j++, k++) {
        if (Math.abs(xArray[k] - xArray[k + 1]) < EPS) {
          throw new InterpolationException("Two values of x were within " + EPS + ": " + xArray[k] + " " + xArray[k + i]);
        }
        diff = (c[j + 1] - d[j]) / (xArray[k] - xArray[k + i]);
        d[j] = (xArray[k + i] - value) * diff;
        c[j] = (xArray[k] - value) * diff;
      }
      dy = 2 * (ns + 1) <= _degree - i ? c[ns + 1] : d[ns--];
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
    if (!(o instanceof PolynomialInterpolator1D)) {
      return false;
    }
    final PolynomialInterpolator1D other = (PolynomialInterpolator1D) o;
    return _degree == other._degree;
  }

  @Override
  public int hashCode() {
    return getClass().hashCode() * 17 + _degree;
  }

}
