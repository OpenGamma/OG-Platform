/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.cashflow;

import java.util.Iterator;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.InterestRateModel;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public abstract class PresentValueCalculator {

  public abstract double calculate(final double t, final double c, final InterestRateModel<Double> rates);

  public double calculate(final DoubleTimeSeries<Long> cashFlows, final InterestRateModel<Double> rates, final Long date) {
    Validate.notNull(cashFlows, "cash flows");
    if (cashFlows.isEmpty()) {
      throw new IllegalArgumentException("Cash flow time series was empty");
    }
    Validate.notNull(date, "date");
    final Iterator<Long> iter = cashFlows.timeIterator();
    Long d;
    double sum = 0, c, t;
    while (iter.hasNext()) {
      d = iter.next();
      t = d - date;
      c = cashFlows.getValue(d);
      sum += calculate(t, c, rates);
    }
    return sum;
  }
}
