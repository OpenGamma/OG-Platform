/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.bond;

import java.util.Iterator;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.math.function.Function1D;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.util.time.DateUtil;

/**
 * @author emcleod
 * 
 */
public class DiscretelyCompoundedBondYieldCalculator extends BondYieldCalculator {

  /*
   * (non-Javadoc)
   * 
   * @see
   * 
   * 
   * 
   * com.opengamma.financial.model.interestrate.bond.BondYieldCalculator#getFunction
   * (com.opengamma.timeseries.DoubleTimeSeries, double,
   * javax.time.calendar.ZonedDateTime)
   */
  @Override
  Function1D<Double, Double> getFunction(final DoubleTimeSeries cashFlows, final double price, final ZonedDateTime date) {
    return new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double y) {
        final Iterator<ZonedDateTime> iter = cashFlows.timeIterator();
        double sum = 0, t;
        ZonedDateTime d;
        while (iter.hasNext()) {
          d = iter.next();
          t = DateUtil.getDifferenceInYears(date, d);
          sum += cashFlows.getDataPoint(d) * Math.pow(1 + y, -t);
        }
        return sum - price;
      }
    };
  }
}
