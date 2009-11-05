/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.curve;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * A DiscountCurve that has a constant interest rate for all times in the
 * future.
 * 
 * @author emcleod
 */
public class ConstantInterestRateDiscountCurve extends DiscountCurve {
  private static final Logger s_Log = LoggerFactory.getLogger(ConstantInterestRateDiscountCurve.class);
  private final double _rate;

  public ConstantInterestRateDiscountCurve(final Double rate) {
    if (rate < 0)
      throw new IllegalArgumentException("Cannot have a negative interest rate");
    _rate = rate;
  }

  @Override
  public double getInterestRate(final Double t) {
    if (t < 0)
      throw new IllegalArgumentException("t was less than zero");
    return _rate;
  }

  @Override
  public double getDiscountFactor(final Double t) {
    if (t < 0)
      throw new IllegalArgumentException("t was less than zero");
    return Math.exp(-_rate * t);
  }

  @Override
  public Set<Double> getMaturities() {
    return Collections.<Double> emptySet();
  }

  @Override
  public DiscountCurve withParallelShift(final Double shift) {
    if (shift == null)
      throw new IllegalArgumentException("Shift was null");
    return new ConstantInterestRateDiscountCurve(_rate + shift);
  }

  @Override
  public DiscountCurve withSingleShift(final Double t, final Double shift) {
    if (t == null)
      throw new IllegalArgumentException("t was null");
    if (shift == null)
      throw new IllegalArgumentException("Shift was null");
    if (t < 0)
      throw new IllegalArgumentException("t was less than zero");
    return new ConstantInterestRateDiscountCurve(_rate + shift);
  }

  @Override
  public DiscountCurve withMultipleShifts(final Map<Double, Double> shifts) {
    if (shifts == null)
      throw new IllegalArgumentException("Shift map was null");
    if (shifts.isEmpty()) {
      s_Log.info("Shift map was empty; returning original curve");
      return new ConstantInterestRateDiscountCurve(_rate);
    }
    if (shifts.size() != 1) {
      s_Log.warn("Shift map contained more than one element - only using first");
    }
    final Map.Entry<Double, Double> firstEntry = shifts.entrySet().iterator().next();
    if (firstEntry.getKey() < 0)
      throw new IllegalArgumentException("Time for shift was less than zero");
    if (firstEntry.getValue() == null)
      throw new IllegalArgumentException("Value for shift was null");
    return new ConstantInterestRateDiscountCurve(_rate + firstEntry.getValue());
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
