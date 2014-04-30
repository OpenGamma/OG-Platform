/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;

/**
 * Log-linear extrapolator: the extrapolant is exp(f(x)) where f(x) is a linear function 
 * which is smoothly connected with a log-interpolator exp(F(x)), such as {@link LogNaturalCubicMonotonicityPreservingInterpolator1D}, 
 * i.e., F'(x) = f'(x) at a respectivie endpoint. 
 */
public class LogLinearExtrapolator1D extends Interpolator1D {
  private static final long serialVersionUID = 1L;
  private final Interpolator1D _interpolator;
  private final double _eps;

  /**
   * @param interpolator Interpolator for specifying the first derivative value at an endpoint
   */
  public LogLinearExtrapolator1D(final Interpolator1D interpolator) {
    this(interpolator, 1e-8);
  }

  /**
   * @param interpolator Interpolator for specifying the first derivative value at an endpoint
   * @param eps Bump parameter of finite difference approximation for the first derivative value
   */
  public LogLinearExtrapolator1D(final Interpolator1D interpolator, double eps) {
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
    if (value < data.firstKey()) {
      return leftExtrapolate(data, value);
    } else if (value > data.lastKey()) {
      return rightExtrapolate(data, value);
    }
    throw new IllegalArgumentException("Value " + value + " was within data range");
  }

  @Override
  public double firstDerivative(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(data, "data");
    Validate.notNull(value, "value");
    if (value < data.firstKey()) {
      return leftExtrapolateDerivative(data, value);
    } else if (value > data.lastKey()) {
      return rightExtrapolateDerivative(data, value);
    }
    throw new IllegalArgumentException("Value " + value + " was within data range");
  }

  @Override
  public double[] getNodeSensitivitiesForValue(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(data, "data");
    if (value < data.firstKey()) {
      return getLeftSensitivities(data, value);
    } else if (value > data.lastKey()) {
      return getRightSensitivities(data, value);
    }
    throw new IllegalArgumentException("Value " + value + " was within data range");
  }

  private Double leftExtrapolate(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(data, "data");
    Validate.notNull(value, "value");
    final double x = data.firstKey();
    final double y = Math.log(data.firstValue());
    final double m = _interpolator.firstDerivative(data, x) / _interpolator.interpolate(data, x);
    return Math.exp(y + (value - x) * m);
  }

  private Double rightExtrapolate(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(data, "data");
    Validate.notNull(value, "value");
    final double x = data.lastKey();
    final double y = Math.log(data.lastValue());
    final double m = _interpolator.firstDerivative(data, x) / _interpolator.interpolate(data, x);
    return Math.exp(y + (value - x) * m);
  }

  private Double leftExtrapolateDerivative(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(data, "data");
    Validate.notNull(value, "value");
    final double x = data.firstKey();
    final double y = Math.log(data.firstValue());
    final double m = _interpolator.firstDerivative(data, x) / _interpolator.interpolate(data, x);
    return m * Math.exp(y + (value - x) * m);
  }

  private Double rightExtrapolateDerivative(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(data, "data");
    Validate.notNull(value, "value");
    final double x = data.lastKey();
    final double y = Math.log(data.lastValue());
    final double m = _interpolator.firstDerivative(data, x) / _interpolator.interpolate(data, x);
    return m * Math.exp(y + (value - x) * m);
  }

  private double[] getLeftSensitivities(final Interpolator1DDataBundle data, final Double value) {
    final double eps = _eps * (data.lastKey() - data.firstKey());
    final double x = data.firstKey();
    final double resValueInterpolator = _interpolator.interpolate(data, x + eps);
    final double resValueExtrapolator = leftExtrapolate(data, value);
    final double[] result = _interpolator.getNodeSensitivitiesForValue(data, x + eps);
    final double factor1 = (value - x) / eps;
    final double factor2 = factor1 * resValueExtrapolator / resValueInterpolator;

    final int n = result.length;
    for (int i = 1; i < n; i++) {
      result[i] *= factor2;
    }
    result[0] = result[0] * factor2 + (1. - factor1) * resValueExtrapolator / data.firstValue();
    return result;
  }

  private double[] getRightSensitivities(final Interpolator1DDataBundle data, final Double value) {
    final double eps = _eps * (data.lastKey() - data.firstKey());
    final double x = data.lastKey();
    final double resValueInterpolator = _interpolator.interpolate(data, x - eps);
    final double resValueExtrapolator = rightExtrapolate(data, value);
    final double[] result = _interpolator.getNodeSensitivitiesForValue(data, x - eps);
    final double factor1 = (value - x) / eps;
    final double factor2 = factor1 * resValueExtrapolator / resValueInterpolator;

    final int n = result.length;
    for (int i = 0; i < n - 1; i++) {
      result[i] *= -factor2;
    }
    result[n - 1] = (1. + factor1) * resValueExtrapolator / data.lastValue() - result[n - 1] * factor2;
    return result;
  }
}
