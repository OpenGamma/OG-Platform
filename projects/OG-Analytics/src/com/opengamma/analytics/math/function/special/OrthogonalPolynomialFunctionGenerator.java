/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.function.special;

import com.opengamma.analytics.math.function.DoubleFunction1D;
import com.opengamma.analytics.math.function.RealPolynomialFunction1D;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public abstract class OrthogonalPolynomialFunctionGenerator {
  private static final RealPolynomialFunction1D ZERO = new RealPolynomialFunction1D(new double[] {0});
  private static final RealPolynomialFunction1D ONE = new RealPolynomialFunction1D(new double[] {1});
  private static final RealPolynomialFunction1D X = new RealPolynomialFunction1D(new double[] {0, 1});

  public abstract DoubleFunction1D[] getPolynomials(int n);

  public abstract Pair<DoubleFunction1D, DoubleFunction1D>[] getPolynomialsAndFirstDerivative(final int n);

  protected DoubleFunction1D getZero() {
    return ZERO;
  }

  protected DoubleFunction1D getOne() {
    return ONE;
  }

  protected DoubleFunction1D getX() {
    return X;
  }
}
