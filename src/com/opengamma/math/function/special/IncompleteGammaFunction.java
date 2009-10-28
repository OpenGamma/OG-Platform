/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.function.special;

import com.opengamma.math.MathException;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.function.Function2D;

/**
 * 
 * @author emcleod
 */
public class IncompleteGammaFunction extends Function2D<Double, Double> {
  private final Function1D<Double, Double> _lnGamma = new NaturalLogGammaFunction();
  private final int MAX_ITER = 100000;
  private final double EPS = 1e-20;

  // TODO Gauss-Laguerre quadrature when a is large.
  @Override
  public Double evaluate(final Double a, final Double x) {
    if (a <= 0)
      throw new IllegalArgumentException("Cannot have a negative value for a");
    double sum, delta, ap;
    final double gammaLn = _lnGamma.evaluate(a);
    ap = a;
    delta = sum = 1. / a;
    for (int i = 0; i < MAX_ITER; i++) {
      ++ap;
      delta *= x / ap;
      sum += delta;
      if (Math.abs(delta) < Math.abs(sum) * EPS)
        return sum * Math.exp(-x + a * Math.log(x) - gammaLn);
    }
    throw new MathException("Could not converge on value in " + MAX_ITER + " iterations");
  }

}
