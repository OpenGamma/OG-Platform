/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

/**
 *
 */
public abstract class InterestRate {

  /**
   * Enum describing the types of an interest rate
   */
  public enum Type {

    /** Simple interest rate */
    SIMPLE,
    /** Periodic interest rate */
    PERIODIC,
    /** Continuously-compounded interest rate */
    CONTINUOUS
  }

  private final double _rate;

  public InterestRate(final double rate) {
    _rate = rate;
  }

  public double getRate() {
    return _rate;
  }

  public abstract double getDiscountFactor(double t);

  /**
   * Create an InterestRate object with the correct composition type from a continuously compounded InterestRate object.
   * @param continuous The continuously compounded InterestRate.
   * @return The new object.
   */
  public abstract InterestRate fromContinuous(ContinuousInterestRate continuous);

  /**
   * Computes the derivative of the rate in the correct composition type from a continuously compounded InterestRate object.
   * @param continuous The continuously compounded InterestRate.
   * @return The derivative.
   */
  public abstract double fromContinuousDerivative(ContinuousInterestRate continuous);

  public abstract InterestRate fromPeriodic(PeriodicInterestRate periodic);

  public abstract ContinuousInterestRate toContinuous();

  public abstract PeriodicInterestRate toPeriodic(int periodsPerYear);

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_rate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final InterestRate other = (InterestRate) obj;
    if (Double.doubleToLongBits(_rate) != Double.doubleToLongBits(other._rate)) {
      return false;
    }
    return true;
  }

}
