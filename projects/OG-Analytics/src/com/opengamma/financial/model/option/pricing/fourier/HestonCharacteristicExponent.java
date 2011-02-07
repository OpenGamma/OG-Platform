/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static com.opengamma.math.ComplexMathUtils.add;
import static com.opengamma.math.ComplexMathUtils.divide;
import static com.opengamma.math.ComplexMathUtils.exp;
import static com.opengamma.math.ComplexMathUtils.log;
import static com.opengamma.math.ComplexMathUtils.multiply;
import static com.opengamma.math.ComplexMathUtils.sqrt;
import static com.opengamma.math.ComplexMathUtils.subtract;

import com.opengamma.math.number.ComplexNumber;

/**
 * 
 */
public class HestonCharacteristicExponent extends CharacteristicExponent {

  private final double _kappa;
  private final double _theta;
  private final double _vol0;
  private final double _omega;
  private final double _rho;

  /**
   * 
   * @param kappa mean reverting speed 
   * @param theta mean reverting level 
   * @param vol0 starting value of vol
   * @param omega vol-of-vol
   * @param rho correlation
   * @param t time to maturity 
   */
  public HestonCharacteristicExponent(final double kappa, final double theta, final double vol0, final double omega, final double rho) {
    _kappa = kappa;
    _theta = theta;
    _vol0 = vol0;
    _omega = omega;
    _rho = rho;

  }

  @Override
  public ComplexNumber evaluate(final ComplexNumber u, final double t) {
    return add(getC(u, t), multiply(_vol0, getD(u, t)));
  }

  private ComplexNumber getC(final ComplexNumber u, final double t) {
    ComplexNumber c1 = multiply(u, new ComplexNumber(0, _rho * _omega));
    ComplexNumber c = c(u);
    ComplexNumber d = d(u);
    ComplexNumber c3 = multiply(t, add(_kappa, subtract(d, c1)));
    ComplexNumber e = exp(multiply(d, t));
    ComplexNumber c4 = divide(subtract(multiply(c, e), 1), subtract(c, 1));
    c4 = log(c4);
    return multiply(_kappa * _theta / _omega / _omega, subtract(c3, multiply(2, c4)));
  }

  private ComplexNumber getD(final ComplexNumber u, final double t) {
    ComplexNumber c1 = multiply(u, new ComplexNumber(0, _rho * _omega));
    ComplexNumber c = c(u);
    ComplexNumber d = d(u);
    ComplexNumber c3 = add(_kappa, subtract(d, c1));
    ComplexNumber e = exp(multiply(d, t));
    ComplexNumber c4 = divide(subtract(e, 1), subtract(multiply(c, e), 1));
    return divide(multiply(c3, c4), _omega * _omega);
  }

  private ComplexNumber c(ComplexNumber u) {
    ComplexNumber c1 = multiply(u, new ComplexNumber(0, _rho * _omega));
    ComplexNumber c2 = d(u);
    ComplexNumber num = add(_kappa, subtract(c2, c1));
    ComplexNumber denom = subtract(_kappa, add(c2, c1));
    return divide(num, denom);
  }

  private ComplexNumber d(ComplexNumber u) {
    ComplexNumber c1 = subtract(multiply(u, new ComplexNumber(0, _rho * _omega)), _kappa);
    c1 = multiply(c1, c1);
    ComplexNumber c2 = multiply(u, new ComplexNumber(0, _omega * _omega));
    ComplexNumber c3 = multiply(u, _omega);
    c3 = multiply(c3, c3);
    ComplexNumber c4 = add(add(c1, c2), c3);
    return multiply(-1, sqrt(c4));
  }

}
