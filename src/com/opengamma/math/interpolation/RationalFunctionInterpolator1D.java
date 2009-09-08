package com.opengamma.math.interpolation;

/**
 * 
 * @author emcleod
 * 
 */

public class RationalFunctionInterpolator1D extends Interpolator1D {
  private final int _degree;

  public RationalFunctionInterpolator1D(Double[] x, Double[] y, int degree) {
    super(x, y);
    _degree = degree;
  }

  @Override
  public InterpolationResult interpolate(double value) throws InterpolationException {
    double diff = Math.abs(value - _x[0]);
    if (Math.abs(diff) < EPS)
      return new InterpolationResult(_y[0], 0.0);
    double diff1;
    double[] c = new double[_degree + 1];
    double[] d = new double[_degree + 1];
    int ns = 0;
    for (int i = 0; i <= _degree; i++) {
      diff1 = Math.abs(value - _x[i]);
      if (diff < EPS) {
        return new InterpolationResult(_y[i], 0);
      } else if (diff1 < diff) {
        ns = i;
        diff = diff1;
      }
      c[i] = _y[i];
      d[i] = _y[i] + EPS;
    }
    double y = _y[ns--];
    double w, t, dd, dy = 0;
    for (int i = 1; i <= _degree; i++) {
      for (int j = 0; j < _degree - i; j++) {
        w = c[j + 1] - d[j];
        diff = _x[i + j] - value;
        t = (_x[j] - value) * d[j] / diff;
        dd = t - c[j + 1];
        if (Math.abs(dd) < EPS)
          throw new InterpolationException("Interpolating function has a pole at the x = " + value);
        dd = w / dd;
        d[j] = c[j + 1] * dd;
        c[j] = t * dd;
      }
      dy = (2 * (ns + 1) <= (_degree - i)) ? c[ns + 1] : d[ns--];
      y += dy;
    }
    return new InterpolationResult(y, dy);
  }
}
