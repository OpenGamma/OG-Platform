/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation.data;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.math.function.PiecewisePolynomialFunction1D;
import com.opengamma.analytics.math.interpolation.PiecewisePolynomialResult;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class Interpolator1DPiecewisePoynomialDataBundle implements Interpolator1DDataBundle {

  private static final PiecewisePolynomialFunction1D FUNC = new PiecewisePolynomialFunction1D();

  private final PiecewisePolynomialResult _poly;
  private final Interpolator1DDataBundle _underlyingData;

  public Interpolator1DPiecewisePoynomialDataBundle(final PiecewisePolynomialResult polyRes) {
    ArgumentChecker.notNull(polyRes, "null polyRes");
    _poly = polyRes;

    // TODO don't really need to back this data back out of PiecewisePolynomialResult. Do this to quickly satisfy the existing interpolator API 
    double[] knots = polyRes.getKnots().getData();
    final int n = knots.length;
    DoubleMatrix2D mat = polyRes.getCoefMatrix();
    ArgumentChecker.isTrue((n - 1) == mat.getNumberOfRows(), "Coef matrix wrong size");
    final int order = mat.getNumberOfColumns();
    double[] values = new double[n];
    for (int i = 0; i < (n - 1); i++) {
      values[i] = mat.getEntry(i, order - 1);
    }
    values[n - 1] = FUNC.evaluate(polyRes, knots[n - 1]).getEntry(0);

    _underlyingData = new ArrayInterpolator1DDataBundle(knots, values, true);

  }

  public PiecewisePolynomialResult getPiecewisePolynomialResult() {
    return _poly;
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

  @Override
  public void setYValueAtIndex(int index, double y) {
    throw new NotImplementedException();
  }

}
