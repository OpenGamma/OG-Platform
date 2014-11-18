/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class LinearExtrapolator1D extends Interpolator1D {
  private static final long serialVersionUID = 1L;
  private final Interpolator1D _interpolator;
  private final double _eps;

  public LinearExtrapolator1D(final Interpolator1D interpolator) { 
    this(interpolator, 1e-8);
  }

  public LinearExtrapolator1D(final Interpolator1D interpolator, final double eps) {
    _interpolator = ArgumentChecker.notNull(interpolator, "interpolator");
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
    final double y = data.firstValue();
    final double eps = _eps * (data.lastKey() - x);
    final double m = (_interpolator.interpolate(data, x + eps) - y) / eps;
    return y + (value - x) * m;
  }

  private Double rightExtrapolate(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(data, "data");
    Validate.notNull(value, "value");
    final double x = data.lastKey();
    final double y = data.lastValue();
    final double eps = _eps * (x - data.firstKey());
    final double m = (y - _interpolator.interpolate(data, x - eps)) / eps;
    return y + (value - x) * m;
  }

  private Double leftExtrapolateDerivative(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(data, "data");
    Validate.notNull(value, "value");
    final double x = data.firstKey();
    final double y = data.firstValue();
    final double eps = _eps * (data.lastKey() - x);
    final double m = (_interpolator.interpolate(data, x + eps) - y) / eps;
    return m;
  }

  private Double rightExtrapolateDerivative(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(data, "data");
    Validate.notNull(value, "value");
    final double x = data.lastKey();
    final double y = data.lastValue();
    final double eps = _eps * (x - data.firstKey());
    final double m = (y - _interpolator.interpolate(data, x - eps)) / eps;
    return m;
  }

  private double[] getLeftSensitivities(final Interpolator1DDataBundle data, final double value) {
    final double eps = _eps * (data.lastKey() - data.firstKey());
    final double x = data.firstKey();
    final double[] result = _interpolator.getNodeSensitivitiesForValue(data, x + eps);
    final int n = result.length;
    for (int i = 1; i < n; i++) {
      result[i] = result[i] * (value - x) / eps;
    }
    result[0] = 1 + (result[0] - 1) * (value - x) / eps;
    return result;
  }

  private double[] getRightSensitivities(final Interpolator1DDataBundle data, final Double value) {
    final double eps = _eps * (data.lastKey() - data.firstKey());
    final double x = data.lastKey();
    final double[] result = _interpolator.getNodeSensitivitiesForValue(data, x - eps);
    final int n = result.length;
    for (int i = 0; i < n - 1; i++) {
      result[i] = -result[i] * (value - x) / eps;
    }
    result[n - 1] = 1 + (1 - result[n - 1]) * (value - x) / eps;
    return result;
  }
}
