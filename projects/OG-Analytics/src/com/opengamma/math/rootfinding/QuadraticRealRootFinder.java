/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import org.apache.commons.lang.Validate;

import com.opengamma.math.MathException;
import com.opengamma.math.function.RealPolynomialFunction1D;

/**
 * 
 */
public class QuadraticRealRootFinder implements Polynomial1DRootFinder<Double> {

  @Override
  public Double[] getRoots(final RealPolynomialFunction1D function) {
    Validate.notNull(function, "function");
    final double[] coefficients = function.getCoefficients();
    if (coefficients.length != 3) {
      throw new IllegalArgumentException("Function is not a quadratic");
    }
    final double c = coefficients[0];
    final double b = coefficients[1];
    final double a = coefficients[2];
    final double discriminant = b * b - 4 * a * c;
    if (discriminant < 0) {
      throw new MathException("No real roots for quadratic");
    }
    final double q = -0.5 * (b + Math.signum(b) * discriminant);
    return new Double[] {q / a, c / q};
  }
}
