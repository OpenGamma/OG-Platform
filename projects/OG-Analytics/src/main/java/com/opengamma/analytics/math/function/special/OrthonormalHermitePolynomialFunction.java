/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.function.special;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.DoubleFunction1D;
import com.opengamma.analytics.math.function.RealPolynomialFunction1D;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * 
 */
public class OrthonormalHermitePolynomialFunction extends OrthogonalPolynomialFunctionGenerator {
  private static final double C1 = 1. / Math.pow(Math.PI, 0.25);
  private static final double C2 = Math.sqrt(2) * C1;
  private static final RealPolynomialFunction1D F0 = new RealPolynomialFunction1D(new double[] {C1 });
  private static final RealPolynomialFunction1D DF1 = new RealPolynomialFunction1D(new double[] {C2 });

  @Override
  public DoubleFunction1D[] getPolynomials(final int n) {
    Validate.isTrue(n >= 0);
    final DoubleFunction1D[] polynomials = new DoubleFunction1D[n + 1];
    for (int i = 0; i <= n; i++) {
      if (i == 0) {
        polynomials[i] = F0;
      } else if (i == 1) {
        polynomials[i] = polynomials[0].multiply(Math.sqrt(2)).multiply(getX());
      } else {
        polynomials[i] = polynomials[i - 1].multiply(getX()).multiply(Math.sqrt(2. / i)).subtract(polynomials[i - 2].multiply(Math.sqrt((i - 1.) / i)));
      }
    }
    return polynomials;
  }

  @Override
  public Pair<DoubleFunction1D, DoubleFunction1D>[] getPolynomialsAndFirstDerivative(final int n) {
    Validate.isTrue(n >= 0);
    @SuppressWarnings("unchecked")
    final Pair<DoubleFunction1D, DoubleFunction1D>[] polynomials = new Pair[n + 1];
    DoubleFunction1D p, dp, p1, p2;
    //  final double divisor = Math.sqrt(2 * n);
    final double sqrt2 = Math.sqrt(2);
    final DoubleFunction1D x = getX();
    for (int i = 0; i <= n; i++) {
      if (i == 0) {
        polynomials[i] = Pairs.of((DoubleFunction1D) F0, getZero());
      } else if (i == 1) {
        polynomials[i] = Pairs.of(polynomials[0].getFirst().multiply(sqrt2).multiply(x), (DoubleFunction1D) DF1);
      } else {
        p1 = polynomials[i - 1].getFirst();
        p2 = polynomials[i - 2].getFirst();
        p = p1.multiply(x).multiply(Math.sqrt(2. / i)).subtract(p2.multiply(Math.sqrt((i - 1.) / i)));
        dp = p1.multiply(Math.sqrt(2 * i));
        polynomials[i] = Pairs.of(p, dp);
      }
    }
    return polynomials;
  }
}
