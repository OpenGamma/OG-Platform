/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.cashflow;

import java.util.Iterator;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.model.bond.BondYieldCalculator;
import com.opengamma.financial.model.bond.DiscretelyCompoundedBondYieldCalculator;
import com.opengamma.financial.model.interestrate.InterestRateModel;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.time.DateUtil;

/**
 * @author emcleod
 * 
 */
public class MacaulayDurationCalculator {
  private final BondYieldCalculator _yield = new DiscretelyCompoundedBondYieldCalculator();

  public double calculate(final DoubleTimeSeries cashFlows, final InterestRateModel<Double> rates, final double price, final ZonedDateTime date) {
    if (cashFlows == null)
      throw new IllegalArgumentException("Cash flow time series was null");
    if (cashFlows.isEmpty())
      throw new IllegalArgumentException("Cash flow time series was empty");
    if (rates == null)
      throw new IllegalArgumentException("Interest rate model was null");
    if (date == null)
      throw new IllegalArgumentException("Date was null");
    final Iterator<ZonedDateTime> iter = cashFlows.timeIterator();
    double sum = 0, t, pv;
    ZonedDateTime d;
    final double y = _yield.calculate(cashFlows, price, date);
    while (iter.hasNext()) {
      d = iter.next();
      t = DateUtil.getDifferenceInYears(date, d);
      pv = cashFlows.getDataPoint(d) * Math.pow(1 + y, -t);
      sum += t * pv;
    }
    return sum / price;
  }
}
