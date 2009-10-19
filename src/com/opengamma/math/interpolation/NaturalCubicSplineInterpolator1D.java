package com.opengamma.math.interpolation;

import java.util.Map;
import java.util.TreeMap;

public class NaturalCubicSplineInterpolator1D extends Interpolator1D {

  @Override
  public InterpolationResult<Double> interpolate(final Map<Double, Double> data, final Double value) {
    final TreeMap<Double, Double> sorted = initData(data);
    final int kLow = getLowerBoundIndex(sorted, value);
    final int kHigh = kLow + 1;
    final Double[] xData = new Double[data.size()];
    final Double[] yData = new Double[data.size()];
    int i = 0;
    for (final Map.Entry<Double, Double> entry : sorted.entrySet()) {
      xData[i] = entry.getKey();
      yData[i++] = entry.getValue();
    }
    final double y;
    double h;
    final double b, a;
    h = xData[kHigh] - xData[kLow];
    if (h == 0.)
      throw new InterpolationException("x data points were not distince");
    a = (xData[kHigh] - value) / h;
    b = (value - xData[kLow]) / h;
    final double[] y2 = getSecondDerivative(xData, yData);
    y = a * yData[kLow] + b * yData[kHigh] + ((a * a * a - a) * y2[kLow] + (b * b * b - b) * y2[kHigh]) * h * h / 6.;
    return new InterpolationResult<Double>(y);
  }

  private double[] getSecondDerivative(final Double[] x, final Double[] y) {
    final int n = x.length - 1;
    final double[] y2 = new double[n];
    double p, sig;
    final double[] u = new double[n - 1];
    y2[0] = -0.5;
    u[0] = 3. * (y[1] - y[0]) / (x[1] - x[0]);
    for (int i = 1; i < n - 1; i++) {
      sig = (x[i] - x[i - 1]) / (x[i + 1] - x[i - 1]);
      p = sig * y2[i - 1] + 2.;
      y2[i] = (sig - 1) / p;
      u[i] = (y[i + 1] - y[i]) / (x[i + 1] - x[i]) - (y[i] - y[i - 1]) / (x[i] - x[i - 1]);
      u[i] = (6 * u[i] / (x[i + 1] - x[i - 1]) - sig * u[i - 1]) / p;
    }
    final double qn = 0.5;
    final double un = 3. / (x[n - 1] - x[n - 2]) * -(y[n - 1] - y[n - 2]) / (x[n - 1] - x[n - 2]);
    y2[n - 1] = (un - qn * u[n - 2]) / (qn * y2[n - 2] + 1.);
    for (int j = n - 2; j >= 0; j--) {
      y2[j] = y2[j] * y2[j + 1] + u[j];
    }
    return y2;
  }
}
