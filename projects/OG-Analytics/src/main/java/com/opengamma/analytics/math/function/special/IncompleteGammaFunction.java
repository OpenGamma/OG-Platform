/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.function.special;

import org.apache.commons.math.special.Gamma;

import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 * The incomplete gamma function is defined as:
 * $$
 * \begin{equation*}
 * P(a, x) = \frac{\gamma(a, x)}{\Gamma(a)}\int_0^x e^{-t}t^{a-1}dt
 * \end{equation*}
 * $$
 * where $a > 0$.
 * <p>
 * This class is a wrapper for the Commons Math library implementation of the incomplete gamma function <a href="http://commons.apache.org/math/api-2.1/index.html">link</a>
 *
 */
public class IncompleteGammaFunction extends Function1D<Double, Double> {
  private final int _maxIter;
  private final double _eps;
  private final double _a;

  public IncompleteGammaFunction(final double a) {
    ArgumentChecker.notNegativeOrZero(a, "a");
    _maxIter = 100000;
    _eps = 1e-12;
    _a = a;
  }

  public IncompleteGammaFunction(final double a, final int maxIter, final double eps) {
    ArgumentChecker.notNegativeOrZero(a, "a");
    ArgumentChecker.notNegative(eps, "eps");
    if (maxIter < 1) {
      throw new IllegalArgumentException("Must have at least one iteration");
    }
    _maxIter = maxIter;
    _eps = eps;
    _a = a;
  }

  @Override
  public Double evaluate(final Double x) {
    try {
      return Gamma.regularizedGammaP(_a, x, _eps, _maxIter);
    } catch (final org.apache.commons.math.MathException e) {
      throw new MathException(e);
    }
  }

}
