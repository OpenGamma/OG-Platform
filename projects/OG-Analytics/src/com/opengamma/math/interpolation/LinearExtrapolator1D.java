/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import org.apache.commons.lang.Validate;

import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 * @param <T>
 */
public class LinearExtrapolator1D<T extends Interpolator1DDataBundle> extends Interpolator1D<T> {
  private static final long serialVersionUID = 1L;
  private static final double EPS = 1e-8;
  private final Interpolator1D<T> _interpolator;

  public LinearExtrapolator1D(final Interpolator1D<T> interpolator) {
    Validate.notNull(interpolator, "interpolator");
    _interpolator = interpolator;
  }

  @Override
  public T getDataBundle(final double[] x, final double[] y) {
    return _interpolator.getDataBundle(x, y);
  }

  @Override
  public T getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    return _interpolator.getDataBundleFromSortedArrays(x, y);
  }

  @Override
  public Double interpolate(final T data, final Double value) {
    Validate.notNull(data, "data");
    Validate.notNull(value, "value");
    if (value < data.firstKey()) {
      return leftExtrapolate(data, value);
    } else if (value > data.lastKey()) {
      return rightExtrapolate(data, value);
    }
    throw new IllegalArgumentException("Value " + value + " was within data range");
  }

  private Double leftExtrapolate(final T data, final Double value) {
    Validate.notNull(data, "data");
    Validate.notNull(value, "value");
    final double x = data.firstKey();
    final double y = data.firstValue();
    final double eps = EPS * (data.lastKey() - x);
    final Interpolator1D<T> interpolator = _interpolator;
    final double m = (interpolator.interpolate(data, x + eps) - y) / eps;
    return y + (value - x) * m;
  }

  private Double rightExtrapolate(final T data, final Double value) {
    Validate.notNull(data, "data");
    Validate.notNull(value, "value");
    final double x = data.lastKey();
    final double y = data.lastValue();
    final double eps = EPS * (x - data.firstKey());
    final Interpolator1D<T> interpolator = _interpolator;
    final double m = (y - interpolator.interpolate(data, x - eps)) / eps;
    return y + (value - x) * m;
  }

}
