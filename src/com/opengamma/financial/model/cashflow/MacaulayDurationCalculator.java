/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.cashflow;

import java.util.Iterator;

import com.opengamma.financial.model.bond.BondYieldCalculator;
import com.opengamma.financial.model.interestrate.InterestRateModel;
import com.opengamma.financial.model.interestrate.curve.ConstantInterestRateModel;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class MacaulayDurationCalculator {
  private final BondYieldCalculator _yield = new BondYieldCalculator();

  public double calculate(final DoubleTimeSeries<Long> cashFlows, final double price, final Long date,
      final PresentValueCalculator pvCalculator) {
    if (cashFlows == null) {
      throw new IllegalArgumentException("Cash flow time series was null");
    }
    if (cashFlows.isEmpty()) {
      throw new IllegalArgumentException("Cash flow time series was empty");
    }
    if (price <= 0) {
      throw new IllegalArgumentException("Price must be positive");
    }
    if (date == null) {
      throw new IllegalArgumentException("Date was null");
    }
    if (pvCalculator == null) {
      throw new IllegalArgumentException("Present value calculator was null");
    }
    final Iterator<Long> iter = cashFlows.timeIterator();
    double sum = 0, t;
    Long d;
    final InterestRateModel<Double> yield = new ConstantInterestRateModel(_yield.calculate(cashFlows, price, date,
        pvCalculator));
    while (iter.hasNext()) {
      d = iter.next();
      t = d - date;
      sum += t * pvCalculator.calculate(t, cashFlows.getValue(d), yield);
    }
    return sum / price;
  }
}
