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
public class PeriodicInterestRate extends InterestRate {
  private final int _compoundingPeriodsPerYear;
  private final double _oneYearValue;

  public PeriodicInterestRate(final double rate, final int compoundingPeriodsPerYear) {
    super(rate);
    ArgumentChecker.notNegativeOrZero(compoundingPeriodsPerYear, "compounding periods per year");
    _compoundingPeriodsPerYear = compoundingPeriodsPerYear;
    _oneYearValue = Math.pow(1 + rate / compoundingPeriodsPerYear, compoundingPeriodsPerYear);
  }

  public int getCompoundingPeriodsPerYear() {
    return _compoundingPeriodsPerYear;
  }

  @Override
  public InterestRate fromContinuous(final ContinuousInterestRate continuous) {
    Validate.notNull(continuous, "continuous");
    final int m = getCompoundingPeriodsPerYear();
    return new PeriodicInterestRate(m * (Math.exp(continuous.getRate() / m) - 1), m);
  }

  @Override
  public double fromContinuousDerivative(final ContinuousInterestRate continuous) {
    Validate.notNull(continuous);
    return Math.exp(continuous.getRate() / getCompoundingPeriodsPerYear());
  }

  @Override
  public InterestRate fromPeriodic(final PeriodicInterestRate periodic) {
    Validate.notNull(periodic, "periodic");
    final int n = periodic.getCompoundingPeriodsPerYear();
    final double oneYearValue = Math.pow(1 + periodic.getRate() / n, n);
    final double r = getCompoundingPeriodsPerYear() * (Math.pow(oneYearValue, 1. / getCompoundingPeriodsPerYear()) - 1);
    return new PeriodicInterestRate(r, getCompoundingPeriodsPerYear());
  }

  @Override
  public double getDiscountFactor(final double t) {
    return Math.pow(_oneYearValue, -t);
  }

  @Override
  public ContinuousInterestRate toContinuous() {
    return new ContinuousInterestRate(Math.log(_oneYearValue));
  }

  @Override
  public PeriodicInterestRate toPeriodic(final int periodsPerYear) {
    ArgumentChecker.notNegativeOrZero(periodsPerYear, "periods per year");
    final double r = periodsPerYear * (Math.pow(_oneYearValue, 1. / periodsPerYear) - 1);
    return new PeriodicInterestRate(r, periodsPerYear);
  }

  @Override
  public String toString() {
    return "Periodic[r = " + getRate() + ", m = " + getCompoundingPeriodsPerYear() + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + _compoundingPeriodsPerYear;
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final PeriodicInterestRate other = (PeriodicInterestRate) obj;
    if (_compoundingPeriodsPerYear != other._compoundingPeriodsPerYear) {
      return false;
    }
    return true;
  }

}
