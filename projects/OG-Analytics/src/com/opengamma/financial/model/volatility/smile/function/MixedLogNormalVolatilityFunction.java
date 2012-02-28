/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.function;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class MixedLogNormalVolatilityFunction extends VolatilityFunctionProvider<MixedLogNormalModelData> {

  @Override
  public Function1D<MixedLogNormalModelData, Double> getVolatilityFunction(final EuropeanVanillaOption option, final double forward) {
    Validate.notNull(option, "option");
    Validate.isTrue(forward >= 0.0, "forward must be greater than zero");

    return new Function1D<MixedLogNormalModelData, Double>() {
      @Override
      public final Double evaluate(final MixedLogNormalModelData data) {
        Validate.notNull(data, "data");
        return getVolatility(option, forward, data);
      }
    };
  }

  public double getVolatility(final EuropeanVanillaOption option, final double forward, final MixedLogNormalModelData data) {
    final double price = getPrice(option, forward, data);
    final double t = option.getTimeToExpiry();
    final double k = option.getStrike();
    final boolean isCall = option.isCall();
    return BlackFormulaRepository.impliedVolatility(price, forward, k, t, isCall);
  }

  public double getPrice(final EuropeanVanillaOption option, final double forward, final MixedLogNormalModelData data) {
    final double[] w = data.getWeights();
    final double[] sigma = data.getVolatilities();
    final double[] rf = data.getRelativeForwards();
    final int n = w.length;
    final double t = option.getTimeToExpiry();
    final double k = option.getStrike();
    final boolean isCall = option.isCall();
    final double kStar = k / forward;
    double sum = 0;
    for (int i = 0; i < n; i++) {
      sum += w[i] * BlackFormulaRepository.price(rf[i], kStar, t, sigma[i], isCall);
    }
    return forward * sum;
  }

}
