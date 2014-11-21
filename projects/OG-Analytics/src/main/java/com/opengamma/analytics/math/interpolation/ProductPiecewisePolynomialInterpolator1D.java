/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.PiecewisePolynomialWithSensitivityFunction1D;
import com.opengamma.analytics.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;

/**
 * Wrapping {@link ProductPiecewisePolynomialInterpolator}
 */
public class ProductPiecewisePolynomialInterpolator1D extends Interpolator1D {
  private static final long serialVersionUID = 1L;
  private static final PiecewisePolynomialWithSensitivityFunction1D FUNC = new PiecewisePolynomialWithSensitivityFunction1D();
  private final ProductPiecewisePolynomialInterpolator _interp;
  private static final double SMALL = 1e-14;

  /**
   * Construct {@link ProductPiecewisePolynomialInterpolator}
   * @param baseInterpolator The base interpolator
   */
  public ProductPiecewisePolynomialInterpolator1D(PiecewisePolynomialInterpolator baseInterpolator) {
    _interp = new ProductPiecewisePolynomialInterpolator(baseInterpolator);
  }

  /**
   * Construct {@link ProductPiecewisePolynomialInterpolator}
   * @param baseInterpolator The base interpolator
   * @param xValuesClamped X values of the clamped points
   * @param yValuesClamped Y values of the clamped points
   */
  public ProductPiecewisePolynomialInterpolator1D(PiecewisePolynomialInterpolator baseInterpolator,
      double[] xValuesClamped, double[] yValuesClamped) {
    _interp = new ProductPiecewisePolynomialInterpolator(baseInterpolator, xValuesClamped, yValuesClamped);
  }

  @Override
  public Double interpolate(Interpolator1DDataBundle data, Double value) {
    Validate.notNull(value, "value");
    Validate.notNull(data, "data bundle");
    Validate.isTrue(data instanceof Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle);
    Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle polyData = (Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle) data;
    if (Math.abs(value) < SMALL) { // this is exact if clamped at (0,0), otherwise this returns a reference value
      return FUNC.differentiate(polyData.getPiecewisePolynomialResult(), value).getEntry(0);
    }
    DoubleMatrix1D res = FUNC.evaluate(polyData.getPiecewisePolynomialResult(), value);
    return res.getEntry(0) / value;
  }

  @Override
  public double firstDerivative(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(value, "value");
    Validate.notNull(data, "data bundle");
    Validate.isTrue(data instanceof Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle);
    Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle polyData = (Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle) data;
    if (Math.abs(value) < SMALL) { // this is exact if clamped at (0,0), otherwise this returns a reference value
      return 0.5 * FUNC.differentiateTwice(polyData.getPiecewisePolynomialResult(), value).getEntry(0);
    }
    DoubleMatrix1D resValue = FUNC.evaluate(polyData.getPiecewisePolynomialResult(), value);
    DoubleMatrix1D resDerivative = FUNC.differentiate(polyData.getPiecewisePolynomialResult(), value);
    return resDerivative.getEntry(0) / value - resValue.getEntry(0) / value / value;
  }

  @Override
  public double[] getNodeSensitivitiesForValue(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(value, "value");
    Validate.notNull(data, "data bundle");
    int nData = data.size();
    double[] res = new double[nData];
    Validate.isTrue(data instanceof Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle);
    Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle polyData = (Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle) data;
    double eps = polyData.getEps();
    double small = polyData.getSmall();
    if (Math.abs(value) < SMALL) { // this is exact if clamped at (0,0), otherwise this returns a reference value
      for (int i = 0; i < nData; ++i) {
        double den = Math.abs(polyData.getValues()[i]) < small ? eps : polyData.getValues()[i] * eps;
        double up = FUNC.differentiate(polyData.getPiecewisePolynomialResultUp()[i], value).getData()[0];
        double dw = FUNC.differentiate(polyData.getPiecewisePolynomialResultDw()[i], value).getData()[0];
        res[i] = 0.5 * (up - dw) / den;
      }
    } else {
      for (int i = 0; i < nData; ++i) {
        double den = Math.abs(polyData.getValues()[i]) < small ? eps : polyData.getValues()[i] * eps;
        double up = FUNC.evaluate(polyData.getPiecewisePolynomialResultUp()[i], value).getData()[0];
        double dw = FUNC.evaluate(polyData.getPiecewisePolynomialResultDw()[i], value).getData()[0];
        res[i] = 0.5 * (up - dw) / den / value;
      }
    }
    return res;
  }

  @Override
  protected double[] getFiniteDifferenceSensitivities(final Interpolator1DDataBundle data, final Double value) {
    throw new IllegalArgumentException("Use the method, getNodeSensitivitiesForValue");
  }

  @Override
  public Interpolator1DDataBundle getDataBundle(final double[] x, final double[] y) {
    return new Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle(new ArrayInterpolator1DDataBundle(x, y, false),
        _interp);
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    return new Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle(new ArrayInterpolator1DDataBundle(x, y, true),
        _interp);
  }

}
