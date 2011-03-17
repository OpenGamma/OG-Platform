/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.function;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

/**
 * Class representing a polynomial that has real coefficients and takes a real argument. The function is defined as:
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{align*}
 * p(x) = a_0 + a_1 x + a_2 x^2 + \\ldots + a_{n-1} x^{n-1}
 * \\end{align*}
 * }
 */
public class RealPolynomialFunction1D extends DoubleFunction1D {
  private final double[] _coefficients;

  /**
   * The array of coefficients for a polynomial
   * {@latex.inline $p(x) = a_0 + a_1 x + a_2 x^2 + ... + a_{n-1} x^{n-1}$}
   * is {@latex.inline $\\{a_0, a_1, a_2, ..., a_{n-1}\\}$}.
   * @param coefficients The array of coefficients, not null or empty
   */
  public RealPolynomialFunction1D(final double[] coefficients) {
    Validate.notNull(coefficients);
    Validate.isTrue(coefficients.length > 0, "coefficients length must be greater than zero");
    _coefficients = coefficients;
  }

  @Override
  public Double evaluate(final Double x) {
    Validate.notNull(x, "x");
    final int n = _coefficients.length;
    double y = _coefficients[n - 1];
    for (int i = n - 2; i >= 0; i--) {
      y = x * y + _coefficients[i];
    }
    return y;
  }

  /**
   * Returns a function representing the exact derivative of this polynomial (which is itself a polynomial).
   * @return A function that calculates the derivative of this polynomial
   */
  @Override
  public DoubleFunction1D derivative() {
    final int n = _coefficients.length;
    final double[] coefficients = new double[n - 1];
    for (int i = 1; i < n; i++) {
      coefficients[i - 1] = _coefficients[i] * i;
    }
    return new RealPolynomialFunction1D(coefficients);
  }

  /**
   * @return The coefficients of this polynomial
   */
  public double[] getCoefficients() {
    return _coefficients;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_coefficients);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final RealPolynomialFunction1D other = (RealPolynomialFunction1D) obj;
    if (!Arrays.equals(_coefficients, other._coefficients)) {
      return false;
    }
    return true;
  }
}
