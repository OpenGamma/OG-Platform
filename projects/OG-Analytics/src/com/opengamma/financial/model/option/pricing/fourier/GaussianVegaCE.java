/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static com.opengamma.math.ComplexMathUtils.add;
import static com.opengamma.math.ComplexMathUtils.log;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.number.ComplexNumber;

/**
 * @deprecated This only exists for code testing during development 
 */
@Deprecated 
public class GaussianVegaCE implements MartingaleCharacteristicExponent {

  private final GaussianCharacteristicExponent _base;
  private final GaussianCharacteristicExponent _div;

  public GaussianVegaCE(final double sigma) {
    _base = new GaussianCharacteristicExponent(-0.5 * sigma * sigma, sigma);
    _div = new GaussianCharacteristicExponent(-sigma, Math.sqrt(2 * sigma));
  }

  @Override
  public Function1D<ComplexNumber, ComplexNumber> getFunction(final double t) {
    Validate.isTrue(t > 0.0, "t > 0");
    final Function1D<ComplexNumber, ComplexNumber> baseFunc = _base.getFunction(t);
    final Function1D<ComplexNumber, ComplexNumber> divFunc = _div.getFunction(t);
    return new Function1D<ComplexNumber, ComplexNumber>() {

      @Override
      public ComplexNumber evaluate(final ComplexNumber u) {
        Validate.notNull(u, "u");
        final ComplexNumber psi = baseFunc.evaluate(u);
        final ComplexNumber temp = divFunc.evaluate(u);
        final ComplexNumber temp2 = log(temp); //don't like taking logs - bad things happen 
        final ComplexNumber res = add(psi, temp2);
        return res;

      }

    };
  }

  @Override
  public ComplexNumber getValue(ComplexNumber u, double t) {
    Function1D<ComplexNumber, ComplexNumber> func = getFunction(t);
    return func.evaluate(u);
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
  public ComplexNumber[] getCharacteristicExponentAdjoint(ComplexNumber u, double t) {
    throw new NotImplementedException();
  }

  @Override
  public Function1D<ComplexNumber, ComplexNumber[]> getAdjointFunction(double t) {
    throw new NotImplementedException();
  }

}
