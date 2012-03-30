/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import org.apache.commons.lang.Validate;

import com.opengamma.util.ArgumentChecker;

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
  public double fromContinuousDerivative(final ContinuousInterestRate continuous) {
    Validate.notNull(continuous);
    return Math.exp(continuous.getRate());
  }

  @Override
  public InterestRate fromPeriodic(final PeriodicInterestRate periodic) {
    Validate.notNull(periodic);
    final int m = periodic.getCompoundingPeriodsPerYear();
    return new AnnualInterestRate(Math.pow(1 + periodic.getRate() / m, m) - 1);
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
    ArgumentChecker.notNegativeOrZero(periodsPerYear, "compounding periods per year");
    return new PeriodicInterestRate(getRate(), 1);
  }

  @Override
  public String toString() {
    return "Annual[r = " + Double.toString(getRate()) + "]";
  }
}
