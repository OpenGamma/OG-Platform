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
public class LaguerrePolynomialFunction extends OrthogonalPolynomialFunctionGenerator {
  private static final DoubleFunction1D F1 = new RealPolynomialFunction1D(new double[] {1, -1});
  private static final DoubleFunction1D DF1 = new RealPolynomialFunction1D(new double[] {-1});

  @Override
  public DoubleFunction1D[] getPolynomials(final int n) {
    return getPolynomials(n, 0);
  }

  @Override
  public Pair<DoubleFunction1D, DoubleFunction1D>[] getPolynomialsAndFirstDerivative(final int n) {
    return getPolynomialsAndFirstDerivative(n, 0);
  }

  public DoubleFunction1D[] getPolynomials(final int n, final double alpha) {
    Validate.isTrue(n >= 0);
    final DoubleFunction1D[] polynomials = new DoubleFunction1D[n + 1];
    for (int i = 0; i <= n; i++) {
      if (i == 0) {
        polynomials[i] = getOne();
      } else if (i == 1) {
        polynomials[i] = new RealPolynomialFunction1D(new double[] {1 + alpha, -1});
      } else {
        polynomials[i] = (polynomials[i - 1].multiply(2. * i + alpha - 1).subtract(polynomials[i - 1].multiply(getX())).subtract(polynomials[i - 2].multiply((i - 1. + alpha))).divide(i));
      }
    }
    return polynomials;
  }

  public Pair<DoubleFunction1D, DoubleFunction1D>[] getPolynomialsAndFirstDerivative(final int n, final double alpha) {
    Validate.isTrue(n >= 0);
    @SuppressWarnings("unchecked")
    final Pair<DoubleFunction1D, DoubleFunction1D>[] polynomials = new Pair[n + 1];
    DoubleFunction1D p, dp, p1, p2;
    for (int i = 0; i <= n; i++) {
      if (i == 0) {
        polynomials[i] = Pairs.of(getOne(), getZero());
      } else if (i == 1) {
        polynomials[i] = Pairs.of(F1, DF1);
      } else {
        p1 = polynomials[i - 1].getFirst();
        p2 = polynomials[i - 2].getFirst();
        p = (p1.multiply(2. * i + alpha - 1).subtract(p1.multiply(getX())).subtract(p2.multiply((i - 1. + alpha))).divide(i));
        dp = (p.multiply(i).subtract(p1.multiply(i + alpha))).divide(getX());
        polynomials[i] = Pairs.of(p, dp);
      }
    }
    return polynomials;
  }
}
