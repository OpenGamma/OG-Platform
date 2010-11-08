/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.function.special;

import org.apache.commons.math.special.Beta;

import com.opengamma.math.MathException;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 * The incomplete beta function is defined as:
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{equation*}
 * I_x(a, b)=\\frac{B_x(a, b)}{B(a, b)}\\int_0^x t^{a-1}(1-t)^{b-1}dt
 * \\end{equation*}}
 * where {latex.inline $a, b > 0$}.
 * <p>
 * This class uses the Commons Math library implementation of the Beta function <a href="http://commons.apache.org/math/api-2.1/index.html">
 * 
 */
public class IncompleteBetaFunction extends Function1D<Double, Double> {
  private final double _a;
  private final double _b;
  private final double _eps;
  private final int _maxIter;

  /**
   * 
   * @param a a
   * @param b b
   * @throws IllegalArgumentException If {@latex.inline $a \\leq 0$} or {@latex.inline $b \\leq 0$}
   */
  public IncompleteBetaFunction(final double a, final double b) {
    ArgumentChecker.notNegativeOrZero(a, "a");
    ArgumentChecker.notNegativeOrZero(b, "b");
    _a = a;
    _b = b;
    _maxIter = 10000;
    _eps = 1e-12;
  }

  /**
   * 
   * @param a a
   * @param b b
   * @param eps epsilon
   * @param maxIter maximum number of iterations
   * @throws IllegalArgumentException If: 
   * <ul>
   * <li>{@latex.inline $a \\leq 0$}; 
   * <li>{@latex.inline $b \\leq 0$}; 
   * <li>eps{@latex.inline $ < 0$};
   * <li> maxIter{@latex.inline $ < 1$}
   * </ul>
   */
  public IncompleteBetaFunction(final double a, final double b, final double eps, final int maxIter) {
    ArgumentChecker.notNegativeOrZero(a, "a");
    ArgumentChecker.notNegativeOrZero(b, "b");
    ArgumentChecker.notNegative(eps, "eps");
    if (maxIter < 1) {
      throw new IllegalArgumentException("Maximum number of iterations must be greater than zero");
    }
    _a = a;
    _b = b;
    _eps = eps;
    _maxIter = maxIter;
  }

  /**
   * @param x x
   * @return the value of the function
   * @throws IllegalArgumentException If {@latex.inline $x < 0$} or {@latex.inline $x > 1$}
   */
  @Override
  public Double evaluate(final Double x) {
    if (!ArgumentChecker.isInRangeInclusive(0, 1, x)) {
      throw new IllegalArgumentException("x must be in the range 0 to 1");
    }
    try {
      return Beta.regularizedBeta(x, _a, _b, _eps, _maxIter);
    } catch (final org.apache.commons.math.MathException e) {
      throw new MathException(e);
    }
  }
}
