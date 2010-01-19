/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.cashflow;

import java.util.Iterator;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.model.interestrate.InterestRateModel;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.time.DateUtil;

/**
 * 
 * @author emcleod
 */
public abstract class PresentValueCalculator {

  public abstract double calculate(final double t, final double c, final InterestRateModel<Double> rates);

  public double calculate(final DoubleTimeSeries cashFlows, final InterestRateModel<Double> rates, final ZonedDateTime date) {
    if (cashFlows == null)
      throw new IllegalArgumentException("Cash flow time series was null");
    if (cashFlows.isEmpty())
      throw new IllegalArgumentException("Cash flow time series was empty");
    if (rates == null)
      throw new IllegalArgumentException("Interest rate model was null");
    if (date == null)
      throw new IllegalArgumentException("Date was null");
    final Iterator<ZonedDateTime> iter = cashFlows.timeIterator();
    ZonedDateTime d;
    double sum = 0, c, t;
    while (iter.hasNext()) {
      d = iter.next();
      t = DateUtil.getDifferenceInYears(date, d);
      c = cashFlows.getDataPoint(d);
      sum += calculate(t, c, rates);
    }
    return sum;
  }
}
