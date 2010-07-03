/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class DoubleQuadraticInterpolator1D extends Interpolator1D<Interpolator1DDataBundle, InterpolationResult> {

  @Override
  public InterpolationResult interpolate(final Interpolator1DDataBundle data, final Double value) {
    Validate.notNull(value, "value");
    Validate.notNull(data, "data bundle");
    checkValue(data, value);
    final int low = data.getLowerBoundIndex(value);
    final int high = low + 1;
    final int n = data.size() - 1;
    final double[] xData = data.getKeys();
    final double[] yData = data.getValues();
    if (low == 0) {
      final Coefficents coef = getCoeffcients(xData, yData, 1);
      final double x = value - xData[1];
      return new InterpolationResult(coef.evaluate(x));
    } else if (high == n) {
      final Coefficents coef = getCoeffcients(xData, yData, n - 1);
      final double x = value - xData[n - 1];
      return new InterpolationResult(coef.evaluate(x));
    } else if (low == n) {
      return new InterpolationResult(yData[n]);
    }
    final Coefficents coef1 = getCoeffcients(xData, yData, low);
    final Coefficents coef2 = getCoeffcients(xData, yData, high);
    final double w = (xData[high] - value) / (xData[high] - xData[low]);
    final double res = w * coef1.evaluate(value - xData[low]) + (1 - w) * coef2.evaluate(value - xData[high]);
    return new InterpolationResult(res);
  }

  private Coefficents getCoeffcients(final double[] x, final double[] y, final int index) {
    final double a = y[index];
    final double dx1 = x[index] - x[index - 1];
    final double dx2 = x[index + 1] - x[index];
    final double dy1 = y[index] - y[index - 1];
    final double dy2 = y[index + 1] - y[index];
    final double b = (dx1 * dy2 / dx2 + dx2 * dy1 / dx1) / (dx1 + dx2);
    final double c = (dy2 / dx2 - dy1 / dx1) / (dx1 + dx2);
    return new Coefficents(a, b, c);
  }

  private class Coefficents extends Function1D<Double, Double> {
    private final double _a;
    private final double _b;
    private final double _c;

    public Coefficents(final double a, final double b, final double c) {
      _a = a;
      _b = b;
      _c = c;
    }

    @Override
    public Double evaluate(final Double x) {
      return _a + _b * x + _c * x * x;
    }
  }

}
