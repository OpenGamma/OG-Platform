/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;

/**
 * 
 */
public class ShiftedLogNormalTailExtrapolation {

  public static double price(final double forward, final double strike, final double timeToExpiry, final boolean isCall, final double mu, final double theta) {
    return BlackFormulaRepository.price(forward * Math.exp(mu), strike, timeToExpiry, theta, isCall);
  }

  public static double impliedVolatility(final double forward, final double strike, final double timeToExpiry, final double mu, final double theta) {
    boolean isCall = strike >= forward;
    double p = price(forward, strike, timeToExpiry, isCall, mu, theta);
    return BlackFormulaRepository.impliedVolatility(p, forward, strike, timeToExpiry, isCall);
  }

  public static double dualDelta(final double forward, final double strike, final double timeToExpiry, final boolean isCall, final double mu, final double theta) {
    return BlackFormulaRepository.dualDelta(forward * Math.exp(mu), strike, timeToExpiry, theta, isCall);
  }

  public static double dVdK(final double forward, final double strike, final double timeToExpiry, final double mu, final double theta) {
    boolean isCall = strike >= forward;
    double vol = impliedVolatility(forward, strike, timeToExpiry, mu, theta);
    double dd = dualDelta(forward, strike, timeToExpiry, isCall, mu, theta);
    double blackDD = BlackFormulaRepository.dualDelta(forward, strike, timeToExpiry, vol, isCall);
    double blackVega = BlackFormulaRepository.vega(forward, strike, timeToExpiry, vol);
    return (dd - blackDD) / blackVega;
  }

}
