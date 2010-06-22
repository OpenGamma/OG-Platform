/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class AnnualInterestRate extends InterestRate {
  private final double _continuousEquivalent;

  public AnnualInterestRate(final double rate) {
    super(rate);
    _continuousEquivalent = Math.log(rate + 1);
  }

  @Override
  public InterestRate fromAnnual(final AnnualInterestRate annual) {
    Validate.notNull(annual);
    return new AnnualInterestRate(annual.getRate());
  }

  @Override
  public InterestRate fromContinuous(final ContinuousInterestRate continuous) {
    Validate.notNull(continuous);
    return new AnnualInterestRate(Math.exp(continuous.getRate()) - 1);
  }

  @Override
  public InterestRate fromPeriodic(final PeriodicInterestRate periodic) {
    Validate.notNull(periodic);
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public double getDiscountFactor(final double t) {
    return 1. / (1 + getRate());
  }

  @Override
  public AnnualInterestRate toAnnual() {
    return new AnnualInterestRate(getRate());
  }

  @Override
  public ContinuousInterestRate toContinuous() {
    return new ContinuousInterestRate(_continuousEquivalent);
  }

  @Override
  public PeriodicInterestRate toPeriodic(final int periodsPerYear) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String toString() {
    return "Annual[r = " + Double.toString(getRate()) + "]";
  }
}
