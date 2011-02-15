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

import com.opengamma.math.TrigonometricFunctionUtils;
import com.opengamma.math.number.ComplexNumber;

/**
 * 
 */
public class IntegratedCIRTimeChangeCharacteristicExponent extends CharacteristicExponent {

  private final double _kappa;
  private final double _theta;
  private final double _lambda;
  private final double _t;

  public IntegratedCIRTimeChangeCharacteristicExponent(final double kappa, final double theta, final double lambda, final double t) {
    _kappa = kappa;
    _theta = theta;
    _lambda = lambda;
    _t = t;
  }

  @Override
  public ComplexNumber evaluate(ComplexNumber u) {

    if (u.getReal() == 0.0 && u.getImaginary() == 0.0) {
      return new ComplexNumber(0.0);
    }

    ComplexNumber ui = multiply(I, u);

    //handle small lambda properly 
    if (2 * mod(u) * _lambda * _lambda / _kappa / _kappa < 1e-6) {
      double d = _theta * _t + (1 - _theta) * (1 - Math.exp(-_kappa * _t)) / _kappa;
      return multiply(d, ui);
    }

    ComplexNumber temp = subtract(_kappa * _kappa, multiply(2 * _lambda * _lambda, ui));
    ComplexNumber gamma = sqrt(temp);
    ComplexNumber gammaHalfT = multiply(gamma, _t / 2.0);
    temp = divide(multiply(2, ui), add(_kappa, divide(gamma, TrigonometricFunctionUtils.tanh(gammaHalfT))));
    ComplexNumber kappaOverGamma = divide(_kappa, gamma);
    double power = 2 * _kappa * _theta / _lambda / _lambda;
    ComplexNumber res = add(multiply(power, subtract(_kappa * _t / 2, getLogCoshSinh(gammaHalfT, kappaOverGamma))), temp);
    return res;

  }

  // ln(cosh(a) + bsinh(a)
  private ComplexNumber getLogCoshSinh(final ComplexNumber a, final ComplexNumber b) {
    ComplexNumber temp = add(TrigonometricFunctionUtils.cosh(a), multiply(b, TrigonometricFunctionUtils.sinh(a)));
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

  @Override
  public double getTime() {
    return _t;
  }

}
