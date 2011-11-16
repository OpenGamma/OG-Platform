/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static com.opengamma.math.number.ComplexNumber.MINUS_I;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.ComplexMathUtils;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.number.ComplexNumber;

/**
 * This characteristic exponent converts a Levy process from calendar time to business time (where time moves at a stochastic rate relative to
 * calendar time). This has the effect of introducing stochastic changes to the model parameters of the original Levy process.
 * <p>
 * If the time-changed Levy process is {@latex.inline $X_{Y_t}$}, with {@latex.inline $Y_t$} the business time, the characteristic function is given by:
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{align*}
 * \\phi(u, t) &= E\\left[e^{iuX_{Y_t}}\\right]\\\\ 
 * &= E\\left[e^{Y_t\\psi_X(u)}\\right]\\\\
 * &= E\\left[e^{i(-i\\psi_X(u))Y_t}\\right]\\\\
 * &= \\phi_{Y_t}(-i\\psi_X(u), t)
 * \\end{align*}
 * }
 * where {@latex.inline $\\psi_X(u)$} is the cumulant characteristic function of the Levy process. The drift correction then becomes
 * {@latex.ilb %preamble{\\usepackage{amsmath}}
 * \\begin{align*}
 * \\omega(t) = -\\frac{\\ln(\\phi(-i, t))}{t}
 * \\end{align*}
 * }
 */
public class TimeChangedCharacteristicExponent implements CharacteristicExponent {
  private final CharacteristicExponent _base;
  private final CharacteristicExponent _timeChange;

  /**
   * 
   * @param base The base characteristic exponent, not null
   * @param timeChange The characteristic exponent to time change, not null
   */
  public TimeChangedCharacteristicExponent(final CharacteristicExponent base, final StocasticClockCharcteristicExponent timeChange) {
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
  public ComplexNumber getValue(ComplexNumber u, double t) {
    Function1D<ComplexNumber, ComplexNumber> func = getFunction(t);
    return func.evaluate(u);
  }

  /**
   * 
   * @return the smaller {@latex.inline $alpha_{max}$} of the base characteristic exponent and the time-changed characteristic exponent
   */
  @Override
  public double getLargestAlpha() {
    return Math.min(_base.getLargestAlpha(), _timeChange.getLargestAlpha());
  }

  /**
   * 
   * @return the larger {@latex.inline $alpha_{min}$} of the base characteristic exponent and the time-changed characteristic exponent
   */
  @Override
  public double getSmallestAlpha() {
    return Math.max(_base.getSmallestAlpha(), _timeChange.getSmallestAlpha());
  }

  /**
   * Gets the base field.
   * @return the base
   */
  public CharacteristicExponent getBase() {
    return _base;
  }

  /**
   * Gets the timeChange field.
   * @return the timeChange
   */
  public CharacteristicExponent getTimeChange() {
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
    final TimeChangedCharacteristicExponent other = (TimeChangedCharacteristicExponent) obj;
    if (!ObjectUtils.equals(_base, other._base)) {
      return false;
    }
    return ObjectUtils.equals(_timeChange, other._timeChange);
  }

  @Override
  public ComplexNumber[] getCharacteristicExponentAdjoint(ComplexNumber u, double t) {
    throw new NotImplementedException();
  }

  @Override
  public Function1D<ComplexNumber, ComplexNumber[]> getAdjointFunction(double t) {
    throw new NotImplementedException();
  }

}
