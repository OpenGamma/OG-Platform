/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;

/**
 * Extrapolator based on a exponential function. Outside the data range the function is an exponential exp(m*x) where m is such that 
 *  - on the left: exp(m * data.firstKey()) = data.firstValue()
 *  - on the right: exp(m * data.lastKey()) = data.lastValue()
 */
public class ExponentialExtrapolator1D extends Interpolator1D {

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
    final double m = Math.log(y) / x;
    return Math.exp(m * value);
  }

  private Double rightExtrapolate(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(data, "data");
    Validate.notNull(value, "value");
    final double x = data.lastKey();
    final double y = data.lastValue();
    final double m = Math.log(y) / x;
    return Math.exp(m * value);
  }

  private Double leftExtrapolateDerivative(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(data, "data");
    Validate.notNull(value, "value");
    final double x = data.firstKey();
    final double y = data.firstValue();
    final double m = Math.log(y) / x;
    return m * Math.exp(m * value);
  }

  private Double rightExtrapolateDerivative(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(data, "data");
    Validate.notNull(value, "value");
    final double x = data.lastKey();
    final double y = data.lastValue();
    final double m = Math.log(y) / x;
    return m * Math.exp(m * value);
  }

  private double[] getLeftSensitivities(final Interpolator1DDataBundle data, final double value) {
    Validate.notNull(data, "data");
    Validate.notNull(value, "value");
    final double x = data.firstKey();
    final double y = data.firstValue();
    final double m = Math.log(y) / x;
    final double ex = Math.exp(m * value);
    final double[] result = new double[data.size()];
    result[0] = ex * value / (x * y);
    return result;
  }

  private double[] getRightSensitivities(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(data, "data");
    Validate.notNull(value, "value");
    final double x = data.lastKey();
    final double y = data.lastValue();
    final double m = Math.log(y) / x;
    final double ex = Math.exp(m * value);
    final double[] result = new double[data.size()];
    result[data.size() - 1] = ex * value / (x * y);
    return result;
  }

}
