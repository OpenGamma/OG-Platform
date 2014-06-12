/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.function.special;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.DoubleFunction1D;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * 
 */
public class LegendrePolynomialFunction extends OrthogonalPolynomialFunctionGenerator {

  @Override
  public DoubleFunction1D[] getPolynomials(final int n) {
    Validate.isTrue(n >= 0);
    final DoubleFunction1D[] polynomials = new DoubleFunction1D[n + 1];
    for (int i = 0; i <= n; i++) {
      if (i == 0) {
        polynomials[i] = getOne();
      } else if (i == 1) {
        polynomials[i] = getX();
      } else {
        polynomials[i] = (polynomials[i - 1].multiply(getX()).multiply(2 * i - 1).subtract(polynomials[i - 2].multiply(i - 1))).multiply(1. / i);
      }
    }
    return polynomials;
  }

  @Override
  public Pair<DoubleFunction1D, DoubleFunction1D>[] getPolynomialsAndFirstDerivative(final int n) {
    Validate.isTrue(n >= 0);
    @SuppressWarnings("unchecked")
    final Pair<DoubleFunction1D, DoubleFunction1D>[] polynomials = new Pair[n + 1];
    DoubleFunction1D p, dp;
    for (int i = 0; i <= n; i++) {
      if (i == 0) {
        polynomials[i] = Pairs.of(getOne(), getZero());
      } else if (i == 1) {
        polynomials[i] = Pairs.of(getX(), getOne());
      } else {
        p = (polynomials[i - 1].getFirst().multiply(getX()).multiply(2 * i - 1).subtract(polynomials[i - 2].getFirst().multiply(i - 1))).multiply(1. / i);
        dp = p.derivative();
        polynomials[i] = Pairs.of(p, dp);
      }
    }
    return polynomials;
  }
}
