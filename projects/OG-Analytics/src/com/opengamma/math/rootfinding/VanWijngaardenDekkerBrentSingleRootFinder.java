/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import com.opengamma.math.MathException;
import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class VanWijngaardenDekkerBrentSingleRootFinder extends RealSingleRootFinder {
  private final double _accuracy;
  private static final int MAX_ITER = 100;
  private static final double ZERO = 1e-16;

  public VanWijngaardenDekkerBrentSingleRootFinder() {
    _accuracy = 1e-9;
  }

  public VanWijngaardenDekkerBrentSingleRootFinder(final double accuracy) {
    _accuracy = accuracy;
  }

  @Override
  public Double getRoot(final Function1D<Double, Double> function, final Double x1, final Double x2) {
    checkInputs(function, x1, x2);
    double a = x1, b = x2, c = x2, d = 0, e = 0;
    double fa = function.evaluate(a);
    double fb = function.evaluate(b);
    if (fa > 0 && fb > 0 || fa < 0 && fb < 0) {
      throw new MathException("Root was not bracketed by " + x1 + " and " + x2);
    }
    double fc = fb;
    double p, q, r, s, eps, xMid, min1, min2;
    for (int i = 0; i < MAX_ITER; i++) {
      if (fb > 0 && fc > 0 || fb < 0 && fc < 0) {
        c = a;
        fc = fa;
        d = b - a;
        e = d;
      }
      if (Math.abs(fc) < Math.abs(fb)) {
        a = b;
        b = c;
        c = a;
        fa = fb;
        fb = fc;
        fc = fa;
      }
      eps = 2 * ZERO * Math.abs(b) + 0.5 * _accuracy;
      xMid = 0.5 * (c - b);
      if (Math.abs(xMid) <= eps || Math.abs(fb) <= ZERO) {
        return b;
      }
      if (Math.abs(e) >= eps && Math.abs(fa) > Math.abs(fb)) {
        s = fb / fa;
        if (Math.abs(a - c) < ZERO) {
          p = 2 * xMid * s;
          q = 1 - s;
        } else {
          q = fa / fc;
          r = fb / fc;
          p = s * (2 * xMid * q * (q - r) - (b - a) * (r - 1));
          q = (q - 1) * (r - 1) * (s - 1);
        }
        if (p > 0) {
          q *= -1;
        }
        p = Math.abs(p);
        min1 = 3 * xMid * q - Math.abs(eps * q);
        min2 = Math.abs(e * q);
        if (2 * p < (min1 < min2 ? min1 : min2)) {
          e = d;
          d = p / q;
        } else {
          d = xMid;
          e = d;
        }
      } else {
        d = xMid;
        e = d;
      }
      a = b;
      fa = fb;
      if (Math.abs(d) > eps) {
        b += d;
      } else {
        b += Math.copySign(eps, xMid);
        fb = function.evaluate(b);
      }
      fa = function.evaluate(a);
      fb = function.evaluate(b);
      fc = function.evaluate(c);
    }
    throw new MathException("Could not converge to root in " + MAX_ITER + " attempts");
  }
}
