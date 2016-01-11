/**
 * Copyright (C) 2016 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility;

import com.opengamma.util.ArgumentChecker;

/**
 * Computes the implied volatility in a Bachelier (normal) model from the Black (log-normal) implied volatility.
 */
public class NormalFromBlackImpliedVolatilityFormula {
  
  /** Limit defining "close of ATM forward" to avoid the formula singularity. **/
  private static final double ATM_LIMIT = 1.0E-3;
  
  /**
   * Compute the implied volatility using an approximate explicit transformation formula.
   * <p>
   * Reference: Hagan, P. S. Volatility conversion calculator. Technical report, Bloomberg.
   * 
   * @param forward  the forward rate/price
   * @param strike  the option strike
   * @param timeToExpiry  the option time to maturity
   * @param blackVolatility  the Black implied volatility
   * @return the implied volatility
   */
  public static double impliedVolatilityApproximate(
      double forward,
      double strike,
      double timeToExpiry,
      double blackVolatility) {
    ArgumentChecker.isTrue(strike > 0, "strike must be strctly positive");
    ArgumentChecker.isTrue(forward > 0, "strike must be strctly positive");
    double lnFK = Math.log(forward / strike);
    double s2t = blackVolatility * blackVolatility * timeToExpiry;
    if (Math.abs((forward - strike) / strike) < ATM_LIMIT) {
      double factor1 = Math.sqrt(forward * strike);
      double factor2 = (1.0d + lnFK * lnFK / 24.0d) / (1.0d + s2t / 24.0d + s2t * s2t / 5670.0d);
      return blackVolatility * factor1 * factor2;
    }
    double factor1 = (forward - strike) / lnFK;
    double factor2 = 1.0d / (1.0d + (1.0d - lnFK * lnFK / 120.0d) / 24.0d * s2t + s2t * s2t / 5670.0d);
    return blackVolatility * factor1 * factor2;
  }

}
