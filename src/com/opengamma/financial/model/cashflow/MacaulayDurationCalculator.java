/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.cashflow;

import java.util.Iterator;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.model.bond.BondYieldCalculator;
import com.opengamma.financial.model.interestrate.InterestRateModel;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.time.DateUtil;

/**
 * @author emcleod
 * 
 */
public class MacaulayDurationCalculator {
  private final BondYieldCalculator _yield = new BondYieldCalculator();

  public double calculate(final DoubleTimeSeries cashFlows, final double price, final ZonedDateTime date, final PresentValueCalculator pvCalculator) {
    if (cashFlows == null)
      throw new IllegalArgumentException("Cash flow time series was null");
    if (cashFlows.isEmpty())
      throw new IllegalArgumentException("Cash flow time series was empty");
    if (price <= 0)
      throw new IllegalArgumentException("Price must be positive");
    if (date == null)
      throw new IllegalArgumentException("Date was null");
    final Iterator<ZonedDateTime> iter = cashFlows.timeIterator();
    double sum = 0, t;
    ZonedDateTime d;
    final double y = _yield.calculate(cashFlows, price, date, pvCalculator);
    final InterestRateModel<Double> yield = new InterestRateModel<Double>() {

      @Override
      public double getInterestRate(final Double x) {
        return y;
      }

    };
    while (iter.hasNext()) {
      d = iter.next();
      t = DateUtil.getDifferenceInYears(date, d);
      sum += t * pvCalculator.calculate(t, cashFlows.getDataPoint(d), yield);
    }
    return sum / price;
  }
}
