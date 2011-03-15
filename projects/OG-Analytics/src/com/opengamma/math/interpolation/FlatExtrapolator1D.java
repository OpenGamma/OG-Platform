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
public class FlatExtrapolator1D<T extends Interpolator1DDataBundle> extends Interpolator1D<T> {
  private static final long serialVersionUID = 1L;

  @Override
  public T getDataBundle(final double[] x, final double[] y) {
    throw new UnsupportedOperationException();
  }

  @Override
  public T getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Double interpolate(final T data, final Double value) {
    Validate.notNull(data, "data");
    Validate.notNull(value, "value");
    if (value < data.firstKey()) {
      return data.firstValue();
    } else if (value > data.lastKey()) {
      return data.lastValue();
    }
    throw new IllegalArgumentException("Value " + value + " was within data range");
  }

}
