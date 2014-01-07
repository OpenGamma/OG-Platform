/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;

/**
 * This left extrapolator is designed for extrapolating a discount factor where the trivial point (0.,1.) is NOT involved in the data. 
 * The extrapolation is completed by applying a quadratic extrapolant on the discount factor (not log of the discount factor), 
 * where the point (0.,1.) is inserted and the first derivative value is assumed to be continuous at firstKey.
 */
public class QuadraticPolynomialLeftExtrapolator extends Interpolator1D {
  private static final long serialVersionUID = 1L;
  private final Interpolator1D _interpolator;
  private final double _eps;

  /**
   * @param interpolator Interpolator for specifying the first derivative value at an endpoint
   */
  public QuadraticPolynomialLeftExtrapolator(final Interpolator1D interpolator) {
    this(interpolator, 1e-8);
  }

  /**
   * @param interpolator Interpolator for specifying the first derivative value at an endpoint
   * @param eps Bump parameter of finite difference approximation for the first derivative value
   */
  public QuadraticPolynomialLeftExtrapolator(final Interpolator1D interpolator, double eps) {
    Validate.notNull(interpolator, "interpolator");
    _interpolator = interpolator;
    _eps = eps;
  }

  @Override
  public Interpolator1DDataBundle getDataBundle(final double[] x, final double[] y) {
    return _interpolator.getDataBundle(x, y);
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    return _interpolator.getDataBundleFromSortedArrays(x, y);
  }

  @Override
  public Double interpolate(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(data, "data");
    Validate.notNull(value, "value");
    if (data.firstKey() == 0.) {
      throw new IllegalArgumentException("The trivial point at key=0. is already included");
    }
    if (value < data.firstKey()) {
      return leftExtrapolate(data, value);
    } else if (value > data.lastKey()) {
      throw new IllegalArgumentException("Value " + value + " was greater than data range");
    }
    throw new IllegalArgumentException("Value " + value + " was within data range");
  }

  @Override
  public double firstDerivative(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(data, "data");
    Validate.notNull(value, "value");
    if (data.firstKey() == 0.) {
      throw new IllegalArgumentException("The trivial point at key=0. is already included");
    }
    if (value < data.firstKey()) {
      return leftExtrapolateDerivative(data, value);
    } else if (value > data.lastKey()) {
      throw new IllegalArgumentException("Value " + value + " was greater than data range");
    }
    throw new IllegalArgumentException("Value " + value + " was within data range");
  }

  @Override
  public double[] getNodeSensitivitiesForValue(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(data, "data");
    if (data.firstKey() == 0.) {
      throw new IllegalArgumentException("The trivial point at key=0. is already included");
    }
    if (value < data.firstKey()) {
      return getLeftSensitivities(data, value);
    } else if (value > data.lastKey()) {
      throw new IllegalArgumentException("Value " + value + " was greater than data range");
    }
    throw new IllegalArgumentException("Value " + value + " was within data range");
  }

  private Double leftExtrapolate(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(data, "data");
    Validate.notNull(value, "value");
    final double x = data.firstKey();
    final double y = data.firstValue();
    final double m = _interpolator.firstDerivative(data, x);
    final double quadCoef = m / x - (y - 1.) / x / x;
    final double linCoef = -m + 2. * (y - 1.) / x;
    return quadCoef * value * value + linCoef * value + 1.;
  }

  private Double leftExtrapolateDerivative(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(data, "data");
    Validate.notNull(value, "value");
    final double x = data.firstKey();
    final double y = data.firstValue();
    final double m = _interpolator.firstDerivative(data, x);
    final double quadCoef = m / x - (y - 1.) / x / x;
    final double linCoef = -m + 2. * (y - 1.) / x;
    return 2. * quadCoef * value + linCoef;
  }

  private double[] getLeftSensitivities(final Interpolator1DDataBundle data, final Double value) {
    final double eps = _eps * (data.lastKey() - data.firstKey());
    final double x = data.firstKey();
    final double[] result = _interpolator.getNodeSensitivitiesForValue(data, x + eps);

    final int n = result.length;
    for (int i = 1; i < n; i++) {
      final double tmp = result[i] * value / eps;
      result[i] = tmp / x * value - tmp;
    }
    final double tmp = (result[0] - 1.) / eps;
    result[0] = (tmp / x - 1. / x / x) * value * value + (2. / x - tmp) * value;
    return result;
  }

}
