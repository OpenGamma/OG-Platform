/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.math.MathException;

/**
 * A shifted log normal model is one where the forward and volatility in the Black model are adjusted to produce plausible prices for far OTM options.
 * Since there are two free parameters, two prices, or a price and gradient, can be matched - the prices (as viewed by implied volatility) then have sensible extrapolated
 * values (i.e. stays positive and finite). <p>
 * <b>Note</b> this is not a full smile model and should never be used for values crossing ATM (there is a step in implied volatility ATM) <p>
 * Do not confuse with displaced diffusion which has dynamics $\frac{df}{f+\alpha}=\sigma_{\alpha} dW$ and is priced by the Black formula with the replacement 
 * $f\rightarrow f+\alpha \quad k\rightarrow k+\alpha \quad \sigma \rightarrow \sigma_{\alpha}$
 */
public class ShiftedLogNormalTailExtrapolation {

  /**
   * The price of an option under a shifted log normal model
   * @param forward The (actual) forward value of the underlying 
   * @param strike The option strike
   * @param timeToExpiry time-to-expiry 
   * @param isCall true for a call option
   * @param mu The shift in the distribution such that the effective forward = f*exp(mu) 
   * @param theta The volatility of the distribution 
   * @return The price
   */
  public static double price(final double forward, final double strike, final double timeToExpiry, final boolean isCall, final double mu, final double theta) {
    return BlackFormulaRepository.price(forward * Math.exp(mu), strike, timeToExpiry, theta, isCall);
  }

  /**
   * The Black implied volatility under a shifted log normal model
   * @param forward The (actual) forward value of the underlying 
   * @param strike The option strike
   * @param timeToExpiry time-to-expiry 
   * @param mu The shift in the distribution such that the effective forward = f*exp(mu) 
   * @param theta The volatility of the distribution 
   * @return The implied volatility
   */
  public static double impliedVolatility(final double forward, final double strike, final double timeToExpiry, final double mu, final double theta) {
    boolean isCall = strike >= forward;

    if (strike == 0) {
      return theta;
    }
    if (mu == 0) {
      return theta;
    }

    double p = price(forward, strike, timeToExpiry, isCall, mu, theta);
    if (p <= 1e-100) { //TODO this is an arbitrary choice to switch to the approximation here 
      double c = Math.log(strike / forward);
      double a = timeToExpiry / 2;
      double b = (-c + mu - theta * theta * timeToExpiry / 2) / theta;
      double arg = b * b - 4 * a * c;
      if (arg < 0) {
        throw new MathException("cannot solve for sigma");
      }
      double root = Math.sqrt(arg);
      double volGuess = isCall ? (-b - root) / 2 / a : (-b + root) / 2 / a;
      return volGuess;
    }
    if (!isCall && p >= strike) {
      double c = Math.log(strike / forward);
      double a = timeToExpiry / 2;
      double b = (c + mu - theta * theta * timeToExpiry / 2) / theta;
      double arg = b * b - 4 * a * c;
      if (arg < 0) {
        throw new MathException("cannot solve for sigma");
      }
      double root = Math.sqrt(arg);
      double volGuess = isCall ? (-b - root) / 2 / a : (-b + root) / 2 / a;
      return volGuess;
    }

    return BlackFormulaRepository.impliedVolatility(p, forward, strike, timeToExpiry, isCall);

  }

  /**
   * The dual delta (i.e. sensitivity of the price to a change in the strike) of an option under a shifted log normal model
   * @param forward The (actual) forward value of the underlying 
   * @param strike The option strike
   * @param timeToExpiry time-to-expiry 
   * @param isCall true for a call option
   * @param mu The shift in the distribution such that the effective forward = f*exp(mu) 
   * @param theta The volatility of the distribution 
   * @return The dual delta
   */
  public static double dualDelta(final double forward, final double strike, final double timeToExpiry, final boolean isCall, final double mu, final double theta) {
    return BlackFormulaRepository.dualDelta(forward * Math.exp(mu), strike, timeToExpiry, theta, isCall);
  }

  /**
   * The gradient of the smile under a shifted log normal model
   * @param forward The (actual) forward value of the underlying 
   * @param strike The option strike
   * @param timeToExpiry time-to-expiry 
   * @param mu The shift in the distribution such that the effective forward = f*exp(mu) 
   * @param theta The volatility of the distribution 
   * @return The smile gradient 
   */
  public static double dVdK(final double forward, final double strike, final double timeToExpiry, final double mu, final double theta) {
    if (mu == 0.0) {
      return 0.0;
    }
    boolean isCall = strike >= forward;
    double vol = impliedVolatility(forward, strike, timeToExpiry, mu, theta);
    double dd = dualDelta(forward, strike, timeToExpiry, isCall, mu, theta);
    double blackDD = BlackFormulaRepository.dualDelta(forward, strike, timeToExpiry, vol, isCall);
    double blackVega = BlackFormulaRepository.vega(forward, strike, timeToExpiry, vol);
    return (dd - blackDD) / blackVega;
  }

  protected static double dVdK(final double forward, final double strike, final double timeToExpiry, final double mu, final double theta, final double vol) {
    if (mu == 0.0) {
      return 0.0;
    }
    boolean isCall = strike >= forward;
    double dd = dualDelta(forward, strike, timeToExpiry, isCall, mu, theta);
    double blackDD = BlackFormulaRepository.dualDelta(forward, strike, timeToExpiry, vol, isCall);
    double blackVega = BlackFormulaRepository.vega(forward, strike, timeToExpiry, vol);
    return (dd - blackDD) / blackVega;
  }

}
