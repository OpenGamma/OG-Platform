/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import java.util.List;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.SABRExtrapolationRightFunction;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.function.Function1D;

/**
 * Use {@link SABRExtrapolationRightFunction} controlled by mu if the strike value is greater than the strike cutoff, otherwise use {@link SmileInterpolatorSABR}. 
 */
public class SmileInterpolatorSABRWithRightExtrapolation extends SmileInterpolatorSABR {

  private final double _cutOffStrike;
  private final double _mu;

  /**
   * Using default SABR smile interpolation, {@link SABRHaganVolatilityFunction}
   * @param cutOffStrike The strike cutoff
   * @param mu The mu
   */
  public SmileInterpolatorSABRWithRightExtrapolation(final double cutOffStrike, final double mu) {
    _cutOffStrike = cutOffStrike;
    _mu = mu;
  }

  /**
   * Specifying volatility function
   * @param model The volatility function
   * @param cutOffStrike The strike cutoff
   * @param mu The mu
   */
  public SmileInterpolatorSABRWithRightExtrapolation(final VolatilityFunctionProvider<SABRFormulaData> model, final double cutOffStrike, final double mu) {
    super(model);
    _cutOffStrike = cutOffStrike;
    _mu = mu;
  }

  /**
   * Specifying SABR smile interpolation parameters 
   * @param seed The seed
   * @param model The volatility model
   * @param beta The beta
   * @param weightFunction The weight function
   * @param cutOffStrike The strike cutoff
   * @param mu The mu
   */
  public SmileInterpolatorSABRWithRightExtrapolation(final int seed, final VolatilityFunctionProvider<SABRFormulaData> model, final double beta, final WeightingFunction weightFunction,
      final double cutOffStrike, final double mu) {
    super(seed, model, beta, weightFunction);
    _cutOffStrike = cutOffStrike;
    _mu = mu;
  }

  @Override
  public Function1D<Double, Double> getVolatilityFunction(final double forward, final double[] strikes, final double expiry, final double[] impliedVols) {

    final List<SABRFormulaData> modelParams;
    SABRFormulaData sabrData;
    final int n;

    if (getModel() instanceof SABRHaganVolatilityFunction) {
      modelParams = getFittedModelParameters(forward, strikes, expiry, impliedVols);
      n = strikes.length;
      sabrData = modelParams.get(n - 3);
    } else {
      n = 1;
      modelParams = getFittedModelParametersGlobal(forward, strikes, expiry, impliedVols);
      sabrData = modelParams.get(0);
    }
    final SABRExtrapolationRightFunction sabrExtrapolation = new SABRExtrapolationRightFunction(forward, sabrData, _cutOffStrike, expiry, _mu, getModel());

    return new Function1D<Double, Double>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double strike) {
        EuropeanVanillaOption option = new EuropeanVanillaOption(strike, expiry, true);

        if (strike < _cutOffStrike) {
          final Function1D<SABRFormulaData, Double> volFunc = getModel().getVolatilityFunction(option, forward);
          if (n == 1) {
            return volFunc.evaluate(modelParams.get(0));
          }
          final int index = SurfaceArrayUtils.getLowerBoundIndex(strikes, strike);
          if (index == 0) {
            return volFunc.evaluate(modelParams.get(0));
          }
          if (index >= n - 2) {
            return volFunc.evaluate(modelParams.get(n - 3));
          }
          final double w = getWeightingFunction().getWeight(strikes, index, strike);
          if (w == 1) {
            return volFunc.evaluate(modelParams.get(index - 1));
          } else if (w == 0) {
            return volFunc.evaluate(modelParams.get(index));
          } else {
            return w * volFunc.evaluate(modelParams.get(index - 1)) + (1 - w) * volFunc.evaluate(modelParams.get(index));
          }
        }

        double price = sabrExtrapolation.price(option);
        return BlackFormulaRepository.impliedVolatility(price, forward, strike, expiry, option.isCall());
      }
    };
  }
}
