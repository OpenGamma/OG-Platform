/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.variance.pricing;

import com.opengamma.financial.equity.variance.VarianceSwapDataBundle2;
import com.opengamma.financial.model.option.pricing.analytic.formula.BlackImpliedStrikeFromDeltaFunction;
import com.opengamma.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.financial.model.volatility.surface.BlackVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.Delta;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.integration.Integrator1D;
import com.opengamma.math.minimization.BrentMinimizer1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.CompareUtils;

/**
 * 
 */
public class VarianceSwapStaticReplicationDelta extends VarianceSwapStaticReplication2<Delta> {

  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  public VarianceSwapStaticReplicationDelta() {
    super(EPS,1-EPS);
  }

  public VarianceSwapStaticReplicationDelta(final double lowerBound, final double upperBound, final Integrator1D<Double, Double> integrator,
      final Delta cutoffLevel, final Delta cutoffSpread) {
    super(lowerBound, upperBound, integrator, cutoffLevel, cutoffSpread);
  }

  @Override
  protected double impliedVarianceFromSpot(final double expiry, VarianceSwapDataBundle2<Delta> market) {

    // 1. Unpack Market data
    final double fwd = market.getForwardCurve().getForward(expiry);
    final BlackVolatilitySurface<Delta> volSurf = market.getVolatilitySurface();

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
      Delta temp = _sa.subtract(_cutoffLevel, _cutoffSpread);
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
      public Double evaluate(final Double delta) {
        Delta ddelta = new Delta(delta);
        final double vol = volSurf.getVolatility(expiry, ddelta);
        final double strike = BlackImpliedStrikeFromDeltaFunction.impliedStrike(delta, true, fwd, expiry, vol);
        boolean isCall = strike >= fwd;

        final double eps = 1e-5;
        final Delta deltaEPS = new Delta(eps);
        double dSigmaDDelta;
        if (delta < eps) {
          final double volUp = volSurf.getVolatility(expiry, _sa.add(ddelta, deltaEPS));
          dSigmaDDelta = (volUp - vol) / eps;
        } else if (delta > 1 - eps) {
          final double volDown = volSurf.getVolatility(expiry, _sa.subtract(ddelta, deltaEPS));
          dSigmaDDelta = (vol - volDown) / eps;
        } else {
          final double volUp = volSurf.getVolatility(expiry, _sa.add(ddelta, deltaEPS));
          final double volDown = volSurf.getVolatility(expiry, _sa.subtract(ddelta, deltaEPS));
          dSigmaDDelta = (volUp - volDown) / 2 / eps;
        }
        double d1 = NORMAL.getInverseCDF(delta);
        double rootT = Math.sqrt(expiry);
        double weight = 2 * (vol * rootT / NORMAL.getPDF(d1) + dSigmaDDelta * (d1 * rootT - vol * expiry)) / strike;

        double otmPrice;
        if (_cutoffProvided && strike < cutoffStrike) { // Extrapolate with ShiftedLognormal
          otmPrice = leftExtrapolator.priceFromFixedStrike(strike);
        } else {
          otmPrice = BlackFormulaRepository.price(fwd, strike, expiry, vol, isCall);
        }
        return weight * otmPrice;
      }
    };

    //      // 4. Compute variance hedge by integrating positions over all strikes
    double variance = _integrator.integrate(otmOptionAndWeight, _lowerBound, _upperBound);
    return variance / expiry;
  }

}
