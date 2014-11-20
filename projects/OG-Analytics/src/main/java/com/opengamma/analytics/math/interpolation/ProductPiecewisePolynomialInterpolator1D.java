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
 * 
 */
public class ProductPiecewisePolynomialInterpolator1D extends Interpolator1D {

  private static final PiecewisePolynomialWithSensitivityFunction1D FUNC = new PiecewisePolynomialWithSensitivityFunction1D();
  private final ProductPiecewisePolynomialInterpolator _interp;
  private static final double SMALL = 1e-14;

  public ProductPiecewisePolynomialInterpolator1D(PiecewisePolynomialInterpolator baseInterpolator) {
    _interp = new ProductPiecewisePolynomialInterpolator(baseInterpolator);
  }

  public ProductPiecewisePolynomialInterpolator1D(PiecewisePolynomialInterpolator baseInterpolator,
      double[] xValuesClamped, double[] yValuesClamped) {
    _interp = new ProductPiecewisePolynomialInterpolator(baseInterpolator, xValuesClamped, yValuesClamped);
  }

  @Override
  public Double interpolate(Interpolator1DDataBundle data, Double value) {
    Validate.notNull(value, "value");
    Validate.notNull(data, "data bundle");
    DoubleMatrix1D res;
    //    if (_interp.isClamped()) {
      Validate.isTrue(data instanceof Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle);
      Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle polyData = (Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle) data;
      if (Math.abs(value) < SMALL) {
        return FUNC.differentiate(polyData.getPiecewisePolynomialResult(), value).getEntry(0);
      }
      res = FUNC.evaluate(polyData.getPiecewisePolynomialResult(), value);
    //    } else {
    //      Validate.isTrue(data instanceof Interpolator1DPiecewisePoynomialDataBundle);
    //      Interpolator1DPiecewisePoynomialDataBundle polyData = (Interpolator1DPiecewisePoynomialDataBundle) data;
    //      if (Math.abs(value) < SMALL) {
    //        return FUNC.differentiate(polyData.getPiecewisePolynomialResultsWithSensitivity(), value).getEntry(0);
    //      }
    //      res = FUNC.evaluate(polyData.getPiecewisePolynomialResultsWithSensitivity(), value);
    //    }
    return res.getEntry(0) / value;
  }

  @Override
  public double firstDerivative(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(value, "value");
    Validate.notNull(data, "data bundle");
    DoubleMatrix1D resValue;
    DoubleMatrix1D resDerivative;
    //    if (_interp.isClamped()) {
      Validate.isTrue(data instanceof Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle);
      Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle polyData = (Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle) data;
      if (Math.abs(value) < SMALL) {
        return FUNC.differentiateTwice(polyData.getPiecewisePolynomialResult(), value).getEntry(0);
      }
      resValue = FUNC.evaluate(polyData.getPiecewisePolynomialResult(), value);
      resDerivative = FUNC.differentiate(polyData.getPiecewisePolynomialResult(), value);
    //    } else {
    //      Validate.isTrue(data instanceof Interpolator1DPiecewisePoynomialDataBundle);
    //      Interpolator1DPiecewisePoynomialDataBundle polyData = (Interpolator1DPiecewisePoynomialDataBundle) data;
    //      if (Math.abs(value) < SMALL) {
    //        return FUNC.differentiateTwice(polyData.getPiecewisePolynomialResultsWithSensitivity(), value).getEntry(0);
    //      }
    //      resValue = FUNC.evaluate(polyData.getPiecewisePolynomialResultsWithSensitivity(), value);
    //      resDerivative = FUNC.differentiate(polyData.getPiecewisePolynomialResultsWithSensitivity(), value);
    //    }
    return resDerivative.getEntry(0) / value - resValue.getEntry(0) / value / value;
  }

  @Override
  public double[] getNodeSensitivitiesForValue(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(value, "value");
    Validate.notNull(data, "data bundle");
    int nData = data.size();
    double[] res = new double[nData];
    //    if (_interp.isClamped()) {
      Validate.isTrue(data instanceof Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle);
      Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle polyData = (Interpolator1DPiecewisePoynomialWithExtraKnotsDataBundle) data;
      double eps = polyData.getEps();
      double small = polyData.getSmall();
      if (Math.abs(value) < SMALL) {
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
    //    } else {
    //      Validate.isTrue(data instanceof Interpolator1DPiecewisePoynomialDataBundle);
    //      Interpolator1DPiecewisePoynomialDataBundle polyData = (Interpolator1DPiecewisePoynomialDataBundle) data;
    //      if (Math.abs(value) < SMALL) {
    //        return FUNC.differentiateNodeSensitivity(polyData.getPiecewisePolynomialResultsWithSensitivity(), value)
    //            .getData();
    //      }
    //      double[] resSense = FUNC.nodeSensitivity(polyData.getPiecewisePolynomialResultsWithSensitivity(), value)
    //          .getData();
    //      for (int i = 0; i < nData; ++i) {
    //        res[i] = resSense[i] / value;
    //      }
    //    }
    return res;
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
