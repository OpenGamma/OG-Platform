/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.Map;
import java.util.TreeMap;

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

  /**
   * 
   * @param data
   *          A map containing the (x, y) data points.
   * @param value
   *          The value of x for which the interpolated point y is required.
   * @returns An InterpolationResult containing the value of the interpolated
   *          point and the interpolation error.
   * @throws IllegalArgumentException
   *           If the x value is null; if the number of data points is smaller
   *           than (degree + 1)
   * @throws InterpolationException
   *           If the next lowest point in the sorted (x, y) data is within the
   *           degree of the polynomial number of data points of the end of the
   *           data; if two x points are equal.
   */
  @Override
  public InterpolationResult<Double> interpolate(final Map<Double, Double> data, final Double value) {
    final TreeMap<Double, Double> sorted = initData(data);
    if (data.size() < _degree + 1) {
      throw new IllegalArgumentException("Need at least " + (_degree + 1) + " data points to perform polynomial interpolation of degree " + _degree);
    }
    final int lower = getLowerBoundIndex(sorted, value);
    final Double[] xArray = sorted.keySet().toArray(new Double[0]);
    final Double[] yArray = sorted.values().toArray(new Double[0]);
    final double[] c = new double[_degree + 1];
    final double[] d = new double[_degree + 1];
    if (lower + _degree >= data.size()) {
      throw new InterpolationException("Lower bound index of x (=" + lower + ") is within " + _degree + " data points of end of series (length = " + data.size() + ")");
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
}
