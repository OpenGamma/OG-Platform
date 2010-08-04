/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.data;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.RealPolynomialFunction1D;

/**
 * 
 */
public class Interpolator1DDoubleQuadraticDataBundle implements Interpolator1DDataBundle {
  private final Interpolator1DDataBundle _underlyingData;
  private final RealPolynomialFunction1D[] _quadratics;

  public Interpolator1DDoubleQuadraticDataBundle(final Interpolator1DDataBundle underlyingData) {
    Validate.notNull(underlyingData);
    _underlyingData = underlyingData;
    _quadratics = getQuadratics(underlyingData);
  }

  private RealPolynomialFunction1D[] getQuadratics(final Interpolator1DDataBundle underlyingData) {
    final int n = underlyingData.size() - 1;
    final double[] xData = underlyingData.getKeys();
    final double[] yData = underlyingData.getValues();
    final RealPolynomialFunction1D[] coef = new RealPolynomialFunction1D[n - 1];
    for (int i = 1; i < n; i++) {
      coef[i - 1] = getQuadratic(xData, yData, i);
    }
    return coef;
  }

  private RealPolynomialFunction1D getQuadratic(final double[] x, final double[] y, final int index) {
    final double a = y[index];
    final double dx1 = x[index] - x[index - 1];
    final double dx2 = x[index + 1] - x[index];
    final double dy1 = y[index] - y[index - 1];
    final double dy2 = y[index + 1] - y[index];
    final double b = (dx1 * dy2 / dx2 + dx2 * dy1 / dx1) / (dx1 + dx2);
    final double c = (dy2 / dx2 - dy1 / dx1) / (dx1 + dx2);
    return new RealPolynomialFunction1D(new double[] {a, b, c});
  }

  public RealPolynomialFunction1D getQuadratic(final int index) {
    return _quadratics[index];
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
    return _underlyingData.higherKey(key);
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

}
