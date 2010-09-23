/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.curve;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;

/**
 * A YieldAndDiscountCurve that has a constant interest rate for all times in the
 * future.
 */
public class ConstantYieldCurve extends YieldAndDiscountCurve {
  private static final Logger s_logger = LoggerFactory.getLogger(ConstantYieldCurve.class);
  private final double _rate;

  public ConstantYieldCurve(final double rate) {
    ArgumentChecker.notNegative(rate, "rate");
    _rate = rate;
  }

  @Override
  public double getInterestRate(final Double t) {
    Validate.notNull(t);
    ArgumentChecker.notNegative(t, "time");
    return _rate;
  }

  @Override
  public double getDiscountFactor(final Double t) {
    Validate.notNull(t);
    ArgumentChecker.notNegative(t, "time");
    return Math.exp(-_rate * t); // TODO only works for continuously compounded rates
  }

  @Override
  public Set<Double> getMaturities() {
    return Collections.emptySet();
  }

  @Override
  public YieldAndDiscountCurve withParallelShift(final Double shift) {
    Validate.notNull(shift);
    return new ConstantYieldCurve(_rate + shift);
  }

  // REVIEW R White - this is inconsistent, it is really just doing a parallel shift
  @Override
  public YieldAndDiscountCurve withSingleShift(final Double t, final Double shift) {
    Validate.notNull(t);
    Validate.notNull(shift);
    ArgumentChecker.notNegative(t, "time");
    return new ConstantYieldCurve(_rate + shift);
  }

  @Override
  public YieldAndDiscountCurve withMultipleShifts(final Map<Double, Double> shifts) {
    Validate.notNull(shifts);
    if (shifts.isEmpty()) {
      s_logger.info("Shift map was empty; returning unchanged curve");
      return new ConstantYieldCurve(_rate);
    }
    if (shifts.size() != 1) {
      s_logger.warn("Shift map contained more than one element - only using first in time");
    }
    final Map<Double, Double> sorted = new TreeMap<Double, Double>(shifts);
    final Map.Entry<Double, Double> firstEntry = sorted.entrySet().iterator().next();
    Validate.notNull(firstEntry);
    ArgumentChecker.notNegative(firstEntry.getKey(), "time");
    Validate.notNull(firstEntry.getValue());
    return new ConstantYieldCurve(_rate + firstEntry.getValue());
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
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ConstantYieldCurve other = (ConstantYieldCurve) obj;
    if (Double.doubleToLongBits(_rate) != Double.doubleToLongBits(other._rate)) {
      return false;
    }
    return true;
  }
}
