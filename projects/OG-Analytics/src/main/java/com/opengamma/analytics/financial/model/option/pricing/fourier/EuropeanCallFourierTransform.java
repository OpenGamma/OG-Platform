/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.fourier;

import static com.opengamma.analytics.math.ComplexMathUtils.add;
import static com.opengamma.analytics.math.ComplexMathUtils.divide;
import static com.opengamma.analytics.math.ComplexMathUtils.exp;
import static com.opengamma.analytics.math.ComplexMathUtils.multiply;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.number.ComplexNumber;

/**
 * Gives the Fourier transform of a European call $\hat{C}(z) =
 * -\frac{\phi(z)}{z(z+i)}$ where $\phi(z)$ is the characteristic function of
 *  the (normalised) terminal distribution of the underlying 
 */
public class EuropeanCallFourierTransform {
  private final CharacteristicExponent _ce;

  public EuropeanCallFourierTransform(final MartingaleCharacteristicExponent ce) {
    Validate.notNull(ce, "characteristic exponent");
    _ce = ce;
  }

  public Function1D<ComplexNumber, ComplexNumber> getFunction(final double t) {
    final Function1D<ComplexNumber, ComplexNumber> function = _ce.getFunction(t);
    return new Function1D<ComplexNumber, ComplexNumber>() {

      @Override
      public ComplexNumber evaluate(final ComplexNumber z) {
        final ComplexNumber num = exp(function.evaluate(z));
        final ComplexNumber denom = multiply(z, add(z, ComplexNumber.I));
        final ComplexNumber res = multiply(-1.0, divide(num, denom));
        return res;
      }
    };
  }

  /**
   * Gets the _ce field.
   * @return the _ce
   */
  public CharacteristicExponent getCharacteristicExponent() {
    return _ce;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _ce.hashCode();
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
    final EuropeanCallFourierTransform other = (EuropeanCallFourierTransform) obj;
    return ObjectUtils.equals(_ce, other._ce);
  }

}
