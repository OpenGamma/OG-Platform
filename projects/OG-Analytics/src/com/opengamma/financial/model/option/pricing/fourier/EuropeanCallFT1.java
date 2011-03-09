/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static com.opengamma.math.ComplexMathUtils.add;
import static com.opengamma.math.ComplexMathUtils.divide;
import static com.opengamma.math.ComplexMathUtils.exp;
import static com.opengamma.math.ComplexMathUtils.multiply;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.number.ComplexNumber;

/**
 * 
 */
public class EuropeanCallFT1 {
  private final CharacteristicExponent1 _ce;

  public EuropeanCallFT1(final CharacteristicExponent1 ce) {
    _ce = new MeanCorrectedCharacteristicExponent1(ce);
  }

  public Function1D<ComplexNumber, ComplexNumber> getFunction(final double t) {
    return new Function1D<ComplexNumber, ComplexNumber>() {

      @Override
      public ComplexNumber evaluate(final ComplexNumber z) {
        @SuppressWarnings("synthetic-access")
        final ComplexNumber num = exp(_ce.getFunction(t).evaluate(z));
        final ComplexNumber denom = multiply(z, add(z, ComplexNumber.I));
        final ComplexNumber res = multiply(-1.0, divide(num, denom));
        return res;
      }
    };
  }
}
