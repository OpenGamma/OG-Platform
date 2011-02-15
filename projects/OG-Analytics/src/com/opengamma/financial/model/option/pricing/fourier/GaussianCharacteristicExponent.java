/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static com.opengamma.math.ComplexMathUtils.add;
import static com.opengamma.math.ComplexMathUtils.multiply;

import org.apache.commons.lang.Validate;

import com.opengamma.math.number.ComplexNumber;

/**
 * This class is primarily for testing 
 */
public class GaussianCharacteristicExponent extends CharacteristicExponent {

  private final double _mu;
  private final double _sigma;
  private final double _t;

  public GaussianCharacteristicExponent(final double mu, final double sigma, final double t) {
    Validate.isTrue(sigma > 0.0, "sigma > 0");
    Validate.isTrue(t > 0.0, "t > 0");
    _mu = mu;
    _sigma = sigma;
    _t = t;
  }

  @Override
  public ComplexNumber evaluate(ComplexNumber u) {

    ComplexNumber temp = multiply(_sigma, u);
    ComplexNumber res = add(multiply(u, new ComplexNumber(0, _mu)), multiply(-0.5, multiply(temp, temp)));
    return multiply(_t, res);
  }

  @Override
  public double getLargestAlpha() {
    return Double.POSITIVE_INFINITY;
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
