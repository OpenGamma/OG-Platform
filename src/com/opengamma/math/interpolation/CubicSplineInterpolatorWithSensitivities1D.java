/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import org.apache.commons.lang.Validate;

import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class CubicSplineInterpolatorWithSensitivities1D extends Interpolator1DWithSensitivities<Interpolator1DCubicSplineWithSensitivitiesDataBundle> {

  /**
   * @param interpolator
   */
  @SuppressWarnings("unchecked")
  public CubicSplineInterpolatorWithSensitivities1D() {
    super((Interpolator1D) new NaturalCubicSplineInterpolator1D());
  }

  @Override
  public InterpolationResultWithSensitivities interpolate(Interpolator1DCubicSplineWithSensitivitiesDataBundle model, Double value) {
    Validate.notNull(value, "Value to be interpolated must not be null");
    Validate.notNull(model, "Model must not be null");
    checkValue(model, value);
    final int low = model.getLowerBoundIndex(value);
    final int high = low + 1;
    final int n = model.size() - 1;
    final double[] xData = model.getKeys();
    final double[] yData = model.getValues();

    double[] sensitivity = new double[n + 1];
    if (model.getLowerBoundIndex(value) == n) {
      sensitivity[n] = 1.0;
      return new InterpolationResultWithSensitivities(yData[n], sensitivity);
    }
    final double delta = xData[high] - xData[low];
    if (Math.abs(delta) < getEPS()) {
      throw new InterpolationException("x data points were not distinct");
    }
    final double a = (xData[high] - value) / delta;
    final double b = (value - xData[low]) / delta;
    final double c = a * (a * a - 1) * delta * delta / 6.0;
    final double d = b * (b * b - 1) * delta * delta / 6.0;

    final DoubleMatrix2D y2Sensitivities = model.getSecondDerivativesSensitivities();

    for (int i = 0; i <= n; i++) {
      sensitivity[i] = c * y2Sensitivities.getEntry(low, i) + d * y2Sensitivities.getEntry(high, i);
    }
    sensitivity[low] += a;
    sensitivity[high] += b;

    return new InterpolationResultWithSensitivities(getUnderlyingInterpolator().interpolate(model, value).getResult(), sensitivity);

  }

}
