/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

/**
 * Class describing interest rate quoted as simple interest a money market basis: discount factor = 1/(1+r*t). 
 */
public class InterestRateSimpleMoneyMarketBasis extends InterestRate {

  /**
   * Constructor.
   * @param rate The rate in the simple interest money market basis: discount factor = 1/(1+r*t).
   */
  public InterestRateSimpleMoneyMarketBasis(double rate) {
    super(rate);
  }

  @Override
  public double getDiscountFactor(double t) {
    return 1.0 / (1 + getRate() * t);
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
