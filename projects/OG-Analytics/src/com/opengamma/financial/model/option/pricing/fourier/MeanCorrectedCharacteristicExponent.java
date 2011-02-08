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
 * 
 */
public class MeanCorrectedCharacteristicExponent extends CharacteristicExponent {

  private final CharacteristicExponent _base;

  public MeanCorrectedCharacteristicExponent(CharacteristicExponent base) {
    Validate.notNull(base);
    _base = base;
  }

  @Override
  public ComplexNumber evaluate(ComplexNumber u, double t) {

    ComplexNumber temp = _base.evaluate(MINUS_I, t);
    Validate.isTrue(temp.getImaginary() == 0.0, "problem with CharacteristicExponentFunction");
    ComplexNumber w = new ComplexNumber(0, -temp.getReal());

    return add(_base.evaluate(u, t), multiply(w, u));
  }

}
