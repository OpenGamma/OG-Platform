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
public class CubicSplineInterpolatorWithSensitivities1D extends
    Interpolator1DWithSensitivities<Interpolator1DCubicSplineWthSensitivitiesModel> {

  /**
   * @param interpolator
   */
  public CubicSplineInterpolatorWithSensitivities1D() {
    super((Interpolator1D) new NaturalCubicSplineInterpolator1D());
    // TODO Auto-generated constructor stub
  }

  /* (non-Javadoc)
   * @see com.opengamma.math.interpolation.Interpolator1DWithSensitivities#interpolate(com.opengamma.math.interpolation.Interpolator1DModel, java.lang.Double)
   */
  @Override
  public InterpolationResultWithSensitivities interpolate(Interpolator1DCubicSplineWthSensitivitiesModel model,
      Double value) {
    Validate.notNull(value, "Value to be interpolated must not be null");
    Validate.notNull(model, "Model must not be null");
    checkValue(model, value);
    final int low = model.getLowerBoundIndex(value);
    final int high = low + 1;
    final int n = model.size() - 1;
    final double[] xData = model.getKeys();
    final double[] yData = model.getValues();

    double[] sense = new double[n + 1];
    if (model.getLowerBoundIndex(value) == n) {
      sense[n] = 1.0;
      return new InterpolationResultWithSensitivities(yData[n], sense);
    }
    final double delta = xData[high] - xData[low];
    if (Math.abs(delta) < getEPS()) {
      throw new InterpolationException("x data points were not distinct");
    }
    final double a = (xData[high] - value) / delta;
    final double b = (value - xData[low]) / delta;
    final double c = a * (a * a - 1) * delta * delta / 6.0;
    final double d = b * (b * b - 1) * delta * delta / 6.0;

    final DoubleMatrix2D y2Sense = model.getSecondDerivativiesSensitivities();

    for (int i = 0; i <= n; i++) {
      sense[i] = c * y2Sense.getEntry(low, i) + d * y2Sense.getEntry(high, i);
    }
    sense[low] += a;
    sense[high] += b;

    return new InterpolationResultWithSensitivities(getUnderlyingInterpolator().interpolate(model, value).getResult(),
        sense);

  }

}
