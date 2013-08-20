/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.PiecewisePolynomialFunction1D;
import com.opengamma.analytics.math.function.PiecewisePolynomialWithSensitivityFunction1D;
import com.opengamma.analytics.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DPiecewisePoynomialDataBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ParallelArrayBinarySort;

/**
 * Piecewise Cubic Hermite Interpolating Polynomial (PCHIP) for use in yield curves. The yield curve r(t) is such that P(0,t) = exp(-t*r(t)) is the
 * discount factor for time t. Here we actually interpolate on the quantity f(t) = t*r(t) (which must be monotonically increasing) rather than r(t) itself.
 * However the inputs are still the set {t_i,r_i}, and the interpolate method returns r(t) rather than f(t). If t_0 != 0 an extra data point at zero is inserted
 * such that t_0 = 0 (the value of r_0 is irrelevant)
 *
 */
public class PCHIPYieldCurveInterpolator1D extends Interpolator1D {

  /** Serialization version */
  private static final long serialVersionUID = 1L;

  // TODO have options on method
  private static final PiecewisePolynomialFunction1D FUNC = new PiecewisePolynomialWithSensitivityFunction1D();

  @Override
  public Double interpolate(final Interpolator1DDataBundle data, final Double value) {
    final double eps = 1e-10;

    Validate.notNull(value, "value");
    Validate.notNull(data, "data bundle");
    Validate.isTrue(data instanceof Interpolator1DPiecewisePoynomialDataBundle);
    final Interpolator1DPiecewisePoynomialDataBundle polyData = (Interpolator1DPiecewisePoynomialDataBundle) data;

    ArgumentChecker.isFalse(value < 0, "value must be zero or positive");
    if (value == 0) {
      return 0.0;
    }

    // TODO direct evaluation using coefficients
    final double t = Math.max(eps, value); // Avoid divide by zero
    final DoubleMatrix1D res = FUNC.evaluate(polyData.getPiecewisePolynomialResultsWithSensitivity(), t);
    return res.getEntry(0) / t;
  }

  @Override
  public double firstDerivative(final Interpolator1DDataBundle data, final Double value) {
    final double eps = 1e-10;

    Validate.notNull(value, "value");
    Validate.notNull(data, "data bundle");
    Validate.isTrue(data instanceof Interpolator1DPiecewisePoynomialDataBundle);
    final Interpolator1DPiecewisePoynomialDataBundle polyData = (Interpolator1DPiecewisePoynomialDataBundle) data;

    ArgumentChecker.isFalse(value < 0, "value must be zero or positive");
    if (value == 0) {
      return 0.0;
    }

    final double t = Math.max(eps, value);
    final DoubleMatrix1D resValue = FUNC.evaluate(polyData.getPiecewisePolynomialResultsWithSensitivity(), t);
    final DoubleMatrix1D resDerivative = FUNC.differentiate(polyData.getPiecewisePolynomialResultsWithSensitivity(), t);
    return (resDerivative.getEntry(0) - resValue.getEntry(0) / t) / t;
  }

  @Override
  public double[] getNodeSensitivitiesForValue(final Interpolator1DDataBundle data, final Double value) {
    throw new NotImplementedException();
  }

  @Override
  public Interpolator1DDataBundle getDataBundle(final double[] x, final double[] y) {
    final int n = x.length;
    ArgumentChecker.isTrue(n == y.length, "x and y different lengths");
    final double[] xSrt = new double[n];
    final double[] ySrt = new double[n];
    System.arraycopy(x, 0, xSrt, 0, n);
    System.arraycopy(y, 0, ySrt, 0, n);
    ParallelArrayBinarySort.parallelBinarySort(xSrt, ySrt);

    return getDataBundleFromSortedArrays(xSrt, ySrt);
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    final int n = x.length;
    ArgumentChecker.isTrue(n == y.length, "x and y different lengths");
    ArgumentChecker.isTrue(n == y.length, "x and y different lengths");
    ArgumentChecker.isTrue(x[0] >= 0, "first x-values cannot be negative");

    double[] xx = x;

    double[] xy;
    if (xx[0] > 0) {
      final double[] temp = new double[n + 1];
      System.arraycopy(xx, 0, temp, 1, n);
      xx = temp;
      xy = new double[n + 1];
      for (int i = 1; i <= n; i++) {
        xy[i] = xx[i] * y[i - 1];
      }
    } else {
      xy = new double[n];
      for (int i = 1; i < n; i++) {
        xy[i] = x[i] * y[i];
      }
    }

    //    final PiecewisePolynomialResult poly = BASE.interpolate(xx, xy);
    return new Interpolator1DPiecewisePoynomialDataBundle(new ArrayInterpolator1DDataBundle(xx, xy, true), new PiecewiseCubicHermiteSplineInterpolatorWithSensitivity());
  }

}
