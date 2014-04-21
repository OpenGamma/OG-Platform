/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.PiecewisePolynomialWithSensitivityFunction1D;
import com.opengamma.analytics.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DPiecewisePoynomialDataBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;

/**
 * Wrapper class for {@link PiecewisePolynomialInterpolator} 
 */
public abstract class PiecewisePolynomialInterpolator1D extends Interpolator1D {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  private PiecewisePolynomialInterpolator _baseMethod;
  private static final PiecewisePolynomialWithSensitivityFunction1D FUNC = new PiecewisePolynomialWithSensitivityFunction1D();

  /**
   * Constructor 
   * @param method Piecewise polynomial interpolation method
   */
  public PiecewisePolynomialInterpolator1D(final PiecewisePolynomialInterpolator method) {
    _baseMethod = method;
  }

  @Override
  public Double interpolate(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(value, "value");
    Validate.notNull(data, "data bundle");
    Validate.isTrue(data instanceof Interpolator1DPiecewisePoynomialDataBundle);
    final Interpolator1DPiecewisePoynomialDataBundle polyData = (Interpolator1DPiecewisePoynomialDataBundle) data;
    final DoubleMatrix1D res = FUNC.evaluate(polyData.getPiecewisePolynomialResultsWithSensitivity(), value);
    return res.getEntry(0);
  }

  @Override
  public double firstDerivative(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(value, "value");
    Validate.notNull(data, "data bundle");
    Validate.isTrue(data instanceof Interpolator1DPiecewisePoynomialDataBundle);
    final Interpolator1DPiecewisePoynomialDataBundle polyData = (Interpolator1DPiecewisePoynomialDataBundle) data;
    final DoubleMatrix1D res = FUNC.differentiate(polyData.getPiecewisePolynomialResultsWithSensitivity(), value);
    return res.getEntry(0);
  }

  @Override
  public double[] getNodeSensitivitiesForValue(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(value, "value");
    Validate.notNull(data, "data bundle");
    Validate.isTrue(data instanceof Interpolator1DPiecewisePoynomialDataBundle);
    final Interpolator1DPiecewisePoynomialDataBundle polyData = (Interpolator1DPiecewisePoynomialDataBundle) data;
    final DoubleMatrix1D res = FUNC.nodeSensitivity(polyData.getPiecewisePolynomialResultsWithSensitivity(), value);
    return res.getData();
  }

  @Override
  public Interpolator1DDataBundle getDataBundle(final double[] x, final double[] y) {
    return new Interpolator1DPiecewisePoynomialDataBundle(new ArrayInterpolator1DDataBundle(x, y, false), this._baseMethod);
  }

  /**
   * Data bundle builder ONLY FOR cubic spline interpolator or hyman filters on cubic spline interpolator using Clamped endpoint conditions
   * @param x X values of data
   * @param y Y values of data
   * @param leftCond First derivative value at left endpoint 
   * @param rightCond First derivative value at right endpoint 
   * @return {@link Interpolator1DPiecewisePoynomialDataBundle}
   */
  public Interpolator1DDataBundle getDataBundle(final double[] x, final double[] y, final double leftCond, final double rightCond) {
    if (!(_baseMethod.getPrimaryMethod() instanceof CubicSplineInterpolator)) {
      throw new IllegalArgumentException("No degrees of freedom at endpoints for this interpolation method");
    }
    return new Interpolator1DPiecewisePoynomialDataBundle(new ArrayInterpolator1DDataBundle(x, y, false), this._baseMethod, leftCond, rightCond);
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    return new Interpolator1DPiecewisePoynomialDataBundle(new ArrayInterpolator1DDataBundle(x, y, true), this._baseMethod);
  }

  /**
   * Data bundle builder ONLY FOR cubic spline interpolator or hyman filters on cubic spline interpolator using Clamped endpoint conditions
   * @param x X values of data
   * @param y Y values of data
   * @param leftCond First derivative value at left endpoint 
   * @param rightCond First derivative value at right endpoint 
   * @return {@link Interpolator1DPiecewisePoynomialDataBundle} 
   */
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y, final double leftCond, final double rightCond) {
    if (!(_baseMethod.getPrimaryMethod() instanceof CubicSplineInterpolator)) {
      throw new IllegalArgumentException("No degrees of freedom at endpoints for this interpolation method");
    }
    return new Interpolator1DPiecewisePoynomialDataBundle(new ArrayInterpolator1DDataBundle(x, y, true), this._baseMethod, leftCond, rightCond);
  }

  /**
   * Access interpolator
   * @return _baseMethod
   */
  public PiecewisePolynomialInterpolator getInterpolator() {
    return _baseMethod;
  }
}
