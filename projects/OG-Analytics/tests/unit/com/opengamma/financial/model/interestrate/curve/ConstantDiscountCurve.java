/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.curve;

import java.util.Map;
import java.util.Set;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ConstantDiscountCurve extends YieldAndDiscountCurve {
  private final double _df;

  public ConstantDiscountCurve(final double df) {
    if (!ArgumentChecker.isInRangeExcludingLow(0, 1, df)) {
      throw new IllegalArgumentException("Discount factor must be < 0 and >= 1");
    }
    _df = df;
  }

  @Override
  public double getDiscountFactor(final Double t) {
    return _df;
  }

  @Override
  public double getInterestRate(final Double t) {
    return Math.log(_df) / -t;
  }

  @Override
  public Set<Double> getMaturities() {
    return null;
  }

  @Override
  public YieldAndDiscountCurve withMultipleShifts(final Map<Double, Double> shifts) {
    return null;
  }

  @Override
  public YieldAndDiscountCurve withParallelShift(final Double shift) {
    return null;
  }

  @Override
  public YieldAndDiscountCurve withSingleShift(final Double t, final Double shift) {
    return null;
  }

}
