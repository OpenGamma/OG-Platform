/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static com.opengamma.math.number.ComplexNumber.MINUS_I;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.ComplexMathUtils;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.number.ComplexNumber;

/**
 * 
 */
public class TimeChangedCharacteristicExponent1 implements CharacteristicExponent1 {
  private final CharacteristicExponent1 _base;
  private final CharacteristicExponent1 _timeChange;

  public TimeChangedCharacteristicExponent1(final CharacteristicExponent1 base, final CharacteristicExponent1 timeChange) {
    Validate.notNull(base, "base");
    Validate.notNull(timeChange, "timeChange");
    _base = base;
    _timeChange = timeChange;
  }

  @Override
  public Function1D<ComplexNumber, ComplexNumber> getFunction(final double t) {
    final Function1D<ComplexNumber, ComplexNumber> baseFunction = _base.getFunction(1);
    final Function1D<ComplexNumber, ComplexNumber> timeChangeFunction = _timeChange.getFunction(t);

    return new Function1D<ComplexNumber, ComplexNumber>() {
      @Override
      public ComplexNumber evaluate(final ComplexNumber u) {
        final ComplexNumber z = ComplexMathUtils.multiply(MINUS_I, baseFunction.evaluate(u));
        return timeChangeFunction.evaluate(z);
      }
    };
  }

  @Override
  public double getLargestAlpha() {
    return Math.min(_base.getLargestAlpha(), _timeChange.getLargestAlpha());
  }

  @Override
  public double getSmallestAlpha() {
    return Math.max(_base.getSmallestAlpha(), _timeChange.getSmallestAlpha());
  }

  /**
   * Gets the base field.
   * @return the base
   */
  public CharacteristicExponent1 getBase() {
    return _base;
  }

  /**
   * Gets the timeChange field.
   * @return the timeChange
   */
  public CharacteristicExponent1 getTimeChange() {
    return _timeChange;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _base.hashCode();
    result = prime * result + _timeChange.hashCode();
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
    final TimeChangedCharacteristicExponent1 other = (TimeChangedCharacteristicExponent1) obj;
    if (!ObjectUtils.equals(_base, other._base)) {
      return false;
    }
    return ObjectUtils.equals(_timeChange, other._timeChange);
  }

}
