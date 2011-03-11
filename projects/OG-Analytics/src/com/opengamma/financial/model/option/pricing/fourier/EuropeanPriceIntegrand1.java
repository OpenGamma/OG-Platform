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

import com.opengamma.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.number.ComplexNumber;

/**
 * 
 */
public class EuropeanPriceIntegrand1 {
  private final CharacteristicExponent1 _ce;
  private final double _alpha;
  private final boolean _useVarianceReduction;

  public EuropeanPriceIntegrand1(final CharacteristicExponent1 ce, final double alpha, final boolean useVarianceReduction) {
    _ce = new MeanCorrectedCharacteristicExponent1(ce);
    _alpha = alpha;
    _useVarianceReduction = useVarianceReduction;
  }

  public Function1D<Double, Double> getFunction(final BlackFunctionData data, final EuropeanVanillaOption option) {
    final double t = option.getTimeToExpiry();
    final Function1D<ComplexNumber, ComplexNumber> characteristicFunction = _ce.getFunction(t);
    final double k = Math.log(option.getStrike() / data.getForward());
    final double blackVol = data.getSimga();
    final CharacteristicExponent1 gaussian = (_useVarianceReduction ? new GaussianCharacteristicExponent1(-0.5 * blackVol * blackVol, blackVol) : null);
    final Function1D<ComplexNumber, ComplexNumber> gaussianFunction = _useVarianceReduction ? gaussian.getFunction(t) : null;
    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        final ComplexNumber res = getIntegrand(x, characteristicFunction, gaussianFunction, k);
        return res.getReal();
      }
    };
  }

  public ComplexNumber getIntegrand(final double x, final Function1D<ComplexNumber, ComplexNumber> ce, final Function1D<ComplexNumber, ComplexNumber> gaussian, final double k) {
    final ComplexNumber z = new ComplexNumber(x, -1 - _alpha);
    final ComplexNumber num1 = exp(add(new ComplexNumber(0, -x * k), ce.evaluate(z)));
    final ComplexNumber denom = new ComplexNumber(_alpha * (1 + _alpha) - x * x, (2 * _alpha + 1) * x);
    final ComplexNumber res = _useVarianceReduction ? divide(subtract(num1, exp(add(new ComplexNumber(0, -x * k), gaussian.evaluate(z)))), denom) : divide(num1, denom);
    return res;
  }

  public CharacteristicExponent1 getCharacteristicExponent() {
    return _ce;
  }

  public double getAlpha() {
    return _alpha;
  }

  public boolean useVarianceReduction() {
    return _useVarianceReduction;
  }
}
