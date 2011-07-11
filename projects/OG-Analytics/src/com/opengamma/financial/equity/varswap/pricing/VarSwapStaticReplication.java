/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.varswap.pricing;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.equity.varswap.derivative.VarianceSwap;
import com.opengamma.financial.model.volatility.BlackFormula;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.integration.Integrator1D;
import com.opengamma.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.util.tuple.DoublesPair;

/**
 * We construct a model independent method to price variance as a static replication
 * of an (in)finite sum of call and put option prices on the underlying.
 * We assume the existence of a smooth function of these option prices / Implied volatilities.
 * The portfolio weighting is 1/k^2. As such, this method is especially sensitive to strike near zero,
 * so we allow the caller to override the Volatilities below a cutoff point (defined as a fraction of the forward rate).
 * We then fit a ShiftedLognormal model to the price of the linear (call) and digital (call spread) at the cutoff.
 * Note that this is not intended to handle large payment delays between last observation date and payment. No convexity adjustment has been applied. 
 * 
 *  Note:'moneyness', the parameterisation of Strike space,  is defined relative to the forward. moneyness := strike/fwd => atm moneyness = 1    
 */
public class VarSwapStaticReplication {

  // Vol Extrapolation 
  private final Double _strikeCutoff; // Lowest interpolated strike. ShiftedLognormal hits Put(_strikeCutoff)
  private final Double _strikeSpread; // Match derivative near cutoff by also fitting to Put(_strikeCutoff + _strikeSpread)
  private final boolean _cutoffProvided; // False if both the above are null 

  // Integration parameters
  private final double _lowerBound; // Integrate over strikes in 'moneyness' or 'relative strike', defined as strike/forward. Start close to zero
  private final double _upperBound; // Upper bound in 'moneyness' (K/F). This represents 'large'
  private final Integrator1D<Double, Double> _integrator;

  /**
   * Default constructor with sensible inputs.
   */
  public VarSwapStaticReplication() {
    _lowerBound = 1e-4; // almost zero
    _upperBound = 5.0; // multiple of the atm forward
    _integrator = new RungeKuttaIntegrator1D();
    _strikeCutoff = 0.25; // TODO Choose how caller tells impliedVariance not to use ShiftedLognormal..
    _strikeSpread = 0.05;
    _cutoffProvided = true;
  }

  public VarSwapStaticReplication(final double lowerBound, final double upperBound, final Integrator1D<Double, Double> integrator, final Double strikeCutoff, final Double strikeSpread) {
    _lowerBound = lowerBound;
    _upperBound = upperBound;
    _integrator = integrator;

    _strikeCutoff = strikeCutoff;
    _strikeSpread = strikeSpread;
    if (_strikeCutoff == null || _strikeSpread == null) {
      Validate.isTrue(_strikeCutoff == null && _strikeSpread == null, "Both a cutoff moneyness and a spread must be provided to specify where to tie ShiftedLognormal.");
      _cutoffProvided = false;
    } else {
      Validate.isTrue(strikeCutoff < 1, "strikeCutoff should be less than the forward, i.e. less than one. Note its defined as moneyness: strike / fwd.");
      _cutoffProvided = true;
    }
  }

  public double presentValue(final VarianceSwap deriv, final VarianceSwapDataBundle market) {
    Validate.notNull(deriv, "VarianceSwap deriv");
    Validate.notNull(market, "VarianceSwapDataBundle market");

    // TODO Can we assert that we are double counting days?
    final double realizedVar = new RealizedVariance().evaluate(deriv); // Realized variance of log returns already observed
    final double remainingVar = impliedVariance(deriv, market); // Remaining variance implied by option prices
    final double finalPayment = deriv.getVarNotional() * (realizedVar + remainingVar - deriv.getVarStrike());
    // FIXME Case !!! Confirm relative scaling of past vs future var, and annualization

    final double df = market.getDiscountCurve().getDiscountFactor(deriv.getTimeToSettlement());
    return df * finalPayment;

  }

  /**
   * Compute the variance of the forward underlying until expiry implied by the market volatility surface 
   * 
   * @param deriv VarianceSwap derivative to be priced
   * @param market VarianceSwapDataBundle containing volatility surface, forward underlying, and funding curve
   * @return presentValue of the *remaining* variance in the swap. 
   */
  public double impliedVariance(final VarianceSwap deriv, final VarianceSwapDataBundle market) {
    Validate.notNull(deriv, "VarianceSwap deriv");
    Validate.notNull(market, "VarianceSwapDataBundle market");

    // 1. Unpack Market data
    final double expiry = deriv.getTimeToObsEnd();
    final double fwd = market.getForwardUnderlying();
    final VolatilitySurface vsurf = market.getVolatilitySurface();

    if (expiry < 1E-4) { // If expiry occurs in less than an hour or so, return 0
      return 0;
    }

    // 2. Fit the leftExtrapolator
    final ShiftedLognormalVolModel leftExtrapolator;
    if (_cutoffProvided) {
      leftExtrapolator = new ShiftedLognormalVolModel(fwd, expiry, vsurf, _strikeCutoff, _strikeCutoff + _strikeSpread);
    } else {
      leftExtrapolator = null;
    }

    // 3. Define the hedging portfolio
    // The position to hold in each otmOption(k) = 2 / strike^2, 
    // where otmOption is a call if k > fwd and a put otherwise
    final Function1D<Double, Double> otmOptionAndWeight = new Function1D<Double, Double>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double moneyness) {

        final double strike = moneyness * fwd;
        final boolean isCall = moneyness > 1; // if strike > fwd, the call is out of the money..

        final double weight = 2 / (fwd * moneyness * moneyness);
        double otmPrice;

        if (_cutoffProvided && moneyness < _strikeCutoff) { // Extrapolate with ShiftedLognormal
          otmPrice = leftExtrapolator.priceFromRelativeStrike(moneyness);
        } else {
          final DoublesPair coord = DoublesPair.of(expiry, strike);
          final double vol = vsurf.getVolatility(coord);
          otmPrice = new BlackFormula(fwd, moneyness * fwd, expiry, vol, null, isCall).computePrice();
        }

        return otmPrice * weight;
      }
    };

    // 4. Compute variance hedge by integrating positions over all strikes
    final double variance = _integrator.integrate(otmOptionAndWeight, _lowerBound, _upperBound);
    return variance;
  }
}
