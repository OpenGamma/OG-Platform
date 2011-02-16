/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.fourier;

import static com.opengamma.math.ComplexMathUtils.add;
import static com.opengamma.math.ComplexMathUtils.divide;
import static com.opengamma.math.ComplexMathUtils.exp;
import static com.opengamma.math.ComplexMathUtils.subtract;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.number.ComplexNumber;

/**
 * 
 */
public class EuropeanPriceIntegrand extends Function1D<Double, Double> {

  private final CharacteristicExponent _ce;

  private final double _alpha;
  private final double _k;
  private final CharacteristicExponent _gaussian;

  public EuropeanPriceIntegrand(final CharacteristicExponent ce, final double alpha, final double forward, final double strike, boolean useVarianceReduction, double blackVol) {

    _ce = new MeanCorrectedCharacteristicExponent(ce);
    _alpha = alpha;
    _k = Math.log(strike / forward);

    _gaussian = (useVarianceReduction ? new GaussianCharacteristicExponent(-0.5 * blackVol * blackVol, blackVol, _ce.getTime()) : null);

  }

  @Override
  public Double evaluate(Double x) {

    ComplexNumber res = getIntegrand(x);
    return res.getReal();
  }

  public ComplexNumber getIntegrand(double x) {
    ComplexNumber z = new ComplexNumber(x, -1 - _alpha);
    ComplexNumber num1 = exp(add(new ComplexNumber(0, -x * _k), _ce.evaluate(z)));
    ComplexNumber num2 = (_gaussian == null ? new ComplexNumber(0.0) : exp(add(new ComplexNumber(0, -x * _k), _gaussian.evaluate(z))));
    ComplexNumber denom = new ComplexNumber(_alpha * (1 + _alpha) - x * x, (2 * _alpha + 1) * x);
    ComplexNumber res = divide(subtract(num1, num2), denom);
    return res;
  }
}
