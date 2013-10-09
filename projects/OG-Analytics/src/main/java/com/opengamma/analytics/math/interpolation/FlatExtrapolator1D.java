/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;

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

  @Override
  public double firstDerivative(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(data, "data");
    Validate.notNull(value, "value");
    if (value < data.firstKey()) {
      return 0.;
    } else if (value > data.lastKey()) {
      return 0.;
    }
    throw new IllegalArgumentException("Value " + value + " was within data range");
  }

  @Override
  public double[] getNodeSensitivitiesForValue(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(data, "data");
    final int n = data.size();
    if (value < data.firstKey()) {
      final double[] result = new double[n];
      result[0] = 1;
      return result;
    } else if (value > data.lastKey()) {
      final double[] result = new double[n];
      result[n - 1] = 1;
      return result;
    }
    throw new IllegalArgumentException("Value " + value + " was within data range");
  }
}
