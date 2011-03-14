/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static com.opengamma.math.ComplexMathUtils.add;
import static com.opengamma.math.ComplexMathUtils.multiply;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.number.ComplexNumber;

/**
 * This class is primarily for testing 
 */
public class GaussianCharacteristicExponent implements CharacteristicExponent {
  private final double _mu;
  private final double _sigma;

  public GaussianCharacteristicExponent(final double mu, final double sigma) {
    Validate.isTrue(sigma > 0.0, "sigma > 0");
    _mu = mu;
    _sigma = sigma;
  }

  @Override
  public Function1D<ComplexNumber, ComplexNumber> getFunction(final double t) {
    Validate.isTrue(t > 0.0, "t > 0");
    return new Function1D<ComplexNumber, ComplexNumber>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public ComplexNumber evaluate(final ComplexNumber u) {
        Validate.notNull(u, "u");
        final ComplexNumber temp = multiply(_sigma, u);
        final ComplexNumber res = add(multiply(u, new ComplexNumber(0, _mu)), multiply(-0.5, multiply(temp, temp)));
        return multiply(t, res);
      }

    };
  }

  @Override
  public double getLargestAlpha() {
    return Double.POSITIVE_INFINITY;
  }

  @Override
  public double getSmallestAlpha() {
    return Double.NEGATIVE_INFINITY;
  }

  /**
   * Gets the mu field.
   * @return the mu
   */
  public double getMu() {
    return _mu;
  }

  /**
   * Gets the sigma field.
   * @return the sigma
   */
  public double getSigma() {
    return _sigma;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_mu);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_sigma);
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
    final GaussianCharacteristicExponent other = (GaussianCharacteristicExponent) obj;
    if (Double.doubleToLongBits(_mu) != Double.doubleToLongBits(other._mu)) {
      return false;
    }
    return Double.doubleToLongBits(_sigma) == Double.doubleToLongBits(other._sigma);
  }

}
