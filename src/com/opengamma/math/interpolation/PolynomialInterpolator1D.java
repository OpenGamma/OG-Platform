package com.opengamma.math.interpolation;

import java.util.Map;
import java.util.TreeMap;

public class PolynomialInterpolator1D extends Interpolator1D {
  private final int _degree;

  public PolynomialInterpolator1D(int degree) {
    _degree = degree;
  }

  @Override
  public InterpolationResult<Double> interpolate(Map<Double, Double> data, Double value) throws InterpolationException {
    TreeMap<Double, Double> sorted = initData(data);
    int lower = getLowerBoundIndex(sorted, value);
    Double[] xArray = sorted.keySet().toArray(new Double[0]);
    Double[] yArray = sorted.values().toArray(new Double[0]);
    double[] c = new double[_degree + 1];
    double[] d = new double[_degree + 1];
    if (lower + _degree >= data.size())
      throw new InterpolationException("Lower bound of x (" + lower + ") is within " + _degree + " data points of end of series");
    int ns = Math.abs(value - xArray[lower]) < Math.abs(value - xArray[lower + 1]) ? 0 : 1;
    for (int i = 0; i <= _degree; i++) {
      c[i] = yArray[i + lower];
      d[i] = c[i];
    }
    double y = yArray[lower + ns--];
    double dy = 0;
    double diff = 0;
    for (int i = 1; i <= _degree; i++) {
      for (int j = 0, k = lower; j <= _degree - i; j++, k++) {
        if (Math.abs(xArray[k] - xArray[k + 1]) < EPS)
          throw new InterpolationException("Two values of x were equal: " + xArray[k] + " " + xArray[k + i]);
        diff = (c[j + 1] - d[j]) / (xArray[k] - xArray[k + i]);
        d[j] = (xArray[k + i] - value) * diff;
        c[j] = (xArray[k] - value) * diff;
      }
      dy = (2 * (ns + 1) <= (_degree - i)) ? c[ns + 1] : d[ns--];
      y += dy;
    }
    return new InterpolationResult<Double>(y, dy);
  }
}
