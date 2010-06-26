/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.bond;

import org.apache.commons.lang.Validate;

import com.opengamma.util.timeseries.yearoffset.YearOffsetDoubleTimeSeries;

/**
 *
 */
public class CouponYieldBondYieldCalculator {

  public double calculate(final YearOffsetDoubleTimeSeries cashFlows, final double faceValue) {
    Validate.notNull(cashFlows, "cash flows");
    if (cashFlows.isEmpty()) {
      throw new IllegalArgumentException("Cash flow time series was empty");
    }
    if (faceValue <= 0) {
      throw new IllegalArgumentException("Face value must be positive");
    }
    if (cashFlows.size() < 1) {
      throw new IllegalArgumentException("Need at least one cash flow to calculate current yield");
    }
    return cashFlows.getEarliestValue() / faceValue;
  }
}
