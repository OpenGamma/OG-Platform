/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.SABRExtrapolationRightFunction;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;

/**
 * Left and right extrapolation for SABR smile interpolation by using the method {@link SABRExtrapolationRightFunction}
 */
public class BenaimDodgsonKainthExtrapolationFunctionProvider extends SmileExtrapolationFunctionSABRProvider {

  private final double _muLow;
  private final double _muHigh;

  /**
   * Constructor specifying parameters which control the fatness of tails
   * @param muLow The parameter for the left tail, must be positive
   * @param muHigh The parameter for the right tail, must be positive
   */
  public BenaimDodgsonKainthExtrapolationFunctionProvider(final double muLow, final double muHigh) {
    ArgumentChecker.isTrue(muLow > 0.0, "muLow should be positive");
    ArgumentChecker.isTrue(muHigh > 0.0, "muHigh should be positive");
    _muLow = muLow;
    _muHigh = muHigh;
  }

  @Override
  public Function1D<Double, Double> getExtrapolationFunction(final SABRFormulaData sabrDataLow, final SABRFormulaData sabrDataHigh,
      final VolatilityFunctionProvider<SABRFormulaData> volatilityFunction, final double forward, final double expiry, final double cutOffStrikeLow, final double cutOffStrikeHigh) {

    final SABRExtrapolationRightFunction sabrLeftExtrapolation = new SABRExtrapolationRightFunction(forward, sabrDataLow, cutOffStrikeLow, expiry, -_muLow, volatilityFunction);
    final SABRExtrapolationRightFunction sabrRightExtrapolation = new SABRExtrapolationRightFunction(forward, sabrDataHigh, cutOffStrikeHigh, expiry, _muHigh, volatilityFunction);

    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double strike) {
        if (strike < cutOffStrikeLow) {
          EuropeanVanillaOption option = new EuropeanVanillaOption(strike, expiry, false);
          double price = sabrLeftExtrapolation.price(option);
          return BlackFormulaRepository.impliedVolatility(price, forward, strike, expiry, option.isCall());
        }
        if (strike > cutOffStrikeHigh) {
          EuropeanVanillaOption option = new EuropeanVanillaOption(strike, expiry, true);
          double price = sabrRightExtrapolation.price(option);
          return BlackFormulaRepository.impliedVolatility(price, forward, strike, expiry, option.isCall());
        }
        throw new OpenGammaRuntimeException("Use smile interpolation method for cutOffStrikeLow <= strike <= cutOffStrikeHigh");
      }
    };
  }

}
