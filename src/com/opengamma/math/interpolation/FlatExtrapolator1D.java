/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import org.apache.commons.lang.Validate;

import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.temp.InterpolationResult;

/**
 * 
 * @param <T>
 */
public class FlatExtrapolator1D<T extends Interpolator1DDataBundle> extends Interpolator1D<T, InterpolationResult> {

  @Override
  public T getDataBundle(final double[] x, final double[] y) {
    throw new UnsupportedOperationException();
  }

  @Override
  public T getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    throw new UnsupportedOperationException();
  }

  @Override
  public InterpolationResult interpolate(final T data, final Double value) {
    Validate.notNull(data, "data");
    Validate.notNull(value, "value");
    if (value < data.firstKey()) {
      return new InterpolationResult(data.firstValue());
    } else if (value > data.lastKey()) {
      return new InterpolationResult(data.lastValue());
    }
    throw new IllegalArgumentException("Value " + value + " was within data range");
  }

}
