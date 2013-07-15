/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.scratchpad;

/**
 * 
 */
public class InterestRateSwap {

  private final double _notional;
  private final double _swapRate;
  private final double _maturity;

  private final InterestRateProcess _interestRateProcess;

  public InterestRateSwap(final double notional, final double swapRate, final double maturity, final InterestRateProcess interestRateProcess) {

    _notional = notional;
    _swapRate = swapRate;
    _maturity = maturity;

    _interestRateProcess = interestRateProcess;

  }

  public double getNotional() {
    return _notional;
  }

  public double getSwapRate() {
    return _swapRate;
  }

  public double getMaturity() {
    return _maturity;
  }

  public InterestRateProcess getInterestRateProcess() {
    return _interestRateProcess;
  }
}
