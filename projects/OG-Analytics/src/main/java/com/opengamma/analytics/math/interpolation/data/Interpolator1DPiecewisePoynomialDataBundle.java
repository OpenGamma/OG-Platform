/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation.data;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.analytics.math.interpolation.PiecewisePolynomialInterpolator;
import com.opengamma.analytics.math.interpolation.PiecewisePolynomialResultsWithSensitivity;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class Interpolator1DPiecewisePoynomialDataBundle implements Interpolator1DDataBundle {

  //  private static final PiecewisePolynomialFunction1D FUNC = new PiecewisePolynomialFunction1D();
  //
  //  private final PiecewisePolynomialResult _poly;
  private final PiecewisePolynomialResultsWithSensitivity _poly;
  private final Interpolator1DDataBundle _underlyingData;

  //  public Interpolator1DPiecewisePoynomialDataBundle(final PiecewisePolynomialResult polyRes) {
  //    ArgumentChecker.notNull(polyRes, "null polyRes");
  //    _poly = polyRes;
  //
  //    // TODO don't really need to back this data back out of PiecewisePolynomialResult. Do this to quickly satisfy the existing interpolator API 
  //    double[] knots = polyRes.getKnots().getData();
  //    final int n = knots.length;
  //    DoubleMatrix2D mat = polyRes.getCoefMatrix();
  //    ArgumentChecker.isTrue((n - 1) == mat.getNumberOfRows(), "Coef matrix wrong size");
  //    final int order = mat.getNumberOfColumns();
  //    double[] values = new double[n];
  //    for (int i = 0; i < (n - 1); i++) {
  //      values[i] = mat.getEntry(i, order - 1);
  //    }
  //    values[n - 1] = FUNC.evaluate(polyRes, knots[n - 1]).getEntry(0);
  //
  //    _underlyingData = new ArrayInterpolator1DDataBundle(knots, values, true);
  //
  //  }

  /**
   * Constructor where coefficients for interpolant and its node sensitivity are computed 
   * @param underlyingData Contains sorted data (x,y)
   * @param method {@link PiecewisePolynomialInterpolator}
   */
  public Interpolator1DPiecewisePoynomialDataBundle(final Interpolator1DDataBundle underlyingData, final PiecewisePolynomialInterpolator method) {
    ArgumentChecker.notNull(underlyingData, "underlying data");
    ArgumentChecker.notNull(method, "method");

    _underlyingData = underlyingData;
    _poly = method.interpolateWithSensitivity(underlyingData.getKeys(), underlyingData.getValues());
  }

  /**
   * @param underlyingData Contains sorted data (x,y)
   * @param method  {@link PiecewisePolynomialInterpolator}
   * @param leftCond  Condition on left endpoint
   * @param rightCond  Condition on right endpoint
   */
  public Interpolator1DPiecewisePoynomialDataBundle(final Interpolator1DDataBundle underlyingData, final PiecewisePolynomialInterpolator method, final double leftCond, final double rightCond) {
    ArgumentChecker.notNull(underlyingData, "underlying data");
    ArgumentChecker.notNull(method, "method");

    _underlyingData = underlyingData;
    final double[] yValues = underlyingData.getValues();
    final int nData = yValues.length;
    final double[] yValuesMod = new double[nData + 2];
    yValuesMod[0] = leftCond;
    yValuesMod[nData + 1] = rightCond;
    System.arraycopy(yValues, 0, yValuesMod, 1, nData);

    _poly = method.interpolateWithSensitivity(underlyingData.getKeys(), yValuesMod);
  }

  //  public PiecewisePolynomialResult getPiecewisePolynomialResult() {
  //    return _poly;
  //  }

  /**
   * Access PiecewisePolynomialResultsWithSensitivity
   * @return PiecewisePolynomialResultsWithSensitivity
   */
  public PiecewisePolynomialResultsWithSensitivity getPiecewisePolynomialResultsWithSensitivity() {
    return _poly;
  }

  /**
   * Get x values of breakpoints, which are different from "keys" for certain interpolations
   * @return X values of breakpoints
   */
  public double[] getBreakpointsX() {
    return _poly.getKnots().getData();
  }

  /**
   * Get y values of breakpoints, which are different from "values" for certain interpolations
   * @return Y values of breakpoints
   */
  public double[] getBreakPointsY() {
    final int nKnots = _poly.getKnots().getNumberOfElements();
    final double[][] coefMat = _poly.getCoefMatrix().getData();
    final int nCoefs = coefMat[0].length;
    final double[] values = new double[nKnots];
    for (int i = 0; i < nKnots - 1; i++) {
      values[i] = coefMat[i][nCoefs - 1];
    }
    values[nKnots - 1] = _underlyingData.lastValue();

    return values;
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
