/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import org.apache.commons.lang.Validate;

import com.opengamma.util.timeseries.yearoffset.YearOffsetDoubleTimeSeries;

/**
 *
 */
public class SimpleYieldToMaturityBondYieldCalculator {

  public double calculate(final YearOffsetDoubleTimeSeries cashFlows, final double cleanPrice) {
    Validate.notNull(cashFlows, "cash flows");
    if (cashFlows.isEmpty()) {
      throw new IllegalArgumentException("Cash flow time series was empty");
    }
    if (cleanPrice <= 0) {
      throw new IllegalArgumentException("Clean price must be positive");
    }
    if (cashFlows.size() < 1) {
      throw new IllegalArgumentException("Need at least one cash flow to calculate current yield");
    }
    return (cashFlows.getEarliestValue() * 100 + (100 - cleanPrice) / cashFlows.getLatestTime()) / cleanPrice;
  }
}
