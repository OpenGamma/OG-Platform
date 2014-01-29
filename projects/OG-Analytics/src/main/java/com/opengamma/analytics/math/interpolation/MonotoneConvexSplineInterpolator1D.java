/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.PiecewisePolynomialFunction1D;
import com.opengamma.analytics.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;

/**
 * 
 */
public class MonotoneConvexSplineInterpolator1D extends PiecewisePolynomialInterpolator1D {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  private static final PiecewisePolynomialFunction1D FUNC = new PiecewisePolynomialFunction1D();
  private static final MonotoneConvexSplineInterpolator BASE_METHOD = new MonotoneConvexSplineInterpolator();

  /**
   * Default constructor where the interpolation method is fixed
   */
  public MonotoneConvexSplineInterpolator1D() {
    super(BASE_METHOD);
  }

  @Override
  public Double interpolate(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(value, "value");
    Validate.notNull(data, "data bundle");
    Validate.isTrue(data instanceof Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle);
    final Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle polyData = (Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle) data;
    final DoubleMatrix1D res = FUNC.evaluate(polyData.getPiecewisePolynomialResult(), value);
    return res.getEntry(0);
  }

  @Override
  public double firstDerivative(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(value, "value");
    Validate.notNull(data, "data bundle");
    Validate.isTrue(data instanceof Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle);
    final Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle polyData = (Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle) data;
    final DoubleMatrix1D res = FUNC.differentiate(polyData.getPiecewisePolynomialResult(), value);
    return res.getEntry(0);
  }

  @Override
  public double[] getNodeSensitivitiesForValue(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(value, "value");
    Validate.notNull(data, "data bundle");
    Validate.isTrue(data instanceof Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle);
    final Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle polyData = (Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle) data;
    final int nData = polyData.size();
    final double[] res = new double[nData];

    final double eps = polyData.getEps();
    final double small = polyData.getSmall();
    for (int i = 0; i < nData; ++i) {
      final double den = Math.abs(polyData.getValues()[i]) < small ? eps : polyData.getValues()[i] * eps;
      final double up = FUNC.evaluate(polyData.getPiecewisePolynomialResultUp()[i], value).getData()[0];
      final double dw = FUNC.evaluate(polyData.getPiecewisePolynomialResultDw()[i], value).getData()[0];
      res[i] = 0.5 * (up - dw) / den;
    }
    return res;
  }

  @Override
  public Interpolator1DDataBundle getDataBundle(final double[] x, final double[] y) {
    return new Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle(new ArrayInterpolator1DDataBundle(x, y, false), BASE_METHOD);
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    return new Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle(new ArrayInterpolator1DDataBundle(x, y, true), BASE_METHOD);
  }
}
