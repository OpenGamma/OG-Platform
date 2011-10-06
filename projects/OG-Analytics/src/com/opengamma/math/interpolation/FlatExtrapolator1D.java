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
 */
public class FlatExtrapolator1D extends Interpolator1D {
  private static final long serialVersionUID = 1L;

  @Override
  public Interpolator1DDataBundle getDataBundle(final double[] x, final double[] y) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Double interpolate(final Interpolator1DDataBundle data, final Double value) {
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
