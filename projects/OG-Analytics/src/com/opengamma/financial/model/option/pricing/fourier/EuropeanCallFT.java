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
public class EuropeanCallFT extends Function1D<ComplexNumber, ComplexNumber> {

  private static final ComplexNumber I = new ComplexNumber(0, 1);

  private final CharacteristicExponent _ce;

  private final double _t;

  public EuropeanCallFT(final CharacteristicExponent ce, final double maturity) {
    _ce = new MeanCorrectedCharacteristicExponent(ce);
    _t = maturity;
  }

  @Override
  public ComplexNumber evaluate(ComplexNumber z) {

    ComplexNumber num = exp(_ce.evaluate(z, _t));
    ComplexNumber denom = multiply(z, add(z, I));
    ComplexNumber res = multiply(-1.0, divide(num, denom));
    return res;
  }
}
