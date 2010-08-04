/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.sensitivity;

import org.apache.commons.lang.Validate;

import com.opengamma.math.interpolation.data.Interpolator1DCubicSplineWithSensitivitiesDataBundle;
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class NaturalCubicSplineInterpolator1DNodeSensitivityCalculator implements Interpolator1DNodeSensitivityCalculator<Interpolator1DCubicSplineWithSensitivitiesDataBundle> {

  @Override
  public double[] calculate(final Interpolator1DCubicSplineWithSensitivitiesDataBundle data, final double value) {
    Validate.notNull(data, "data");
    final int n = data.size();
    final double[] result = new double[n];
    if (data.getLowerBoundIndex(value) == n - 1) {
      result[n - 1] = 1.0;
      return result;
    }
    final double[] xData = data.getKeys();
    final int low = data.getLowerBoundIndex(value);
    final int high = low + 1;
    final double delta = xData[high] - xData[low];
    final double a = (xData[high] - value) / delta;
    final double b = (value - xData[low]) / delta;
    final double c = a * (a * a - 1) * delta * delta / 6.;
    final double d = b * (b * b - 1) * delta * delta / 6.;
    final DoubleMatrix2D y2Sensitivities = data.getSecondDerivativesSensitivities();
    for (int i = 0; i < n; i++) {
      result[i] = c * y2Sensitivities.getEntry(low, i) + d * y2Sensitivities.getEntry(high, i);
    }
    result[low] += a;
    result[high] += b;
    return result;
  }

}
