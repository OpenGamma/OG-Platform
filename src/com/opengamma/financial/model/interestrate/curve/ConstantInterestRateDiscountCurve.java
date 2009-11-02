/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.curve;

/**
 * 
 * 
 * A DiscountCurve that has a constant interest rate for all times in the
 * future.
 * 
 * @author emcleod
 */
public class ConstantInterestRateDiscountCurve extends DiscountCurve {
  private final double _rate;

  public ConstantInterestRateDiscountCurve(final Double rate) {
    _rate = rate;
  }

  @Override
  public double getInterestRate(final Double t) {
    return _rate;
  }

  @Override
  public double getDiscountFactor(final Double t) {
    return Math.exp(-_rate * t);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(_rate);
    result = prime * result + (int) (temp ^ temp >>> 32);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final ConstantInterestRateDiscountCurve other = (ConstantInterestRateDiscountCurve) obj;
    if (Double.doubleToLongBits(_rate) != Double.doubleToLongBits(other._rate))
      return false;
    return true;
  }
}
