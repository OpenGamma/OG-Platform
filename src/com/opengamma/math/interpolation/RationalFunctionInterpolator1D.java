package com.opengamma.math.interpolation;

import java.util.Map;
import java.util.TreeMap;

/**
 * 
 * @author emcleod
 * 
 */

public class RationalFunctionInterpolator1D extends Interpolator1D {
  private final int _degree;

  public RationalFunctionInterpolator1D(int degree) {
    _degree = degree;
  }

  @Override
  public InterpolationResult<Double> interpolate(Map<Double, Double> data, Double value) throws InterpolationException {
    final TreeMap<Double, Double> sorted = initData(data);
    final Double[] xArray = sorted.keySet().toArray(new Double[0]);
    final Double[] yArray = sorted.values().toArray(new Double[0]);
    double diff = Math.abs(value - xArray[0]);
    if (Math.abs(diff) < EPS) {
      return new InterpolationResult<Double>(yArray[0], 0.0);
    }
    double diff1;
    final double[] c = new double[_degree + 1];
    final double[] d = new double[_degree + 1];
    int ns = 0;
    for (int i = 0; i <= _degree; i++) {
      diff1 = Math.abs(value - xArray[i]);
      if (diff < EPS) {
        return new InterpolationResult<Double>(yArray[i], 0.);
      } else if (diff1 < diff) {
        ns = i;
        diff = diff1;
      }
      c[i] = yArray[i];
      d[i] = yArray[i] + EPS;
    }
    double y = yArray[ns--];
    double w, t, dd, dy = 0;
    for (int i = 1; i <= _degree; i++) {
      for (int j = 0; j < _degree - i; j++) {
        w = c[j + 1] - d[j];
        diff = xArray[i + j] - value;
        t = (xArray[j] - value) * d[j] / diff;
        dd = t - c[j + 1];
        if (Math.abs(dd) < EPS) {
          throw new InterpolationException("Interpolating function has a pole at the x = " + value);
        }
        dd = w / dd;
        d[j] = c[j + 1] * dd;
        c[j] = t * dd;
      }
      dy = 2 * (ns + 1) <= _degree - i ? c[ns + 1] : d[ns--];
      y += dy;
    }
    return new InterpolationResult<Double>(y, dy);
  }
}
