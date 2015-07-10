/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.PiecewisePolynomialFunction1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Given a data set {xValues[i], yValues[i]}, extrapolate {x[i], x[i] * y[i]} by a polynomial function defined by ProductPiecewisePolynomialInterpolator1D, 
 * that is, use polynomial coefficients for the leftmost (rightmost) interval obtained in ProductPiecewisePolynomialInterpolator1D.
 */
public class ProductPolynomialExtrapolator1D extends Interpolator1D {
  private static final long serialVersionUID = 1L;
  private final ProductPiecewisePolynomialInterpolator1D _interpolator;
  private final PiecewisePolynomialFunction1D _func;
  private static final double SMALL = 1e-14;

  /**
   * The extrapolator using PiecewisePolynomialWithSensitivityFunction1D
   * @param interpolator The interpolator
   */
  public ProductPolynomialExtrapolator1D(Interpolator1D interpolator) {
    this(interpolator, new PiecewisePolynomialFunction1D());
  }

  /**
   * The extrapolator using a specific polynomial function
   * @param interpolator The interpolator
   * @param func The polynomial function
   */
  public ProductPolynomialExtrapolator1D(Interpolator1D interpolator, PiecewisePolynomialFunction1D func) {
    ArgumentChecker.notNull(interpolator, "interpolator");
    ArgumentChecker.notNull(func, "func");
    ArgumentChecker.isTrue(interpolator instanceof ProductPiecewisePolynomialInterpolator1D,
        "This interpolator should be used with ProductPiecewisePolynomialInterpolator1D");
    _interpolator = (ProductPiecewisePolynomialInterpolator1D) interpolator;
    _func = func;
  }

  @Override
  public Interpolator1DDataBundle getDataBundle(final double[] x, final double[] y) {
    return _interpolator.getDataBundle(x, y);
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    return _interpolator.getDataBundleFromSortedArrays(x, y);
  }

  /**
   * {@inheritDoc}
   * For small Math.abs(value), this method returns the exact value if clamped at (0,0), 
   * otherwise this returns a reference value
   */
  @Override
  public Double interpolate(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(data, "data");
    Validate.notNull(value, "value");
    ArgumentChecker.isTrue(value < data.firstKey() || value > data.lastKey(), "value was within data range");
    Validate.isTrue(data instanceof Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle);
    Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle polyData = (Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle) data;
    return _interpolator.interpolate(polyData, value, _func, SMALL);
  }

  /**
   * {@inheritDoc}
   * For small Math.abs(value), this method returns the exact value if clamped at (0,0), 
   * otherwise this returns a reference value
   */
  @Override
  public double firstDerivative(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(data, "data");
    Validate.notNull(value, "value");
    ArgumentChecker.isTrue(value < data.firstKey() || value > data.lastKey(), "value was within data range");
    Validate.isTrue(data instanceof Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle);
    Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle polyData = (Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle) data;
    return _interpolator.firstDerivative(polyData, value, _func, SMALL);
  }

  /**
   * {@inheritDoc}
   * For small Math.abs(value), this method returns the exact value if clamped at (0,0), 
   * otherwise this returns a reference value
   */
  @Override
  public double[] getNodeSensitivitiesForValue(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(data, "data");
    Validate.notNull(value, "value");
    ArgumentChecker.isTrue(value < data.firstKey() || value > data.lastKey(), "value was within data range");
    Validate.isTrue(data instanceof Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle);
    Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle polyData = (Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle) data;
    return _interpolator.getNodeSensitivitiesForValue(polyData, value, _func, SMALL);
  }

  @Override
  protected double[] getFiniteDifferenceSensitivities(final Interpolator1DDataBundle data, final Double value) {
    throw new IllegalArgumentException("Use the method, getNodeSensitivitiesForValue");
  }
}
