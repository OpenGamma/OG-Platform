/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.PiecewisePolynomialFunction1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DPiecewisePoynomialDataBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;

/**
 * Piecewise Cubic Hermite Interpolating Polynomial (PCHIP)
 */
public class PCHIPYieldCurveInterpolator1D extends Interpolator1D {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  // TODO have options on method
  private static final PiecewisePolynomialInterpolator BASE = new PiecewiseCubicHermiteSplineInterpolator();
  private static final PiecewisePolynomialFunction1D FUNC = new PiecewisePolynomialFunction1D();

  @Override
  public Double interpolate(final Interpolator1DDataBundle data, final Double value) {
    final double eps = 1e-5;

    Validate.notNull(value, "value");
    Validate.notNull(data, "data bundle");
    Validate.isTrue(data instanceof Interpolator1DPiecewisePoynomialDataBundle);
    final Interpolator1DPiecewisePoynomialDataBundle polyData = (Interpolator1DPiecewisePoynomialDataBundle) data;

    final double t = Math.max(eps, value); //Avoid divide by zero
    final DoubleMatrix1D res = FUNC.evaluate(polyData.getPiecewisePolynomialResult(), t);
    return res.getEntry(0) / t;
  }

  @Override
  public double[] getNodeSensitivitiesForValue(final Interpolator1DDataBundle data, final Double value) {
    throw new NotImplementedException();
  }

  @Override
  public Interpolator1DDataBundle getDataBundle(final double[] x, final double[] y) {
    final int n = x.length;
    ArgumentChecker.isTrue(n == y.length, "x and y different lengths");
    double[] xy = new double[n];
    for (int i = 0; i < n; i++) {
      xy[i] = x[i] * y[i];
    }

    final PiecewisePolynomialResult poly = BASE.interpolate(x, xy);
    return new Interpolator1DPiecewisePoynomialDataBundle(poly);
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    final int n = x.length;
    ArgumentChecker.isTrue(n == y.length, "x and y different lengths");
    double[] xy = new double[n];
    for (int i = 0; i < n; i++) {
      xy[i] = x[i] * y[i];
    }
    final PiecewisePolynomialResult poly = BASE.interpolate(x, xy);
    return new Interpolator1DPiecewisePoynomialDataBundle(poly);
  }

}
