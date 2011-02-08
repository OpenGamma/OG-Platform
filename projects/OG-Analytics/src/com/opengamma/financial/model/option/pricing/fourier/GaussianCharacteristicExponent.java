/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static com.opengamma.math.ComplexMathUtils.add;
import static com.opengamma.math.ComplexMathUtils.multiply;

import com.opengamma.math.number.ComplexNumber;

/**
 * 
 */
public class GaussianCharacteristicExponent extends CharacteristicExponent {

  private final double _mu;
  private final double _sigma;

  public GaussianCharacteristicExponent(final double mu, final double sigma) {
    _mu = mu;
    _sigma = sigma;
  }

  @Override
  public ComplexNumber evaluate(ComplexNumber u, double t) {

    ComplexNumber temp = multiply(_sigma, u);
    ComplexNumber res = add(multiply(u, new ComplexNumber(0, _mu)), multiply(-0.5, multiply(temp, temp)));
    return multiply(t, res);
  }
}
