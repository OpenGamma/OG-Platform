/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.interpolation;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.interpolation.data.ArrayInterpolator1DDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DCubicSplineDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;

/**
 * 
 */
public class NaturalCubicSplineInterpolator1D extends Interpolator1D {
  private static final long serialVersionUID = 1L;
  private final double _eps;

  public NaturalCubicSplineInterpolator1D() {
    _eps = 1e-12;
  }

  public NaturalCubicSplineInterpolator1D(final double eps) {
    _eps = eps;
  }

  @Override
  public Double interpolate(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(value, "value");
    Validate.notNull(data, "data bundle");
    Validate.isTrue(data instanceof Interpolator1DCubicSplineDataBundle);
    Interpolator1DCubicSplineDataBundle splineData = (Interpolator1DCubicSplineDataBundle) data;
    final int low = data.getLowerBoundIndex(value);
    final int high = low + 1;
    final int n = data.size() - 1;
    final double[] xData = data.getKeys();
    final double[] yData = data.getValues();
    if (data.getLowerBoundIndex(value) == n) {
      return yData[n];
    }
    final double delta = xData[high] - xData[low];
    if (Math.abs(delta) < _eps) {
      throw new MathException("x data points were not distinct");
    }
    final double a = (xData[high] - value) / delta;
    final double b = (value - xData[low]) / delta;
    final double[] y2 = splineData.getSecondDerivatives();
    return a * yData[low] + b * yData[high] + (a * (a * a - 1) * y2[low] + b * (b * b - 1) * y2[high]) * delta * delta / 6.;
  }

  @Override
  public double firstDerivative(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(value, "value");
    Validate.notNull(data, "data bundle");
    Validate.isTrue(data instanceof Interpolator1DCubicSplineDataBundle);
    Interpolator1DCubicSplineDataBundle splineData = (Interpolator1DCubicSplineDataBundle) data;
    int low = data.getLowerBoundIndex(value);
    int high = low + 1;
    final int n = data.size() - 1;
    final double[] xData = data.getKeys();
    final double[] yData = data.getValues();
    if (low == n) {
      low = n - 1;
      high = n;
    }
    // if (data.getLowerBoundIndex(value) == n) {
    // return yData[n];
    // }
    final double delta = xData[high] - xData[low];
    if (Math.abs(delta) < _eps) {
      throw new MathException("x data points were not distinct");
    }
    final double a = (xData[high] - value) / delta;
    final double b = (value - xData[low]) / delta;
    final double[] y2 = splineData.getSecondDerivatives();
    return (yData[high] - yData[low]) / delta + ((-3. * a * a + 1.) * y2[low] + (3. * b * b - 1.) * y2[high]) * delta / 6.;
  }

  @Override
  public double[] getNodeSensitivitiesForValue(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(data, "data");
    Validate.isTrue(data instanceof Interpolator1DCubicSplineDataBundle);
    Interpolator1DCubicSplineDataBundle cubicData = (Interpolator1DCubicSplineDataBundle) data;
    final int n = cubicData.size();
    final double[] result = new double[n];
    if (cubicData.getLowerBoundIndex(value) == n - 1) {
      result[n - 1] = 1.0;
      return result;
    }
    final double[] xData = cubicData.getKeys();
    final int low = cubicData.getLowerBoundIndex(value);
    final int high = low + 1;
    final double delta = xData[high] - xData[low];
    final double a = (xData[high] - value) / delta;
    final double b = (value - xData[low]) / delta;
    final double c = a * (a * a - 1) * delta * delta / 6.;
    final double d = b * (b * b - 1) * delta * delta / 6.;
    final double[][] y2Sensitivities = cubicData.getSecondDerivativesSensitivities();
    for (int i = 0; i < n; i++) {
      result[i] = c * y2Sensitivities[low][i] + d * y2Sensitivities[high][i];
    }
    result[low] += a;
    result[high] += b;
    return result;
  }

  @Override
  public Interpolator1DDataBundle getDataBundle(final double[] x, final double[] y) {
    return new Interpolator1DCubicSplineDataBundle(new ArrayInterpolator1DDataBundle(x, y));
  }

  @Override
  public Interpolator1DDataBundle getDataBundleFromSortedArrays(final double[] x, final double[] y) {
    return new Interpolator1DCubicSplineDataBundle(new ArrayInterpolator1DDataBundle(x, y, true));
  }
}
