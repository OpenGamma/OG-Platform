/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static com.opengamma.math.number.ComplexNumber.MINUS_I;

import org.apache.commons.lang.Validate;

import com.opengamma.math.ComplexMathUtils;
import com.opengamma.math.number.ComplexNumber;

/**
 * 
 */
public class TimeChangedCharacteristicExponent extends CharacteristicExponent {
  private final CharacteristicExponent _base;
  private final CharacteristicExponent _timeChange;

  public TimeChangedCharacteristicExponent(final CharacteristicExponent base, final CharacteristicExponent timeChange) {
    Validate.notNull(base, "base");
    Validate.notNull(timeChange, "timeChange");
    Validate.isTrue(base.getTime() == 1.0, "base Characteristic Exponent must be evaulated at t = 1.0");
    _base = base;
    _timeChange = timeChange;
  }

  @Override
  public ComplexNumber evaluate(final ComplexNumber u) {
    final ComplexNumber z = ComplexMathUtils.multiply(MINUS_I, _base.evaluate(u));
    return _timeChange.evaluate(z);
  }

  @Override
  public double getLargestAlpha() {
    return Math.min(_base.getLargestAlpha(), _timeChange.getLargestAlpha());
  }

  @Override
  public double getSmallestAlpha() {
    return Math.max(_base.getSmallestAlpha(), _timeChange.getSmallestAlpha());
  }

  @Override
  public double getTime() {
    return _timeChange.getTime();
  }

}
