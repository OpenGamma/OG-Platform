/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.cashflow;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.model.bond.BondYieldCalculator;
import com.opengamma.financial.model.interestrate.InterestRateModel;
import com.opengamma.financial.model.interestrate.curve.ConstantInterestRateModel;
import com.opengamma.timeseries.DoubleTimeSeries;

/**
 * @author emcleod
 * 
 */
public class EffectiveDurationCalculator {
  private final BondYieldCalculator _yield = new BondYieldCalculator();
  private final double _eps = 1e-3;

  public double calculate(final DoubleTimeSeries cashFlows, final double price, final ZonedDateTime date, final PresentValueCalculator pvCalculator) {
    if (cashFlows == null)
      throw new IllegalArgumentException("Cash flow time series was null");
    if (cashFlows.isEmpty())
      throw new IllegalArgumentException("Cash flow time series was empty");
    if (price <= 0)
      throw new IllegalArgumentException("Price must be positive");
    if (date == null)
      throw new IllegalArgumentException("Date was null");
    if (pvCalculator == null)
      throw new IllegalArgumentException("Present value calculator was null");
    final double yield = _yield.calculate(cashFlows, price, date, pvCalculator);
    final InterestRateModel<Double> yCurve = new ConstantInterestRateModel(yield);
    final InterestRateModel<Double> yCurveUp = new ConstantInterestRateModel(yield + _eps);
    final InterestRateModel<Double> yCurveDown = new ConstantInterestRateModel(yield - _eps);
    return (pvCalculator.calculate(cashFlows, yCurveDown, date) - pvCalculator.calculate(cashFlows, yCurveUp, date)) / (2 * pvCalculator.calculate(cashFlows, yCurve, date) * _eps);
  }
}
