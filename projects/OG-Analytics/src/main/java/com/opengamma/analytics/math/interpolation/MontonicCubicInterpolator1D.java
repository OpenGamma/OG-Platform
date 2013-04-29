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
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDoubleQuadraticDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DPiecewisePoynomialDataBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;

/**
 * 
 */
public class MontonicCubicInterpolator1D extends Interpolator1D {

  // TODO have options on method
  private static final MonotonicityPreservingCubicSplineInterpolator BASE = new MonotonicityPreservingCubicSplineInterpolator(new PiecewiseCubicHermiteSplineInterpolator());
  private static final PiecewisePolynomialFunction1D FUNC = new PiecewisePolynomialFunction1D();

  @Override
  public Double interpolate(Interpolator1DDataBundle data, Double value) {
    Validate.notNull(value, "value");
    Validate.notNull(data, "data bundle");
    Validate.isTrue(data instanceof Interpolator1DPiecewisePoynomialDataBundle);
    final Interpolator1DPiecewisePoynomialDataBundle polyData = (Interpolator1DPiecewisePoynomialDataBundle) data;
    final DoubleMatrix1D res = FUNC.evaluate(polyData.getPiecewisePolynomialResult(), value);
    return res.getEntry(0);
  }

  @Override
  public double[] getNodeSensitivitiesForValue(Interpolator1DDataBundle data, Double value) {
    throw new NotImplementedException();
  }

  @Override
  public Interpolator1DDataBundle getDataBundle(double[] x, double[] y) {
    PiecewisePolynomialResult poly = BASE.interpolate(x, y);
    return new Interpolator1DPiecewisePoynomialDataBundle(poly);
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(double[] x, double[] y) {
    PiecewisePolynomialResult poly = BASE.interpolate(x, y);
    return new Interpolator1DPiecewisePoynomialDataBundle(poly);
  }

}
