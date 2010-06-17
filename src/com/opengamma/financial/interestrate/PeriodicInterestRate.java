/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class PeriodicInterestRate extends InterestRate {
  private final int _compoundingPeriodsPerYear;
  private final int _ratePeriodsPerYear;

  public PeriodicInterestRate(final double rate, final int compoundingPeriodsPerYear, final int ratePeriodsPerYear) {
    super(rate);
    ArgumentChecker.notNegativeOrZero(compoundingPeriodsPerYear, "compounding periods per year");
    ArgumentChecker.notNegativeOrZero(ratePeriodsPerYear, "rate periods per year");
    _compoundingPeriodsPerYear = compoundingPeriodsPerYear;
    _ratePeriodsPerYear = ratePeriodsPerYear;
  }

  public int getCompoundingPeriodsPerYear() {
    return _compoundingPeriodsPerYear;
  }

  public int getRatePeriodsPerYear() {
    return _ratePeriodsPerYear;
  }

  @Override
  public InterestRate fromAnnual(final AnnualInterestRate annual) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public InterestRate fromContinuous(final ContinuousInterestRate continuous) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public InterestRate fromPeriodic(final PeriodicInterestRate periodic) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public double getDiscountFactor(final double t) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public AnnualInterestRate toAnnual() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ContinuousInterestRate toContinuous() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PeriodicInterestRate toPeriodic(final int periodsPerYear) {
    // TODO Auto-generated method stub
    return null;
  }

}
