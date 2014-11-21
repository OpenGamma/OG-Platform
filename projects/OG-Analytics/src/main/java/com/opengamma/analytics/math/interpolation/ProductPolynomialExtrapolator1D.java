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
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;

/**
 * Given a data set {xValues[i], yValues[i]}, extrapolate {x[i], x[i] * y[i]} by a polynomial function defined by ProductPiecewisePolynomialInterpolator1D, 
 * that is, use polynomial coefficients for the leftmost (rightmost) interval obtained in ProductPiecewisePolynomialInterpolator1D.
 */
public class ProductPolynomialExtrapolator1D extends Interpolator1D {
  private static final long serialVersionUID = 1L;
  private final Interpolator1D _interpolator;
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
    _interpolator = interpolator;
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

  @Override
  public Double interpolate(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(data, "data");
    Validate.notNull(value, "value");
    ArgumentChecker.isTrue(value < data.firstKey() || value > data.lastKey(), "value was within data range");
    DoubleMatrix1D res;
    Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle polyData = (Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle) data;
    if (Math.abs(value) < SMALL) {
      return getPolynomialFunction().differentiate(polyData.getPiecewisePolynomialResult(), value).getEntry(0);
    }
    res = getPolynomialFunction().evaluate(polyData.getPiecewisePolynomialResult(), value);
    return res.getEntry(0) / value;
  }

  @Override
  public double firstDerivative(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(data, "data");
    Validate.notNull(value, "value");
    ArgumentChecker.isTrue(value < data.firstKey() || value > data.lastKey(), "value was within data range");
    DoubleMatrix1D resValue;
    DoubleMatrix1D resDerivative;
    Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle polyData = (Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle) data;
    if (Math.abs(value) < SMALL) {
      return 0.5 * getPolynomialFunction().differentiateTwice(polyData.getPiecewisePolynomialResult(), value).getEntry(
          0);
    }
    resValue = getPolynomialFunction().evaluate(polyData.getPiecewisePolynomialResult(), value);
    resDerivative = getPolynomialFunction().differentiate(polyData.getPiecewisePolynomialResult(), value);
    return resDerivative.getEntry(0) / value - resValue.getEntry(0) / value / value;
  }

  @Override
  public double[] getNodeSensitivitiesForValue(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(data, "data");
    Validate.notNull(value, "value");
    ArgumentChecker.isTrue(value < data.firstKey() || value > data.lastKey(), "value was within data range");
    int nData = data.size();
    double[] res = new double[nData];
    Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle polyData = (Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle) data;
    double eps = polyData.getEps();
    double small = polyData.getSmall();
    if (Math.abs(value) < SMALL) {
      for (int i = 0; i < nData; ++i) {
        double den = Math.abs(polyData.getValues()[i]) < small ? eps : polyData.getValues()[i] * eps;
        double up = getPolynomialFunction().differentiate(polyData.getPiecewisePolynomialResultUp()[i], value)
            .getData()[0];
        double dw = getPolynomialFunction().differentiate(polyData.getPiecewisePolynomialResultDw()[i], value)
            .getData()[0];
        res[i] = 0.5 * (up - dw) / den;
      }
    } else {
      for (int i = 0; i < nData; ++i) {
        double den = Math.abs(polyData.getValues()[i]) < small ? eps : polyData.getValues()[i] * eps;
        double up = getPolynomialFunction().evaluate(polyData.getPiecewisePolynomialResultUp()[i], value).getData()[0];
        double dw = getPolynomialFunction().evaluate(polyData.getPiecewisePolynomialResultDw()[i], value).getData()[0];
        res[i] = 0.5 * (up - dw) / den / value;
      }
    }
    return res;
  }

  private PiecewisePolynomialFunction1D getPolynomialFunction() {
    return this._func;
  }
}
