/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import com.opengamma.math.function.PolynomialFunction1D;

public class QuadraticRealRootFinder implements Polynomial1DRootFinder<Double> {

  public Double[] getRoots(final PolynomialFunction1D function) {
    if (function == null) {
      throw new IllegalArgumentException("Function was null");
    }
    final Double[] coefficients = function.getCoefficients();
    if (coefficients.length != 3) {
      throw new IllegalArgumentException("Function is not a quadratic");
    }
    final double c = coefficients[0];
    final double b = coefficients[1];
    final double a = coefficients[2];
    final double discriminant = b * b - 4 * a * c;
    if (discriminant < 0) {
      throw new RootNotFoundException("No real roots for quadratic");
    }
    final double q = -0.5 * (b + Math.signum(b) * discriminant);
    return new Double[] { q / a, c / q };
  }
}
