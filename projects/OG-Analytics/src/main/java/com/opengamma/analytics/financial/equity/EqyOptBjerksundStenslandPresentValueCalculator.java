/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity;

import com.opengamma.analytics.financial.equity.option.EquityIndexFutureOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityOption;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BjerksundStenslandModel;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculates the present value of equity options using the Black method.
 */
public final class EqyOptBjerksundStenslandPresentValueCalculator extends InstrumentDerivativeVisitorAdapter<StaticReplicationDataBundle, Double> {
  /** A static instance */
  private static final EqyOptBjerksundStenslandPresentValueCalculator INSTANCE = new EqyOptBjerksundStenslandPresentValueCalculator();
  /** The present value calculator */
  private static final BjerksundStenslandModel MODEL = new BjerksundStenslandModel();

  /**
   * Gets the static instance
   * @return The static instance
   */
  public static EqyOptBjerksundStenslandPresentValueCalculator getInstance() {
    return INSTANCE;
  }

  private EqyOptBjerksundStenslandPresentValueCalculator() {
  }

  @Override
  public Double visitEquityIndexOption(final EquityIndexOption option, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");
    final double spot = data.getForwardCurve().getSpot();
    final double strike = option.getStrike();
    final double time = option.getTimeToExpiry();
    final double sigma = data.getVolatilitySurface().getVolatility(time, strike);
    final boolean isCall = option.isCall();
    final double interestRate = data.getDiscountCurve().getInterestRate(time);
    final double costOfCarry = interestRate; //TODO
    return option.getUnitAmount() * MODEL.price(spot, strike, interestRate, costOfCarry, time, sigma, isCall);
  }

  @Override
  public Double visitEquityOption(final EquityOption option, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");
    final double spot = data.getForwardCurve().getSpot();
    final double strike = option.getStrike();
    final double time = option.getTimeToExpiry();
    final double sigma = data.getVolatilitySurface().getVolatility(time, strike);
    final boolean isCall = option.isCall();
    final double interestRate = data.getDiscountCurve().getInterestRate(time);
    final double forward = data.getForwardCurve().getForward(time);
    final double costOfCarry = Math.log(forward / spot) / time;
    return option.getUnitAmount() * MODEL.price(spot, strike, interestRate, costOfCarry, time, sigma, isCall);
  }

  @Override
  public Double visitEquityIndexFutureOption(final EquityIndexFutureOption option, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");
    final double spot = data.getForwardCurve().getSpot();
    final double strike = option.getStrike();
    final double time = option.getExpiry();
    final double sigma = data.getVolatilitySurface().getVolatility(time, strike);
    final boolean isCall = option.isCall();
    final double interestRate = data.getDiscountCurve().getInterestRate(time);
    final double costOfCarry = interestRate; //TODO
    return option.getPointValue() * MODEL.price(spot, strike, interestRate, costOfCarry, time, sigma, isCall);
  }
}
