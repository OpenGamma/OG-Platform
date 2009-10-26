/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.function.special;

import com.opengamma.math.MathException;
import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 */
public class InverseIncompleteBetaFunction extends Function1D<Double, Double> {
  private final double _a;
  private final double _b;
  private final Function1D<Double, Double> _lnGamma = new NaturalLogGammaFunction();
  private final Function1D<Double, Double> _beta;
  private final double EPS = 1e-9;

  public InverseIncompleteBetaFunction(final double a, final double b) {
    if (a <= 0)
      throw new IllegalArgumentException("a must be positive");
    if (b <= 0)
      throw new IllegalArgumentException("b must be positive");
    _a = a;
    _b = b;
    _beta = new IncompleteBetaFunction(a, b);
  }

  @Override
  public Double evaluate(final Double p) {
    if (p < 0 || p > 1)
      throw new IllegalArgumentException("x must lie in the range 0 to 1");
    double pp, x, t, a1 = _a - 1;
    final double b1 = _b - 1;
    double h, w, lna, lnb, u;
    if (_a >= 1 && _b >= 1) {
      pp = p < 0.5 ? p : 1 - p;
      t = Math.sqrt(-2 * Math.log(pp));
      x = (2.30753 + t * 0.27061) / (1 + t * (0.99229 + t * 0.04481)) - t;
      if (x < 0.5) {
        x *= -1;
      }
      a1 = (Math.sqrt(x) - 3.) / 6.;
      h = 2. / (1. / (2 * _a - 1) + 1. / (2 * _b - 1));
      w = x * Math.sqrt(a1 + h) / h - (1. / (2 * _b - 1) - 1. / (2 * _a - 1)) * (a1 + 5. / 6 - 2. / (3 * h));
      x = _a / (_a + _b + Math.exp(2 * w));
    } else {
      lna = Math.log(_a / (_a + _b));
      lnb = Math.log(_b / (_a + _b));
      t = Math.exp(_a * lna) / _a;
      u = Math.exp(_b * lnb) / _b;
      w = t + u;
      if (p < t / w) {
        x = Math.pow(_a * w * p, 1. / _a);
      } else {
        x = 1 - Math.pow(_b * w * (1 - p), 1. / _b);
      }
    }
    final double afac = -_lnGamma.evaluate(_a) - _lnGamma.evaluate(_b) + _lnGamma.evaluate(_a + _b);
    double err;
    for (int j = 0; j < 10; j++) {
      if (x == 0 || x == 1)
        throw new MathException("a or b too small for accurate evaluation");
      err = _beta.evaluate(x) - p;
      t = Math.exp(a1 * Math.log(x) + b1 * Math.log(1 - x) + afac);
      u = err / t;
      t = u / (1 - 0.5 * Math.min(1, u * (a1 / x - b1 / (1 - x))));
      x -= t;
      if (x <= 0) {
        x = 0.5 * (x + t);
      }
      if (x >= 1) {
        x = 0.5 * (x + t + 1);
      }
      if (Math.abs(t) < EPS * x && j > 0) {
        break;
      }
    }
    return x;
  }
}
