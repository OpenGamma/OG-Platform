/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.variance.pricing;

import com.opengamma.financial.equity.variance.VarianceSwapDataBundle2;
import com.opengamma.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.Strike;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.integration.Integrator1D;
import com.opengamma.math.minimization.BrentMinimizer1D;
import com.opengamma.util.CompareUtils;

/**
 * 
 */
public class VarianceSwapStaticReplicationStrike extends VarianceSwapStaticReplication2<Strike> {

  public VarianceSwapStaticReplicationStrike() {
    super(EPS, 20);
  }

  public VarianceSwapStaticReplicationStrike(final double lowerBound, final double upperBound, final Integrator1D<Double, Double> integrator,
      final Strike cutoffLevel, final Strike cutoffSpread) {
    super(lowerBound, upperBound, integrator, cutoffLevel, cutoffSpread);
  }

  @Override
  protected double impliedVarianceFromSpot(final double expiry, VarianceSwapDataBundle2<Strike> market) {

    // 1. Unpack Market data
    final double fwd = market.getForwardCurve().getForward(expiry);
    final BlackVolatilitySurface<Strike> volSurf = market.getVolatilitySurface();

    if (expiry < 1E-4) { // If expiry occurs in less than an hour or so, return 0
      return 0;
    }

    /******* Handle strike parameterisation cases separately *******/

    /******* Case 1: BlackVolatilityFixedStrikeSurface *******/

    // 2. Fit the leftExtrapolator to the two target strikes, if provided
    final ShiftedLognormalVolModel leftExtrapolator;
    final double cutoffStrike = _cutoffProvided ? volSurf.getAbsoluteStrike(expiry, _cutoffLevel) : 0.0;
    final Double lowerBoundOfExtrapolator;
    final Double lowerBoundValue;

    if (_cutoffProvided) {
      Strike temp = _sa.add(_cutoffSpread, _cutoffLevel);
      final double secondStrike = volSurf.getAbsoluteStrike(expiry, temp);
      final double cutoffVol = volSurf.getVolatility(expiry, _cutoffLevel);
      final double secondVol = volSurf.getVolatility(expiry, temp);

      // Check for trivial case where cutoff is so low that there's no effective value in the option
      double cutoffPrice = BlackFormulaRepository.price(fwd, cutoffStrike, expiry, cutoffVol, cutoffStrike > fwd);
      if (CompareUtils.closeEquals(cutoffPrice, 0)) {
        leftExtrapolator = new ShiftedLognormalVolModel(fwd, expiry, 0.0, -1.0e6); // Model will price every strike at zero
        lowerBoundOfExtrapolator = 0.0;
        lowerBoundValue = 0.0;
      } else { // The typical case
        leftExtrapolator = new ShiftedLognormalVolModel(fwd, expiry, cutoffStrike, cutoffVol, secondStrike, secondVol);

        // Now, handle behaviour near zero strike. ShiftedLognormalVolModel has non-zero put price for zero strike.
        // What we do is to find the strike, k_min, at which p(k)/k^2 begins to blow up, and fit a quadratic, p(k) = p(k_min) * k^2 / k_min^2
        // This ensures the implied volatility and the integrand are well behaved in the limit k -> 0.
        final Function1D<Double, Double> shiftedLnIntegrand = new Function1D<Double, Double>() {
          @Override
          public Double evaluate(Double strike) {
            return 2.0 * leftExtrapolator.priceFromFixedStrike(strike) / (strike * strike);
          }
        };
        lowerBoundOfExtrapolator = new BrentMinimizer1D().minimize(shiftedLnIntegrand, EPS, EPS, cutoffStrike);
        lowerBoundValue = shiftedLnIntegrand.evaluate(lowerBoundOfExtrapolator);
      }
    } else {
      leftExtrapolator = null;
      lowerBoundOfExtrapolator = 0.0;
      lowerBoundValue = 0.0;
    }

    // 3. Define the hedging portfolio: The position to hold in each otmOption(k) = 2 / strike^2,
    //                                       where otmOption is a call if k > fwd and a put otherwise
    final Function1D<Double, Double> otmOptionAndWeight = new Function1D<Double, Double>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double strike) {
        final boolean isCall = strike > fwd;
        final double weight = 2 / (strike * strike);
        if (_cutoffProvided && strike < cutoffStrike) { // Extrapolate with ShiftedLognormal
          if (strike >= lowerBoundOfExtrapolator) {
            return leftExtrapolator.priceFromFixedStrike(strike) * weight;
          }
          return lowerBoundValue;
        } // Interp/Extrap directly on volSurf
        final double vol = volSurf.getVolatility(expiry, strike);
        final double otmPrice = BlackFormulaRepository.price(fwd, strike, expiry, vol, isCall);
        return otmPrice * weight;
      }
    };

    //      // 4. Compute variance hedge by integrating positions over all strikes
    double variance = _integrator.integrate(otmOptionAndWeight, _lowerBound * fwd, _upperBound * fwd);
    return variance / expiry;
  }

}
