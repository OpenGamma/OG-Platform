/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class DoubleQuadraticInterpolatorWithSensitivities1D extends Interpolator1DWithSensitivities<Interpolator1DDoubleQuadraticDataBundle> {

  /**
   * @param interpolator
   */
  public DoubleQuadraticInterpolatorWithSensitivities1D() {
    super(new DoubleQuadraticInterpolator1D());
  }

  @Override
  public InterpolationResultWithSensitivities interpolate(final Interpolator1DDoubleQuadraticDataBundle data, final Double value) {
    Validate.notNull(value, "Value to be interpolated must not be null");
    Validate.notNull(data, "Model must not be null");
    final int low = data.getLowerBoundIndex(value);
    final int high = low + 1;
    final int n = data.size() - 1;
    final double[] xData = data.getKeys();

    final double[] sensitivity = new double[n + 1];
    if (low == 0) {
      final double[] temp = getQuadraticSensitivies(xData, value, 1);
      sensitivity[0] = temp[0];
      sensitivity[1] = temp[1];
      sensitivity[2] = temp[2];
      return new InterpolationResultWithSensitivities(getUnderlyingInterpolator().interpolate(data, value).getResult(), sensitivity);
    } else if (high == n) {
      final double[] temp = getQuadraticSensitivies(xData, value, n - 1);
      sensitivity[n - 2] = temp[0];
      sensitivity[n - 1] = temp[1];
      sensitivity[n] = temp[2];
      return new InterpolationResultWithSensitivities(getUnderlyingInterpolator().interpolate(data, value).getResult(), sensitivity);
    } else if (low == n) {
      sensitivity[n] = 1.0;
      return new InterpolationResultWithSensitivities(data.getValues()[0], sensitivity);
    }

    final double[] temp1 = getQuadraticSensitivies(xData, value, low);
    final double[] temp2 = getQuadraticSensitivies(xData, value, high);
    final double w = (xData[high] - value) / (xData[high] - xData[low]);
    sensitivity[low - 1] = w * temp1[0];
    sensitivity[low] = w * temp1[1] + (1 - w) * temp2[0];
    sensitivity[high] = w * temp1[2] + (1 - w) * temp2[1];
    sensitivity[high + 1] = (1 - w) * temp2[2];

    return new InterpolationResultWithSensitivities(getUnderlyingInterpolator().interpolate(data, value).getResult(), sensitivity);

  }

  private double[] getQuadraticSensitivies(final double[] xData, final double x, final int i) {
    final double[] res = new double[3];
    final double deltaX = x - xData[i];
    final double h1 = xData[i] - xData[i - 1];
    final double h2 = xData[i + 1] - xData[i];
    res[0] = deltaX * (deltaX - h2) / h1 / (h1 + h2);
    res[1] = 1 + deltaX * (h2 - h1 - deltaX) / h1 / h2;
    res[2] = deltaX * (h1 + deltaX) / (h1 + h2) / h2;
    return res;
  }

}
