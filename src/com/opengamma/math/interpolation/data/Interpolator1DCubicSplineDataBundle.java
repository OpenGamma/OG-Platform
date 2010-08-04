/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.data;

import org.apache.commons.lang.Validate;


/**
 * 
 */
public class Interpolator1DCubicSplineDataBundle implements Interpolator1DDataBundle {
  private final Interpolator1DDataBundle _underlyingData;
  private final double[] _secondDerivatives;

  public Interpolator1DCubicSplineDataBundle(final Interpolator1DDataBundle underlyingData) {
    Validate.notNull(underlyingData);
    _underlyingData = underlyingData;
    _secondDerivatives = getSecondDerivative(underlyingData);
  }

  private double[] getSecondDerivative(final Interpolator1DDataBundle underlyingData) {
    final double[] x = underlyingData.getKeys();
    final double[] y = underlyingData.getValues();
    final int n = x.length;
    final double[] y2 = new double[n];
    double p, ratio;
    final double[] u = new double[n - 1];
    y2[0] = 0.0;
    u[0] = 0.0;
    for (int i = 1; i < n - 1; i++) {
      ratio = (x[i] - x[i - 1]) / (x[i + 1] - x[i - 1]);
      p = ratio * y2[i - 1] + 2.0;
      y2[i] = (ratio - 1.0) / p;
      u[i] = (y[i + 1] - y[i]) / (x[i + 1] - x[i]) - (y[i] - y[i - 1]) / (x[i] - x[i - 1]);
      u[i] = (6.0 * u[i] / (x[i + 1] - x[i - 1]) - ratio * u[i - 1]) / p;
    }
    y2[n - 1] = 0.0;
    for (int k = n - 2; k >= 0; k--) {
      y2[k] = y2[k] * y2[k + 1] + u[k];
    }
    return y2;
  }

  @Override
  public boolean containsKey(final Double key) {
    return _underlyingData.containsKey(key);
  }

  @Override
  public Double firstKey() {
    return _underlyingData.firstKey();
  }

  @Override
  public Double firstValue() {
    return _underlyingData.firstValue();
  }

  @Override
  public Double get(final Double key) {
    return _underlyingData.get(key);
  }

  @Override
  public InterpolationBoundedValues getBoundedValues(final Double key) {
    return _underlyingData.getBoundedValues(key);
  }

  @Override
  public double[] getKeys() {
    return _underlyingData.getKeys();
  }

  @Override
  public int getLowerBoundIndex(final Double value) {
    return _underlyingData.getLowerBoundIndex(value);
  }

  @Override
  public Double getLowerBoundKey(final Double value) {
    return _underlyingData.getLowerBoundKey(value);
  }

  @Override
  public double[] getValues() {
    return _underlyingData.getValues();
  }

  @Override
  public Double higherKey(final Double key) {
    return _underlyingData.higherKey(key);
  }

  @Override
  public Double higherValue(final Double key) {
    return _underlyingData.higherValue(key);
  }

  @Override
  public Double lastKey() {
    return _underlyingData.lastKey();
  }

  @Override
  public Double lastValue() {
    return _underlyingData.lastValue();
  }

  @Override
  public int size() {
    return _underlyingData.size();
  }

  public double[] getSecondDerivatives() {
    return _secondDerivatives;
  }
}
