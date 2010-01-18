/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.bond;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.rootfinding.RealSingleRootFinder;
import com.opengamma.math.rootfinding.VanWijngaardenDekkerBrentSingleRootFinder;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * @author emcleod
 * 
 */
public abstract class BondYieldCalculator {
  private final RealSingleRootFinder _root = new VanWijngaardenDekkerBrentSingleRootFinder();

  public double calculate(final DoubleTimeSeries cashFlows, final Double price, final ZonedDateTime date) {
    if (cashFlows == null)
      throw new IllegalArgumentException("Cash flow time series was null");
    if (cashFlows.isEmpty())
      throw new IllegalArgumentException("Cash flow time series was empty");
    if (price <= 0)
      throw new IllegalArgumentException("Price must be positive");
    if (date == null)
      throw new IllegalArgumentException("Date was null");
    return _root.getRoot(getFunction(cashFlows, price, date), 0., 10000.);
  }

  abstract Function1D<Double, Double> getFunction(DoubleTimeSeries cashFlows, double price, ZonedDateTime date);

}
