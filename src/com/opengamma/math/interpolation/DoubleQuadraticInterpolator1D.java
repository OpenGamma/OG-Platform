/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import org.apache.commons.lang.Validate;

import com.opengamma.math.interpolation.Interpolator1DDoubleQuadraticDataBundle.Coefficents;

/**
 * 
 */
public class DoubleQuadraticInterpolator1D extends Interpolator1D<Interpolator1DDoubleQuadraticDataBundle, InterpolationResult> {

  @Override
  public InterpolationResult interpolate(final Interpolator1DDoubleQuadraticDataBundle data, final Double value) {
    Validate.notNull(value, "value");
    Validate.notNull(data, "data bundle");
    checkValue(data, value);
    final int low = data.getLowerBoundIndex(value);
    final int high = low + 1;
    final int n = data.size() - 1;
    final double[] xData = data.getKeys();
    final double[] yData = data.getValues();
    if (low == 0) {
      final Coefficents coef = data.getCoefficents(0);
      final double x = value - xData[1];
      return new InterpolationResult(coef.evaluate(x));
    } else if (high == n) {
      final Coefficents coef = data.getCoefficents(n - 2);
      final double x = value - xData[n - 1];
      return new InterpolationResult(coef.evaluate(x));
    } else if (low == n) {
      return new InterpolationResult(yData[n]);
    }
    final Coefficents coef1 = data.getCoefficents(low - 1);
    final Coefficents coef2 = data.getCoefficents(high - 1);
    final double w = (xData[high] - value) / (xData[high] - xData[low]);
    final double res = w * coef1.evaluate(value - xData[low]) + (1 - w) * coef2.evaluate(value - xData[high]);
    return new InterpolationResult(res);
  }

}
