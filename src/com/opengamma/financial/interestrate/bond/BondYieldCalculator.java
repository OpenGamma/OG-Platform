/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.cashflow.PresentValueCalculator;
import com.opengamma.financial.model.interestrate.InterestRateModel;
import com.opengamma.financial.model.interestrate.curve.ConstantInterestRateModel;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.rootfinding.RealSingleRootFinder;
import com.opengamma.math.rootfinding.VanWijngaardenDekkerBrentSingleRootFinder;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class BondYieldCalculator {
  private final RealSingleRootFinder _root = new VanWijngaardenDekkerBrentSingleRootFinder();

  public double calculate(final DoubleTimeSeries<Long> cashFlows, final Double price, final Long date, final PresentValueCalculator pvCalculator) {
    Validate.notNull(cashFlows, "cash flows");
    if (cashFlows.isEmpty()) {
      throw new IllegalArgumentException("Cash flow time series was empty");
    }
    if (price <= 0) {
      throw new IllegalArgumentException("Price must be positive");
    }
    Validate.notNull(date, "date");
    Validate.notNull(pvCalculator, "presenve value calculator");
    final Function1D<Double, Double> f = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double y) {
        final InterestRateModel<Double> rate = new ConstantInterestRateModel(y);
        return pvCalculator.calculate(cashFlows, rate, date) - price;
      }

    };
    return _root.getRoot(f, 0., 100000.);
  }
}
