/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.differentiation.ScalarFirstOrderDifferentiator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;

/**
 * Left and right extrapolation for SABR smile interpolation by using the shifted lognormal model
 */
public class ShiftedLogNormalExtrapolationFunctionProvider extends SmileExtrapolationFunctionSABRProvider {

  private static final ShiftedLogNormalTailExtrapolationFitter TAIL_FITTER = new ShiftedLogNormalTailExtrapolationFitter();
  private static final ScalarFirstOrderDifferentiator DIFFERENTIATOR = new ScalarFirstOrderDifferentiator();

  /*
   * Failure behaviors. See {@link SmileInterpolatorSpline} for detail.
   */
  private static final String s_exception = "Exception";
  private static final String s_flat = "Flat";
  private static final String s_quiet = "Quiet";
  private final String _extrapolatorFailureBehaviour;

  /**
   * Default constructor, throwing exception if fitting fails 
   */
  public ShiftedLogNormalExtrapolationFunctionProvider() {
    _extrapolatorFailureBehaviour = s_exception;
  }

  /**
   * Constructor specifying the behavior when fitting fails 
   * @param extrapolatorFailureBehaviour The expected behavior
   */
  public ShiftedLogNormalExtrapolationFunctionProvider(final String extrapolatorFailureBehaviour) {
    ArgumentChecker.notNull(extrapolatorFailureBehaviour, "extrapolatorFailureBehaviour");
    _extrapolatorFailureBehaviour = extrapolatorFailureBehaviour;
  }

  @Override
  public Function1D<Double, Double> getExtrapolationFunction(final SABRFormulaData sabrDataLow, final SABRFormulaData sabrDataHigh,
      final VolatilityFunctionProvider<SABRFormulaData> volatilityFunction, final double forward, final double expiry, final double cutOffStrikeLow, final double cutOffStrikeHigh) {
    ArgumentChecker.notNull(sabrDataLow, "sabrDataLow");
    ArgumentChecker.notNull(sabrDataHigh, "sabrDataHigh");
    ArgumentChecker.notNull(volatilityFunction, "volatilityFunction");
    ArgumentChecker.isTrue(0.0 < cutOffStrikeLow, "cutOffStrikeLow should be positive");

    final Function1D<Double, Boolean> domain = new Function1D<Double, Boolean>() {
      @Override
      public Boolean evaluate(final Double k) {
        return k >= cutOffStrikeLow && k <= cutOffStrikeHigh;
      }
    };

    final Function1D<Double, Double> interpFuncLow = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double strike) {
        EuropeanVanillaOption option = new EuropeanVanillaOption(strike, expiry, true);
        final Function1D<SABRFormulaData, Double> volFunc = volatilityFunction.getVolatilityFunction(option, forward);
        return volFunc.evaluate(sabrDataLow);
      }
    };

    final Function1D<Double, Double> interpFuncHigh = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double strike) {
        EuropeanVanillaOption option = new EuropeanVanillaOption(strike, expiry, true);
        final Function1D<SABRFormulaData, Double> volFunc = volatilityFunction.getVolatilityFunction(option, forward);
        return volFunc.evaluate(sabrDataHigh);
      }
    };

    final double[] shiftLnVolLowTail;
    final double[] shiftLnVolHighTail;

    final Function1D<Double, Double> dSigmaDxLow = DIFFERENTIATOR.differentiate(interpFuncLow, domain);
    final Function1D<Double, Double> dSigmaDxHigh = DIFFERENTIATOR.differentiate(interpFuncHigh, domain);

    if (_extrapolatorFailureBehaviour.equalsIgnoreCase(s_quiet)) {
      ArgumentChecker.isTrue(cutOffStrikeLow <= forward, "Cannot do left tail extrapolation when the lowest strike ({}) is greater than the forward ({})", cutOffStrikeLow, forward);
      ArgumentChecker.isTrue(cutOffStrikeHigh >= forward, "Cannot do right tail extrapolation when the highest strike ({}) is less than the forward ({})", cutOffStrikeHigh, forward);
      shiftLnVolHighTail = TAIL_FITTER.fitVolatilityAndGradRecursivelyByReducingSmile(forward, cutOffStrikeHigh, interpFuncHigh.evaluate(cutOffStrikeHigh), dSigmaDxHigh.evaluate(cutOffStrikeHigh),
          expiry);
      shiftLnVolLowTail = TAIL_FITTER.fitVolatilityAndGradRecursivelyByReducingSmile(forward, cutOffStrikeLow, interpFuncLow.evaluate(cutOffStrikeLow), dSigmaDxLow.evaluate(cutOffStrikeLow), expiry);
    } else if (_extrapolatorFailureBehaviour.equalsIgnoreCase(s_exception)) {
      ArgumentChecker.isTrue(cutOffStrikeLow <= forward, "Cannot do left tail extrapolation when the lowest strike ({}) is greater than the forward ({})", cutOffStrikeLow, forward);
      ArgumentChecker.isTrue(cutOffStrikeHigh >= forward, "Cannot do right tail extrapolation when the highest strike ({}) is less than the forward ({})", cutOffStrikeHigh, forward);
      shiftLnVolHighTail = TAIL_FITTER.fitVolatilityAndGrad(forward, cutOffStrikeHigh, interpFuncHigh.evaluate(cutOffStrikeHigh), dSigmaDxHigh.evaluate(cutOffStrikeHigh), expiry);
      shiftLnVolLowTail = TAIL_FITTER.fitVolatilityAndGrad(forward, cutOffStrikeLow, interpFuncLow.evaluate(cutOffStrikeLow), dSigmaDxLow.evaluate(cutOffStrikeLow), expiry);
    } else if (_extrapolatorFailureBehaviour.equalsIgnoreCase(s_flat)) {
      shiftLnVolHighTail = TAIL_FITTER.fitVolatilityAndGrad(forward, cutOffStrikeHigh, interpFuncHigh.evaluate(cutOffStrikeHigh), 0.0, expiry);
      shiftLnVolLowTail = TAIL_FITTER.fitVolatilityAndGrad(forward, cutOffStrikeLow, interpFuncLow.evaluate(cutOffStrikeLow), 0.0, expiry);
    } else {
      throw new OpenGammaRuntimeException("Unrecognized _extrapolatorFailureBehaviour. Looking for one of Exception, Quiet, or Flat");
    }

    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double strike) {
        if (strike < cutOffStrikeLow) {
          return ShiftedLogNormalTailExtrapolation.impliedVolatility(forward, strike, expiry, shiftLnVolLowTail[0], shiftLnVolLowTail[1]);
        }
        if (strike > cutOffStrikeHigh) {
          return ShiftedLogNormalTailExtrapolation.impliedVolatility(forward, strike, expiry, shiftLnVolHighTail[0], shiftLnVolHighTail[1]);
        }
        throw new OpenGammaRuntimeException("Use smile interpolation method for cutOffStrikeLow <= strike <= cutOffStrikeHigh");
      }
    };
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_extrapolatorFailureBehaviour == null) ? 0 : _extrapolatorFailureBehaviour.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ShiftedLogNormalExtrapolationFunctionProvider)) {
      return false;
    }
    ShiftedLogNormalExtrapolationFunctionProvider other = (ShiftedLogNormalExtrapolationFunctionProvider) obj;
    if (_extrapolatorFailureBehaviour == null) {
      if (other._extrapolatorFailureBehaviour != null) {
        return false;
      }
    } else if (!_extrapolatorFailureBehaviour.equals(other._extrapolatorFailureBehaviour)) {
      return false;
    }
    return true;
  }

}
