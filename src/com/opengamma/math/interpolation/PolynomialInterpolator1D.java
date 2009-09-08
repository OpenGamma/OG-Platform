package com.opengamma.math.interpolation;

public class PolynomialInterpolator1D extends Interpolator1D {
  private final int _degree;

  public PolynomialInterpolator1D(Double[] x, Double[] y, int degree) {
    super(x, y);
    _degree = degree;
  }

  @Override
  public InterpolationResult interpolate(double value) throws InterpolationException {
    int lower = getLowerBound(value);
    double[] c = new double[_degree + 1];
    double[] d = new double[_degree + 1];
    if (lower + _degree >= _x.length)
      throw new InterpolationException("Lower bound of x (" + lower + ") is within " + _degree + " data points of end of series");
    int ns = Math.abs(value - _x[lower]) < Math.abs(value - _x[lower + 1]) ? 0 : 1;
    for (int i = 0; i <= _degree; i++) {
      c[i] = _y[i + lower];
      d[i] = c[i];
    }
    double y = _y[lower + ns--];
    double dy = 0;
    double diff = 0;
    for (int i = 1; i <= _degree; i++) {
      for (int j = 0, k = lower; j <= _degree - i; j++, k++) {
        if (Math.abs(_x[k] - _x[k + 1]) < EPS)
          throw new InterpolationException("Two values of x were equal: " + _x[k] + " " + _x[k + i]);
        diff = (c[j + 1] - d[j]) / (_x[k] - _x[k + i]);
        d[j] = (_x[k + i] - value) * diff;
        c[j] = (_x[k] - value) * diff;
      }
      dy = (2 * (ns + 1) <= (_degree - i)) ? c[ns + 1] : d[ns--];
      y += dy;
    }
    return new InterpolationResult(y, dy);
  }
}
