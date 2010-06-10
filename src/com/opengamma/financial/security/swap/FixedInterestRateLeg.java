/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.swap;

import com.opengamma.util.ArgumentChecker;

/**
 * Represents a fixed interest rate leg of a swap.
 */
public class FixedInterestRateLeg extends InterestRateLeg {
  private double _rate; // TODO: Elaine 28-May-2010 -- change rate from double to InterestRateType.

  /**
   * @param rate the fixed interest rate as a decimal (e,g, 5% = 0.05)
   */
  public FixedInterestRateLeg(double rate) {
    super();
    ArgumentChecker.notNegative(rate, "rate");
    _rate = rate;
  }
  
  /**
   * @return the fixed interest rate as a decimal (e.g. 5% = 0.05)
   */
  public double getRate() {
    return _rate;
  }
}
