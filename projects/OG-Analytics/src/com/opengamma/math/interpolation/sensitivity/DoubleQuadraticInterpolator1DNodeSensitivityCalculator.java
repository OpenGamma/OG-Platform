/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation.sensitivity;

import org.apache.commons.lang.Validate;

import com.opengamma.math.interpolation.data.Interpolator1DDoubleQuadraticDataBundle;

/**
 * 
 */
public class DoubleQuadraticInterpolator1DNodeSensitivityCalculator implements Interpolator1DNodeSensitivityCalculator<Interpolator1DDoubleQuadraticDataBundle> {

  @Override
  public double[] calculate(final Interpolator1DDoubleQuadraticDataBundle data, final double value) {
    Validate.notNull(data, "data");
    final int low = data.getLowerBoundIndex(value);
    final int high = low + 1;
    final int n = data.size();
    final double[] xData = data.getKeys();
    final double[] result = new double[n];
    if (low == 0) {
      final double[] temp = getQuadraticSensitivities(xData, value, 1);
      result[0] = temp[0];
      result[1] = temp[1];
      result[2] = temp[2];
      return result;
    } else if (high == n - 1) {
      final double[] temp = getQuadraticSensitivities(xData, value, n - 2);
      result[n - 3] = temp[0];
      result[n - 2] = temp[1];
      result[n - 1] = temp[2];
      return result;
    } else if (high == n) {
      result[n - 1] = 1;
      return result;
    }
    final double[] temp1 = getQuadraticSensitivities(xData, value, low);
    final double[] temp2 = getQuadraticSensitivities(xData, value, high);
    final double w = (xData[high] - value) / (xData[high] - xData[low]);
    result[low - 1] = w * temp1[0];
    result[low] = w * temp1[1] + (1 - w) * temp2[0];
    result[high] = w * temp1[2] + (1 - w) * temp2[1];
    result[high + 1] = (1 - w) * temp2[2];
    return result;
  }

  private double[] getQuadraticSensitivities(final double[] xData, final double x, final int i) {
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
