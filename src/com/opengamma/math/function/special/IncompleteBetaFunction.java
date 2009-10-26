/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.function.special;

import com.opengamma.math.function.Function1D;

/**
 * 
 * @author emcleod
 */
public class IncompleteBetaFunction extends Function1D<Double, Double> {
  private final double _a;
  private final double _b;
  private final double _testValue;
  private final Function1D<Double, Double> _betaFunction;
  private final double EPS = 1e-12;

  public IncompleteBetaFunction(final double a, final double b) {
    if (a <= 0)
      throw new IllegalArgumentException("a must be positive");
    if (b <= 0)
      throw new IllegalArgumentException("b must be positive");
    _a = a;
    _b = b;
    _testValue = (a + 1) / (2 + a + b);
    final Function1D<Double, Double> lnGamma = new NaturalLogGammaFunction();
    final double lnAB = lnGamma.evaluate(a + b);
    final double lnA = lnGamma.evaluate(a);
    final double lnB = lnGamma.evaluate(b);
    _betaFunction = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        return Math.exp(lnAB - lnA - lnB + a * Math.log(x) + b * Math.log(1 - x));
      }

    };
  }

  @Override
  public Double evaluate(final Double x) {
    if (x < 0 || x > 1)
      throw new IllegalArgumentException("x must fall in the range 0 to 1");
    final double beta = _betaFunction.evaluate(x);
    if (x < _testValue)
      return beta * getContinuousFraction(_a, _b, x) / _a;
    return 1 - beta * getContinuousFraction(_b, _a, 1 - x) / _b;
  }

  private double getContinuousFraction(final double a, final double b, final double x) {
    int m2;
    double aa, c, d, del, h, qap, qam, qab;
    qab = a + b;
    qap = a + 1;
    qam = a - 1;
    c = 1;
    d = 1 - qab * x / qap;
    if (Math.abs(d) < EPS) {
      d = EPS;
    }
    d = 1. / d;
    h = d;
    for (int m = 1; m < 10000; m++) {
      m2 = 2 * m;
      aa = m * (b - m) * x / ((qam + m2) * (a + m2));
      d = getValueFromRecurrence(d, aa);
      c = getValueFromRecurrence(1. / c, aa);
      d = 1. / d;
      h *= c * d;
      aa = -(a + m) * (qab + m) * x / ((a + m2) * (qap + m2));
      d = getValueFromRecurrence(d, aa);
      c = getValueFromRecurrence(1. / c, aa);
      d = 1. / d;
      del = c * d;
      h *= del;
      if (Math.abs(del - 1) < EPS) {
        break;
      }
    }
    return h;
  }

  private double getValueFromRecurrence(final double d, final double aa) {
    double result = 1 + aa * d;
    if (Math.abs(result) < EPS) {
      result = EPS;
    }
    return result;
  }
}
