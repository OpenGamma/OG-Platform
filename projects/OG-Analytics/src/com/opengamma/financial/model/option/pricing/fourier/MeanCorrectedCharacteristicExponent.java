/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static com.opengamma.math.ComplexMathUtils.add;
import static com.opengamma.math.ComplexMathUtils.multiply;
import static com.opengamma.math.number.ComplexNumber.MINUS_I;

import org.apache.commons.lang.Validate;

import com.opengamma.math.number.ComplexNumber;

/**
 * 
 */
public class MeanCorrectedCharacteristicExponent extends CharacteristicExponent {

  private final CharacteristicExponent _base;
  private final ComplexNumber _w;

  public MeanCorrectedCharacteristicExponent(final CharacteristicExponent base) {
    Validate.notNull(base);
    _base = base;
    final ComplexNumber temp = _base.evaluate(MINUS_I);
    Validate.isTrue(Math.abs(temp.getImaginary()) < 1e-12, "problem with CharacteristicExponentFunction");
    _w = new ComplexNumber(0, -temp.getReal());
  }

  @Override
  public ComplexNumber evaluate(final ComplexNumber u) {

    return add(_base.evaluate(u), multiply(_w, u));
  }

  @Override
  public double getLargestAlpha() {
    return _base.getLargestAlpha();
  }

  @Override
  public double getSmallestAlpha() {
    return _base.getSmallestAlpha();
  }

  @Override
  public double getTime() {
    return _base.getTime();
  }

}
