package com.opengamma.math.function;

/**
 * 
 * @author emcleod
 * 
 */
public class PolynomialFunction1D extends Function1D<Double, Double> {
  private Double[] _coefficients;

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
  public PolynomialFunction1D(Double[] coefficients) throws IllegalArgumentException {
    if (coefficients == null) throw new IllegalArgumentException("Coefficient array was null");
    if (coefficients.length == 0) throw new IllegalArgumentException("Coefficient array was empty");
    for (int i = 0; i < coefficients.length; i++) {
      if (coefficients[i] == null) throw new IllegalArgumentException("There was a null value in the coefficient array at element " + i);
    }
    _coefficients = coefficients;
  }

  @Override
  public Double evaluate(Double x) {
    if (x == null) throw new IllegalArgumentException("Null argument");
    int n = _coefficients.length;
    double y = _coefficients[n - 1].doubleValue();
    for (int i = n - 2; i >= 0; i--) {
      y = x * y + _coefficients[i];
    }
    return y;
  }

  public Double[] getCoefficients() {
    return _coefficients;
  }
}
