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
public class DoubleQuadraticInterpolator1D extends Interpolator1D<Interpolator1DDataBundle, InterpolationResult> {

  @Override
  public InterpolationResult interpolate(Interpolator1DDataBundle data, Double value) {

    Validate.notNull(value, "value");
    Validate.notNull(data, "data bundle");
    checkValue(data, value);
    final int low = data.getLowerBoundIndex(value);
    final int high = low + 1;
    final int n = data.size() - 1;
    final double[] xData = data.getKeys();
    final double[] yData = data.getValues();

    if (low == 0) {
      Coefficents coef = getCoeffcients(xData, yData, 1);
      double x = value - xData[1];
      return new InterpolationResult(coef.calculateValue(x));
    } else if (high == n) {
      Coefficents coef = getCoeffcients(xData, yData, n - 1);
      double x = value - xData[n - 1];
      return new InterpolationResult(coef.calculateValue(x));
    } else if (low == n) {
      return new InterpolationResult(yData[n]);
    }

    Coefficents coef1 = getCoeffcients(xData, yData, low);
    Coefficents coef2 = getCoeffcients(xData, yData, high);
    double w = (xData[high] - value) / (xData[high] - xData[low]);

    double res = w * coef1.calculateValue(value - xData[low]) + (1 - w) * coef2.calculateValue(value - xData[high]);
    return new InterpolationResult(res);
  }

  private Coefficents getCoeffcients(double[] x, double[] y, int index) {
    double a = y[index];
    double dx1 = x[index] - x[index - 1];
    double dx2 = x[index + 1] - x[index];
    double dy1 = y[index] - y[index - 1];
    double dy2 = y[index + 1] - y[index];
    double b = (dx1 * dy2 / dx2 + dx2 * dy1 / dx1) / (dx1 + dx2);
    double c = (dy2 / dx2 - dy1 / dx1) / (dx1 + dx2);
    return new Coefficents(a, b, c);
  }

  private class Coefficents {
    private double _a;
    private double _b;
    private double _c;

    public Coefficents(double a, double b, double c) {
      _a = a;
      _b = b;
      _c = c;
    }

    public double calculateValue(double x) {
      return _a + _b * x + _c * x * x;
    }

    public double getA() {
      return _a;
    }

    public double getB() {
      return _b;
    }

    public double getC() {
      return _c;
    }

    public void setA(double a) {
      _a = a;
    }

    public void setB(double b) {
      _b = b;
    }

    public void setC(double c) {
      _c = c;
    }
  }

}
