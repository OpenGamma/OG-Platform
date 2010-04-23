/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.function.special;

import com.opengamma.math.function.Function1D;

/**
 * 
 * The incomplete beta function is defined as:
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{equation*}
 * \\I_x(a, b)=\\frac{B_x(a, b)}{B(a, b)}\\int_0^x t^{a-1}(1-t)^{b-1}dt
 * \\end{equation*}}
 * where {latex.inline a, b > 0}.
 * 
 * This class uses the Commons Math library implementation of the Beta function <a href="http://commons.apache.org/math/api-2.1/index.html">
 * 
 * @author emcleod
 */
public class IncompleteBetaFunction extends Function1D<Double, Double> {
  private final double _beta;
  private final double _a;
  private final double _b;
  private final double _eps;
  private final int _maxIter;

  /**
   * 
   * @param a
   * @param b
   * @throws IllegalArgumentException If {@latex.inline $a \\leq 0$} or {@latex.inline $b \\leq 0$}
   */
  public IncompleteBetaFunction(final double a, final double b) {
    if (a <= 0)
      throw new IllegalArgumentException("a must be greater than or equal to zero");
    if (b <= 0)
      throw new IllegalArgumentException("b must be greater than or equal to zero");
    _a = a;
    _b = b;
    _beta = Math.exp(Beta.logBeta(a, b));
  }

  /**
   * 
   * @param a
   * @param b
   * @param eps
   * @param maxIter
   * @throws IllegalArgumentException If: 
   * <ul>
   * <li>{@latex.inline $a \\leq 0$}; 
   * <li>{@latex.inline $b \\leq 0$}; 
   * <li>eps{@latex.inline $ < 0$};
   * <li> maxIter{@latex.inline $ < 1$}
   * </ul>
   */
  public IncompleteBetaFunction(final double a, final double b, final double eps, final int maxIter) {
    if (a <= 0)
      throw new IllegalArgumentException("a must be greater than or equal to zero");
    if (b <= 0)
      throw new IllegalArgumentException("b must be greater than or equal to zero");
    if (eps < 0)
      throw new IllegalArgumentException("Epsilon must be greater than zero");
    if (maxIter < 1)
      throw new IllegalArgumentException("Maximum number of iterations must be greater than zero");
    _a = a;
    _b = b;
    _beta = Math.exp(Beta.logBeta(a, b));
    _eps = eps;
    _maxIter = maxIter;
  }

  /**
   * @param x
   * @throws IllegalArgumentException If {@latex.inline $x < 0$} or {@latex.inline $x > 1$}
   */
  @Override
  public Double evaluate(final Double x) {
    if (x < 0 || x > 1)
      throw new IllegalArgumentException("x must be in the range 0 to 1");
    return Beta.regularizedBeta(x, _a, _b, _eps, _maxIter) / _beta;
  }
}
