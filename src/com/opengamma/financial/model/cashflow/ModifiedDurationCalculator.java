/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.cashflow;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.bond.BondYieldCalculator;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class ModifiedDurationCalculator {
  // TODO this is correct for now, but when we have a bond cashflow instrument,
  // it will need to take number of payments per year into account
  private final MacaulayDurationCalculator _macaulay = new MacaulayDurationCalculator();
  private final BondYieldCalculator _yield = new BondYieldCalculator();

  public double calculate(final DoubleTimeSeries<Long> cashFlows, final double price, final Long date, final PresentValueCalculator pvCalculator) {
    Validate.notNull(cashFlows, "cash flows");
    if (cashFlows.isEmpty()) {
      throw new IllegalArgumentException("Cash flow time series was empty");
    }
    if (price <= 0) {
      throw new IllegalArgumentException("Price must be positive");
    }
    Validate.notNull(date, "date");
    Validate.notNull(pvCalculator, "present value calculator");

    final double d = _macaulay.calculate(cashFlows, price, date, pvCalculator);
    final double y = _yield.calculate(cashFlows, price, date, pvCalculator);
    return d / (1 + y);
  }
}
