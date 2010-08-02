/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.function;

import org.apache.commons.lang.Validate;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class RealPolynomialFunction1D extends Function1D<Double, Double> {
  private final double[] _coefficients;

  /**
   * 
   * @param coefficients
   *          An array of coefficients <i>a<sub>i</sub></i> specifying a
   *          polynomial, with <br/>
   *          <i>y = a<sub>0</sub> + a<sub>1</sub>x + ... + a<sub>n
   *          -1</sub>x<sup>n - 1</sup></i><br/>
   *          If a coefficient is zero, the value in the array must be zero; a
   *          null value will throw an exception.
   */
  public RealPolynomialFunction1D(final double[] coefficients) {
    Validate.notNull(coefficients);
    ArgumentChecker.notEmpty(coefficients, "coefficients");
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

  public double[] getCoefficients() {
    return _coefficients;
  }
}
