package com.opengamma.math.function;

import com.opengamma.math.number.ComplexNumber;

/**
 * 
 * @author emcleod
 * 
 */

public class ComplexNumberPolynomialFunction1D extends Function1D<ComplexNumber, ComplexNumber> {
  // TODO Number -> ComplexNumber
  // TODO ComplexNumber -> Double
  private ComplexNumber[] _coefficients;

  public ComplexNumberPolynomialFunction1D(ComplexNumber[] coefficients) throws IllegalArgumentException {
    if (coefficients == null) throw new IllegalArgumentException("Coefficient array was null");
    if (coefficients.length == 0) throw new IllegalArgumentException("Coefficient array was empty");
    for (int i = 0; i < coefficients.length; i++) {
      if (coefficients[i] == null) throw new IllegalArgumentException("There was a null value in the coefficient array at element " + i);
    }
    _coefficients = coefficients;
  }

  @Override
  public ComplexNumber evaluate(ComplexNumber x) {
    // TODO
    return null;
  }
}
