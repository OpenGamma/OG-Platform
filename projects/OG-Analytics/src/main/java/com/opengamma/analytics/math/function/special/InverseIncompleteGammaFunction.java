/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.function.special;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.function.Function2D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
//TODO either find another implementation or delete this class
public class InverseIncompleteGammaFunction extends Function2D<Double, Double> {
  private final Function1D<Double, Double> _lnGamma = new NaturalLogGammaFunction();
  private static final double EPS = 1e-8;

  @Override
  public Double evaluate(final Double a, final Double p) {
    ArgumentChecker.notNegativeOrZero(a, "a");
    if (!ArgumentChecker.isInRangeExclusive(0, 1, p)) {
      throw new IllegalArgumentException("p must lie between 0 and 1: have " + p);
    }
    double x;
    double err;
    double t;
    double u;
    double pp, lna1 = 0, afac = 0;
    final double a1 = a - 1;
    final Function1D<Double, Double> gammaIncomplete = new IncompleteGammaFunction(a);
    final double gln = _lnGamma.evaluate(a);
    if (a > 1) {
      lna1 = Math.log(a1);
      afac = Math.exp(a1 * (lna1 - 1) - gln);
      pp = p < 0.5 ? p : 1 - p;
      t = Math.sqrt(-2 * Math.log(pp));
      x = (2.30753 + t * 0.27061) / (1 + t * (0.99229 + t * 0.04481)) - t;
      if (p < 0.5) {
        x = -x;
      }
      x = Math.max(0.001, a * Math.pow(1 - 1. / (9 * a) - x / (3 * Math.sqrt(a)), 3));
    } else {
      t = 1 - a * (0.253 + a * 0.12);
      if (p < t) {
        x = Math.pow(p / t, 1. / a);
      } else {
        x = 1. - Math.log(1 - (p - t) / (1. - t));
      }
    }
    for (int i = 0; i < 12; i++) {
      if (x <= 0) {
        return 0.;
      }
      err = gammaIncomplete.evaluate(x) - p;
      if (a > 1) {
        t = afac * Math.exp(-(x - a1) + a1 * (Math.log(x) - lna1));
      } else {
        t = Math.exp(-x + a1 * Math.log(x) - gln);
      }
      u = err / t;
      t = u / (1 - 0.5 * Math.min(1, u * ((a - 1) / x - 1)));
      x -= t;
      if (x <= 0) {
        x = 0.05 * (x + t);
      }
      if (Math.abs(t) < EPS * x) {
        break;
      }
    }
    return x;
  }

}
