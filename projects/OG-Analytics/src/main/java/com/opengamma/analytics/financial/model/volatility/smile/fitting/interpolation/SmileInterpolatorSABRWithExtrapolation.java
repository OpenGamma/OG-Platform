/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import java.util.List;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;

/**
 * SABR smile interpolator with left and right extrapolation by using {@link SmileExtrapolationFunctionSABRProvider}
 */
public class SmileInterpolatorSABRWithExtrapolation extends SmileInterpolatorSABR {
  private final SmileExtrapolationFunctionSABRProvider _extrapolationFunctionProvider;

  /**
   * Constructor specifying extrapolation method
   * @param extrapolationFunctionProvider The extrapolation method
   */
  public SmileInterpolatorSABRWithExtrapolation(
      final SmileExtrapolationFunctionSABRProvider extrapolationFunctionProvider) {
    ArgumentChecker.notNull(extrapolationFunctionProvider, "extrapolationFunctionProvider");
    _extrapolationFunctionProvider = extrapolationFunctionProvider;
  }

  /**
   * Constructor specifying volatility formula extrapolation method
   * @param formula The volatility formula
   * @param extrapolationFunctionProvider The extrapolation method
   */
  public SmileInterpolatorSABRWithExtrapolation(final VolatilityFunctionProvider<SABRFormulaData> formula,
      final SmileExtrapolationFunctionSABRProvider extrapolationFunctionProvider) {
    super(formula);
    ArgumentChecker.notNull(extrapolationFunctionProvider, "extrapolationFunctionProvider");
    _extrapolationFunctionProvider = extrapolationFunctionProvider;
  }

  /**
   * Constructor specifying seed, volatility formula, beta, weight function and extrapolation method, 
   * see {@link SmileInterpolatorSABR} for the parameter detail
   * @param seed The seed 
   * @param formula The volatility formula
   * @param beta The beta parameter
   * @param weightFunction The weight function 
   * @param extrapolationFunctionProvider The extrapolation method
   */
  public SmileInterpolatorSABRWithExtrapolation(final int seed,
      final VolatilityFunctionProvider<SABRFormulaData> formula, final double beta,
      final WeightingFunction weightFunction, final SmileExtrapolationFunctionSABRProvider extrapolationFunctionProvider) {
    super(seed, formula, beta, weightFunction);
    ArgumentChecker.notNull(extrapolationFunctionProvider, "extrapolationFunctionProvider");
    _extrapolationFunctionProvider = extrapolationFunctionProvider;
  }

  @Override
  public Function1D<Double, Double> getVolatilityFunction(final double forward, final double[] strikes,
      final double expiry, final double[] impliedVols) {
    ArgumentChecker.notNull(strikes, "strikes");
    ArgumentChecker.notNull(impliedVols, "impliedVols");
    ArgumentChecker.isTrue(strikes.length == impliedVols.length, "strikes and impliedVols should have the same length");

    final int nStrikes = strikes.length;
    final double cutOffStrikeLow = strikes[0];
    final double cutOffStrikeHigh = strikes[nStrikes - 1];

    SABRFormulaData sabrDataLow;
    SABRFormulaData sabrDataHigh;
    final List<SABRFormulaData> modelParams;
    final int n;

    List<SABRFormulaData> modelParamsTmp;
    int nTmp;
    try {
      modelParamsTmp = getFittedModelParameters(forward, strikes, expiry, impliedVols);
      nTmp = strikes.length;
      int ref = Math.max(0, nTmp - 3);
      sabrDataLow = modelParamsTmp.get(0);
      sabrDataHigh = modelParamsTmp.get(ref);
    } catch (final Exception e) { //try global fit if local fit failed
      nTmp = 1;
      modelParamsTmp = getFittedModelParametersGlobal(forward, strikes, expiry, impliedVols);
      sabrDataLow = modelParamsTmp.get(0);
      sabrDataHigh = modelParamsTmp.get(0);
    }
    modelParams = modelParamsTmp;
    n = nTmp;

    final Function1D<Double, Double> interpFunc;
    if (n == 1) {
      interpFunc = new Function1D<Double, Double>() {
        @SuppressWarnings("synthetic-access")
        @Override
        public Double evaluate(final Double strike) {
          final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, expiry, true);
          final Function1D<SABRFormulaData, Double> volFunc = getModel().getVolatilityFunction(option, forward);
          return volFunc.evaluate(modelParams.get(0));
        }
      };
    } else {
      interpFunc = getVolatilityFunctionFromModelParameters(forward, strikes, expiry, modelParams);
    }

    final Function1D<Double, Double> extrapFunc = _extrapolationFunctionProvider.getExtrapolationFunction(sabrDataLow,
        sabrDataHigh, getModel(), forward, expiry, cutOffStrikeLow, cutOffStrikeHigh);

    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double strike) {
        ArgumentChecker.notNegative(strike, "strike");
        if (strike < cutOffStrikeLow) {
          return extrapFunc.evaluate(strike);
        }
        if (strike > cutOffStrikeHigh) {
          return extrapFunc.evaluate(strike);
        }
        return interpFunc.evaluate(strike);
      }
    };
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result +
        ((_extrapolationFunctionProvider == null) ? 0 : _extrapolationFunctionProvider.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (!(obj instanceof SmileInterpolatorSABRWithExtrapolation)) {
      return false;
    }
    SmileInterpolatorSABRWithExtrapolation other = (SmileInterpolatorSABRWithExtrapolation) obj;
    if (_extrapolationFunctionProvider == null) {
      if (other._extrapolationFunctionProvider != null) {
        return false;
      }
    } else if (!_extrapolationFunctionProvider.equals(other._extrapolationFunctionProvider)) {
      return false;
    }
    return true;
  }

}
