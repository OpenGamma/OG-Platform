/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.function.special;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.DoubleFunction1D;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class HermitePolynomialFunction extends OrthogonalPolynomialFunctionGenerator {
  private static final DoubleFunction1D TWO_X = new DoubleFunction1D() {

    @Override
    public Double evaluate(final Double x) {
      return 2 * x;
    }

  };

  @Override
  public DoubleFunction1D[] getPolynomials(final int n) {
    Validate.isTrue(n >= 0);
    final DoubleFunction1D[] polynomials = new DoubleFunction1D[n + 1];
    for (int i = 0; i <= n; i++) {
      if (i == 0) {
        polynomials[i] = getOne();
      } else if (i == 1) {
        polynomials[i] = TWO_X;
      } else {
        polynomials[i] = polynomials[i - 1].multiply(2).multiply(getX()).subtract(polynomials[i - 2].multiply(2 * i - 2));
      }
    }
    return polynomials;
  }

  @Override
  public Pair<DoubleFunction1D, DoubleFunction1D>[] getPolynomialsAndFirstDerivative(final int n) {
    throw new NotImplementedException();
  }
}
