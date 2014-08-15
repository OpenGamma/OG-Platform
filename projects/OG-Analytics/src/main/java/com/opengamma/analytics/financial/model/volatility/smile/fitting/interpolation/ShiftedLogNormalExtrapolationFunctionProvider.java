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

  private static final String s_exception = "Exception"; // OG-Financial's BlackVolatilitySurfacePropertyNamesAndValues.EXCEPTION_SPLINE_EXTRAPOLATOR_FAILURE; 
  private static final String s_flat = "Flat"; // OG-Financial's BlackVolatilitySurfacePropertyNamesAndValues.FLAT_SPLINE_EXTRAPOLATOR_FAILURE;
  private static final String s_quiet = "Quiet"; // OG-Financial's BlackVolatilitySurfacePropertyNamesAndValues.QUIET_SPLINE_EXTRAPOLATOR_FAILURE;

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
    _extrapolatorFailureBehaviour = extrapolatorFailureBehaviour;
  }

  @Override
  public Function1D<Double, Double> getExtrapolationFunction(final SABRFormulaData sabrDataLow, final SABRFormulaData sabrDataHigh,
      final VolatilityFunctionProvider<SABRFormulaData> volatilityFunction, final double forward, final double expiry, final double cutOffStrikeLow, final double cutOffStrikeHigh) {

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

    // Volatility gradient (dVol/dStrike) of interpolator
    final Function1D<Double, Double> dSigmaDxLow = DIFFERENTIATOR.differentiate(interpFuncLow, domain);
    final Function1D<Double, Double> dSigmaDxHigh = DIFFERENTIATOR.differentiate(interpFuncHigh, domain);

    // The 'quiet' method reduces smile if the volatility gradient is either out of bounds of ShiftedLognormal model, or if root-finder fails to find solution
    if (_extrapolatorFailureBehaviour.equalsIgnoreCase(s_quiet)) {
      ArgumentChecker.isTrue(cutOffStrikeLow <= forward, "Cannot do left tail extrapolation when the lowest strike ({}) is greater than the forward ({})", cutOffStrikeLow, forward);
      ArgumentChecker.isTrue(cutOffStrikeHigh >= forward, "Cannot do right tail extrapolation when the highest strike ({}) is less than the forward ({})", cutOffStrikeHigh, forward);
      shiftLnVolHighTail = TAIL_FITTER.fitVolatilityAndGradRecursivelyByReducingSmile(forward, cutOffStrikeHigh, interpFuncHigh.evaluate(cutOffStrikeHigh), dSigmaDxHigh.evaluate(cutOffStrikeHigh),
          expiry);
      shiftLnVolLowTail = TAIL_FITTER.fitVolatilityAndGradRecursivelyByReducingSmile(forward, cutOffStrikeLow, interpFuncLow.evaluate(cutOffStrikeLow), dSigmaDxLow.evaluate(cutOffStrikeLow), expiry);
      // 'Exception' will throw an exception if it fails to fit to target vol and gradient provided by interpolating function at the boundary
    } else if (_extrapolatorFailureBehaviour.equalsIgnoreCase(s_exception)) {
      ArgumentChecker.isTrue(cutOffStrikeLow <= forward, "Cannot do left tail extrapolation when the lowest strike ({}) is greater than the forward ({})", cutOffStrikeLow, forward);
      ArgumentChecker.isTrue(cutOffStrikeHigh >= forward, "Cannot do right tail extrapolation when the highest strike ({}) is less than the forward ({})", cutOffStrikeHigh, forward);
      shiftLnVolHighTail = TAIL_FITTER.fitVolatilityAndGrad(forward, cutOffStrikeHigh, interpFuncHigh.evaluate(cutOffStrikeHigh), dSigmaDxHigh.evaluate(cutOffStrikeHigh), expiry);
      shiftLnVolLowTail = TAIL_FITTER.fitVolatilityAndGrad(forward, cutOffStrikeLow, interpFuncLow.evaluate(cutOffStrikeLow), dSigmaDxLow.evaluate(cutOffStrikeLow), expiry);
      // 'Flat' will simply return the target volatility at the boundary. Thus the target gradient is zero.
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

}
