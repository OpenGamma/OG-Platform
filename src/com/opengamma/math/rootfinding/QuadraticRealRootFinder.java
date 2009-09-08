package com.opengamma.math.rootfinding;

import com.opengamma.math.function.PolynomialFunction1D;

/**
 * 
 * @author emcleod
 * 
 */

public class QuadraticRealRootFinder implements Polynomial1DRootFinder {

  public Double[] getRoot(PolynomialFunction1D f) throws RootNotFoundException {
    Double[] coefficients = f.getCoefficients();
    if (coefficients.length != 3)
      throw new IllegalArgumentException("Function is not a quadratic");
    double c = coefficients[0];
    double b = coefficients[1];
    double a = coefficients[2];
    double discriminant = b * b - 4 * a * c;
    if (discriminant < 0)
      throw new RootNotFoundException("No real roots for quadratic");
    double q = -0.5 * (b + Math.signum(b) * discriminant);
    return new Double[] { q / a, c / q };
  }
}
