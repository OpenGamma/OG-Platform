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
  public SmileInterpolatorSABRWithExtrapolation(final SmileExtrapolationFunctionSABRProvider extrapolationFunctionProvider) {
    ArgumentChecker.notNull(extrapolationFunctionProvider, "extrapolationFunctionProvider");
    _extrapolationFunctionProvider = extrapolationFunctionProvider;
  }

  /**
   * Constructor specifying volatility formula extrapolation method
   * @param formula The volatility formula
   * @param extrapolationFunctionProvider The extrapolation method
   */
  public SmileInterpolatorSABRWithExtrapolation(final VolatilityFunctionProvider<SABRFormulaData> formula, final SmileExtrapolationFunctionSABRProvider extrapolationFunctionProvider) {
    super(formula);
    ArgumentChecker.notNull(extrapolationFunctionProvider, "extrapolationFunctionProvider");
    _extrapolationFunctionProvider = extrapolationFunctionProvider;
  }

  /**
   * Constructor specifying seed, volatility formula, beta, weight function and extrapolation method, see {@link SmileInterpolatorSABR} for the parameter detail
   * @param seed The seed 
   * @param formula The volatility formula
   * @param beta The beta parameter
   * @param weightFunction The weight function 
   * @param extrapolationFunctionProvider The extrapolation method
   */
  public SmileInterpolatorSABRWithExtrapolation(final int seed, final VolatilityFunctionProvider<SABRFormulaData> formula, final double beta, final WeightingFunction weightFunction,
      final SmileExtrapolationFunctionSABRProvider extrapolationFunctionProvider) {
    super(seed, formula, beta, weightFunction);
    ArgumentChecker.notNull(extrapolationFunctionProvider, "extrapolationFunctionProvider");
    _extrapolationFunctionProvider = extrapolationFunctionProvider;
  }

  @Override
  public Function1D<Double, Double> getVolatilityFunction(final double forward, final double[] strikes, final double expiry, final double[] impliedVols) {
    ArgumentChecker.notNull(strikes, "strikes");
    ArgumentChecker.notNull(impliedVols, "impliedVols");

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
      sabrDataLow = modelParamsTmp.get(0);
      sabrDataHigh = modelParamsTmp.get(nTmp - 3);
    } catch (final Exception e) { //try global fit if local fit failed
      nTmp = 1;
      modelParamsTmp = getFittedModelParametersGlobal(forward, strikes, expiry, impliedVols);
      sabrDataLow = modelParamsTmp.get(0);
      sabrDataHigh = modelParamsTmp.get(0);
    }
    modelParams = modelParamsTmp;
    n = nTmp;

    final Function1D<Double, Double> interpFunc = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double strike) {
        EuropeanVanillaOption option = new EuropeanVanillaOption(strike, expiry, true);
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
    };

    final Function1D<Double, Double> extrapFunc = _extrapolationFunctionProvider.getExtrapolationFunction(sabrDataLow, sabrDataHigh, getModel(), forward, expiry, cutOffStrikeLow, cutOffStrikeHigh);

    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double strike) {
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
    result = prime * result + ((_extrapolationFunctionProvider == null) ? 0 : _extrapolationFunctionProvider.hashCode());
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
