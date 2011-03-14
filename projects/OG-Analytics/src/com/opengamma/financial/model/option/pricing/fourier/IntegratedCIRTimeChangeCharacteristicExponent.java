/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static com.opengamma.math.ComplexMathUtils.add;
import static com.opengamma.math.ComplexMathUtils.divide;
import static com.opengamma.math.ComplexMathUtils.log;
import static com.opengamma.math.ComplexMathUtils.mod;
import static com.opengamma.math.ComplexMathUtils.multiply;
import static com.opengamma.math.ComplexMathUtils.sqrt;
import static com.opengamma.math.ComplexMathUtils.subtract;
import static com.opengamma.math.number.ComplexNumber.I;

import com.opengamma.math.TrigonometricFunctionUtils;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.number.ComplexNumber;

/**
 * 
 */
public class IntegratedCIRTimeChangeCharacteristicExponent implements CharacteristicExponent {
  private final double _kappa;
  private final double _theta;
  private final double _lambda;

  public IntegratedCIRTimeChangeCharacteristicExponent(final double kappa, final double theta, final double lambda) {
    _kappa = kappa;
    _theta = theta;
    _lambda = lambda;
  }

  @Override
  public Function1D<ComplexNumber, ComplexNumber> getFunction(final double t) {
    return new Function1D<ComplexNumber, ComplexNumber>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public ComplexNumber evaluate(final ComplexNumber u) {

        if (u.getReal() == 0.0 && u.getImaginary() == 0.0) {
          return new ComplexNumber(0.0);
        }

        final ComplexNumber ui = multiply(I, u);

        //handle small lambda properly 
        if (2 * mod(u) * _lambda * _lambda / _kappa / _kappa < 1e-6) {
          final double d = _theta * t + (1 - _theta) * (1 - Math.exp(-_kappa * t)) / _kappa;
          return multiply(d, ui);
        }

        ComplexNumber temp = subtract(_kappa * _kappa, multiply(2 * _lambda * _lambda, ui));
        final ComplexNumber gamma = sqrt(temp);
        final ComplexNumber gammaHalfT = multiply(gamma, t / 2.0);
        temp = divide(multiply(2, ui), add(_kappa, divide(gamma, TrigonometricFunctionUtils.tanh(gammaHalfT))));
        final ComplexNumber kappaOverGamma = divide(_kappa, gamma);
        final double power = 2 * _kappa * _theta / _lambda / _lambda;
        final ComplexNumber res = add(multiply(power, subtract(_kappa * t / 2, getLogCoshSinh(gammaHalfT, kappaOverGamma))), temp);
        return res;

      }
    };
  }

  // ln(cosh(a) + bsinh(a)
  private ComplexNumber getLogCoshSinh(final ComplexNumber a, final ComplexNumber b) {
    final ComplexNumber temp = add(TrigonometricFunctionUtils.cosh(a), multiply(b, TrigonometricFunctionUtils.sinh(a)));
    return log(temp);
  }

  @Override
  public double getLargestAlpha() {
    return _kappa * _kappa / 2 / _lambda / _lambda;
  }

  @Override
  public double getSmallestAlpha() {
    return Double.NEGATIVE_INFINITY;
  }

  /**
   * Gets the kappa field.
   * @return the kappa
   */
  public double getKappa() {
    return _kappa;
  }

  /**
   * Gets the theta field.
   * @return the theta
   */
  public double getTheta() {
    return _theta;
  }

  /**
   * Gets the lambda field.
   * @return the lambda
   */
  public double getLambda() {
    return _lambda;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_kappa);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_lambda);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_theta);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    final IntegratedCIRTimeChangeCharacteristicExponent other = (IntegratedCIRTimeChangeCharacteristicExponent) obj;
    if (Double.doubleToLongBits(_kappa) != Double.doubleToLongBits(other._kappa)) {
      return false;
    }
    if (Double.doubleToLongBits(_lambda) != Double.doubleToLongBits(other._lambda)) {
      return false;
    }
    return Double.doubleToLongBits(_theta) == Double.doubleToLongBits(other._theta);
  }

}
