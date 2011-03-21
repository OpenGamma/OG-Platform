/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.function.special;

import org.apache.commons.lang.Validate;
import org.apache.commons.math.special.Beta;

import com.opengamma.math.MathException;
import com.opengamma.math.function.Function1D;

/**
 * 
 * The incomplete beta function is defined as:
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{equation*}
 * I_x(a, b)=\\frac{B_x(a, b)}{B(a, b)}\\int_0^x t^{a-1}(1-t)^{b-1}dt
 * \\end{equation*}}
 * where {@latex.inline $a, b > 0$}.
 * <p>
 * This class uses the <a href="http://commons.apache.org/math/api-2.1/org/apache/commons/math/special/Beta.html">Commons Math library implementation</a> of the Beta function.
 * 
 */
public class IncompleteBetaFunction extends Function1D<Double, Double> {
  private final double _a;
  private final double _b;
  private final double _eps;
  private final int _maxIter;

  /**
   * Uses the default values for the accuracy (10<sup>-12</sup>) and number of iterations (10000).
   * @param a a, {@latex.inline $a > 0$}
   * @param b b, {@latex.inline $b > 0$}
   */
  public IncompleteBetaFunction(final double a, final double b) {
    this(a, b, 1e-12, 10000);
  }

  /**
   * 
   * @param a a, {@latex.inline $a > 0$}
   * @param b b, {@latex.inline $b > 0$}
   * @param eps Approximation accuracy, {@latex.inline $\\epsilon \\geq 0$}
   * @param maxIter Maximum number of iterations, {@latex.inline iter $\\geq 1$}
   */
  public IncompleteBetaFunction(final double a, final double b, final double eps, final int maxIter) {
    Validate.isTrue(a > 0, "a must be > 0");
    Validate.isTrue(b > 0, "b must be > 0");
    Validate.isTrue(eps >= 0, "eps must not be negative");
    Validate.isTrue(maxIter >= 1, "maximum number of iterations must be greater than zero");
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
    Validate.isTrue(x >= 0 && x <= 1, "x must be in the range 0 to 1");
    try {
      return Beta.regularizedBeta(x, _a, _b, _eps, _maxIter);
    } catch (final org.apache.commons.math.MathException e) {
      throw new MathException(e);
    }
  }
}
