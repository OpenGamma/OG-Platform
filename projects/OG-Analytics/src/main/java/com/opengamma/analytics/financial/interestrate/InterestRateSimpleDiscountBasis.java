/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import com.opengamma.util.ArgumentChecker;

/**
 * Class describing interest rate quoted as simple interest a discounting basis: discount factor = 1-r*t. 
 */
public class InterestRateSimpleDiscountBasis extends InterestRate {

  /**
   * Constructor.
   * @param rate The rate in the simple interest money market basis: discount factor = 1-r*t.
   */
  public InterestRateSimpleDiscountBasis(double rate) {
    super(rate);
  }

  @Override
  public double getDiscountFactor(double t) {
    double df = 1.0 - getRate() * t;
    ArgumentChecker.isTrue(df > 0, "Time not compatible with simple interest on a discount basis (1-r*t<0)");
    return df;
  }

  @Override
  public InterestRate fromContinuous(ContinuousInterestRate continuous) {
    throw new UnsupportedOperationException("Can not convert from continuous compounding to simple interest rate");
  }

  @Override
  public double fromContinuousDerivative(ContinuousInterestRate continuous) {
    throw new UnsupportedOperationException("Can not convert from continuous compounding to simple interest rate");
  }

  @Override
  public InterestRate fromPeriodic(PeriodicInterestRate periodic) {
    throw new UnsupportedOperationException("Can not convert from periodic compounding to simple interest rate");
  }

  @Override
  public ContinuousInterestRate toContinuous() {
    throw new UnsupportedOperationException("Can not convert from simple interest rate to continuous compounding");
  }

  @Override
  public PeriodicInterestRate toPeriodic(int periodsPerYear) {
    throw new UnsupportedOperationException("Can not convert from simple interest rate to periodic compounding");
  }

}
